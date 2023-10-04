package com.foodmate.backend.service.impl;

import com.foodmate.backend.dto.GroupDto;
import com.foodmate.backend.entity.ChatRoom;
import com.foodmate.backend.entity.Food;
import com.foodmate.backend.entity.FoodGroup;
import com.foodmate.backend.entity.Member;
import com.foodmate.backend.enums.Error;
import com.foodmate.backend.exception.FoodException;
import com.foodmate.backend.exception.GroupException;
import com.foodmate.backend.exception.MemberException;
import com.foodmate.backend.repository.ChatRoomRepository;
import com.foodmate.backend.repository.FoodGroupRepository;
import com.foodmate.backend.repository.FoodRepository;
import com.foodmate.backend.repository.MemberRepository;
import com.foodmate.backend.service.GroupService;
import lombok.RequiredArgsConstructor;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class GroupServiceImpl implements GroupService {

    private static final int RESERVATION_INTERVAL_HOUR = 1;
    private static final int RESERVATION_RANGE_MONTH = 1;

    private final MemberRepository memberRepository;
    private final FoodRepository foodRepository;
    private final FoodGroupRepository foodGroupRepository;
    private final ChatRoomRepository chatRoomRepository;

    // 모임 생성
    @Override
    public String addGroup(Authentication authentication, GroupDto.Request request) {

        Member member = getMember(authentication);

        // 푸드 카테고리 체크
        Food food = foodRepository.findByType(request.getFood())
                .orElseThrow(() -> new FoodException(Error.FOOD_NOT_FOUND));

        // 모임일시
        LocalDateTime groupDateTime = request.getDate().atTime(request.getTime());

        // 현재시간으로부터 한시간 이후 ~ 한달 이내의 모임만 생성 가능
        if (groupDateTime.isBefore(LocalDateTime.now().plusHours(RESERVATION_INTERVAL_HOUR)) ||
                groupDateTime.isAfter(LocalDateTime.now().plusMonths(RESERVATION_RANGE_MONTH))) {
            throw new GroupException(Error.OUT_OF_DATE_RANGE);
        }

        // 좌표 생성
        Point storeLocation = new GeometryFactory().createPoint(
                new Coordinate(Double.parseDouble(request.getLongitude()), Double.parseDouble(request.getLatitude())));

        FoodGroup foodGroup = FoodGroup.builder()
                .member(member)
                .title(request.getTitle())
                .name(request.getName())
                .content(request.getContent())
                .food(food)
                .groupDateTime(groupDateTime)
                .maximum(request.getMaximum())
                .storeName(request.getStoreName())
                .storeAddress(request.getStoreAddress())
                .location(storeLocation)
                .build();

        // 모임 저장
        foodGroupRepository.save(foodGroup);

        // TODO 채팅룸 생성 - 그룹 채팅 기능 구현 후 보완할 것
        chatRoomRepository.save(new ChatRoom(foodGroup));

        return "모임 생성 완료";
    }

    private Member getMember(Authentication authentication) {
        return memberRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new MemberException(Error.USER_NOT_FOUND));
    }

}
