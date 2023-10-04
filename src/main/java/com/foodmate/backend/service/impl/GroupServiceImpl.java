package com.foodmate.backend.service.impl;

import com.foodmate.backend.dto.GroupDto;
import com.foodmate.backend.entity.*;
import com.foodmate.backend.enums.EnrollmentStatus;
import com.foodmate.backend.enums.Error;
import com.foodmate.backend.exception.*;
import com.foodmate.backend.repository.*;
import com.foodmate.backend.service.GroupService;
import lombok.RequiredArgsConstructor;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    private final EnrollmentRepository enrollmentRepository;

    // 모임 생성
    @Override
    public String addGroup(Authentication authentication, GroupDto.Request request) {

        Member member = getMember(authentication);

        // 입력된 음식 정보 올바른지 체크
        Food food = validateFood(request.getFood());

        // 입력된 모임일시 올바른지 체크
        LocalDateTime groupDateTime = validateDateTime(request);

        // 좌표 생성
        Point storeLocation = getPoint(request.getLatitude(), request.getLongitude());

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

    // 특정 모임 상세 조회
    @Override
    public GroupDto.DetailResponse getGroupDetail(Long groupId) {

        FoodGroup group = validateGroupId(groupId);

        // 현재 인원은 모임 생성자 포함이니까 +1 해줘야
        int current = enrollmentRepository.countByFoodGroupIdAndStatus(groupId, EnrollmentStatus.ACCEPT) + 1;

        ChatRoom chatRoom = chatRoomRepository.findByFoodGroupId(groupId)
                .orElseThrow(() -> new ChatException(Error.CHATROOM_NOT_FOUND));

        return GroupDto.DetailResponse.fromEntity(group, current, chatRoom);
    }

    // 특정 모임 수정
    @Override
    public String updateGroup(Long groupId, Authentication authentication, GroupDto.Request request) {

        FoodGroup group = validateGroupId(groupId);

        Member member = getMember(authentication);

        // 해당 모임 생성자만 수정 가능
        if (group.getMember().getId() != member.getId()) {
            throw new GroupException(Error.NO_MODIFY_PERMISSION_GROUP);
        }

        // 입력된 음식 정보 올바른지 체크
        Food food = validateFood(request.getFood());

        // 입력된 모임일시 올바른지 체크
        LocalDateTime groupDateTime = validateDateTime(request);

        // 좌표 생성
        Point storeLocation = getPoint(request.getLatitude(), request.getLongitude());

        group.setTitle(request.getTitle());
        group.setName(request.getName());
        group.setContent(request.getContent());
        group.setFood(food);
        group.setGroupDateTime(groupDateTime);
        group.setMaximum(request.getMaximum());
        group.setStoreName(request.getStoreName());
        group.setStoreAddress(request.getStoreAddress());
        group.setLocation(storeLocation);

        foodGroupRepository.save(group);

        return "모임 수정 완료";
    }

    // TODO 삭제된 모임의 댓글 대댓글 일괄삭제 - 스케쥴링으로 하루에 한번?
    // 특정 모임 삭제
    @Transactional
    @Override
    public String deleteGroup(Long groupId, Authentication authentication) {

        FoodGroup group = validateGroupId(groupId);

        Member member = getMember(authentication);

        // 해당 모임 생성자만 삭제 가능
        if (group.getMember().getId() != member.getId()) {
            throw new GroupException(Error.NO_DELETE_PERMISSION_GROUP);
        }

        group.setIsDeleted(LocalDateTime.now());
        foodGroupRepository.save(group);

        // 해당 모임에 신청한 모임신청목록들의 상태를 모임취소로 일괄 변경
        enrollmentRepository.changeStatusByGroupId(groupId, EnrollmentStatus.GROUP_CANCEL);

        // TODO 채팅방 삭제 - 그룹 채팅 기능 구현 후 보완할 것
        chatRoomRepository.deleteByFoodGroupId(groupId);

        return "모임 삭제 완료";
    }

    // TODO 동시성 1차 체크?
    // 특정 모임 신청
    @Override
    public String enrollInGroup(Long groupId, Authentication authentication) {

        FoodGroup group = validateGroupId(groupId);

        Member member = getMember(authentication);

        // 이미 신청 이력이 존재할 경우
        if (enrollmentRepository.existsByMemberAndFoodGroup(member, group)) {
            throw new EnrollmentException(Error.ENROLLMENT_HISTORY_EXISTS);
        }

        // 현재 인원은 모임 생성자 포함이니까 +1 해줘야
        int current = enrollmentRepository.countByFoodGroupIdAndStatus(groupId, EnrollmentStatus.ACCEPT) + 1;

        // 모임 현재인원 체크
        if (current >= group.getMaximum()) {
            throw new EnrollmentException(Error.GROUP_FULL);
        }

        enrollmentRepository.save(Enrollment.builder()
                .member(member)
                .foodGroup(group)
                .status(EnrollmentStatus.SUBMIT)
                .build());

        return "신청 완료";
    }

    // {groupId} 경로 검증 - 존재하는 그룹이면서, 삭제되지 않은 경우만 반환
    private FoodGroup validateGroupId(Long groupId) {

        FoodGroup group = foodGroupRepository.findById(groupId)
                .orElseThrow(() -> new GroupException(Error.GROUP_NOT_FOUND));

        if (group.getIsDeleted() != null) {
            throw new GroupException(Error.GROUP_DELETED);
        }

        return group;
    }

    private Member getMember(Authentication authentication) {
        return memberRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new MemberException(Error.USER_NOT_FOUND));
    }

    private Food validateFood(String foodName) {
        return foodRepository.findByType(foodName)
                .orElseThrow(() -> new FoodException(Error.FOOD_NOT_FOUND));
    }

    private LocalDateTime validateDateTime(GroupDto.Request request) {

        // 입력된 모임일시
        LocalDateTime groupDateTime = request.getDate().atTime(request.getTime());

        // 현재시간으로부터 한시간 이후 ~ 한달 이내의 모임만 생성 가능
        if (groupDateTime.isBefore(LocalDateTime.now().plusHours(RESERVATION_INTERVAL_HOUR)) ||
                groupDateTime.isAfter(LocalDateTime.now().plusMonths(RESERVATION_RANGE_MONTH))) {
            throw new GroupException(Error.OUT_OF_DATE_RANGE);
        }

        return groupDateTime;
    }

    private Point getPoint(String latitude, String longitude) {
        return new GeometryFactory().createPoint(
                new Coordinate(Double.parseDouble(latitude), Double.parseDouble(longitude)));
    }

}
