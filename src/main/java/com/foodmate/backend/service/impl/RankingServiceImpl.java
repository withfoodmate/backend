package com.foodmate.backend.service.impl;

import com.foodmate.backend.dto.RankingDto;
import com.foodmate.backend.entity.Member;
import com.foodmate.backend.repository.FoodGroupRepository;
import com.foodmate.backend.repository.MemberRepository;
import com.foodmate.backend.service.RankingService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RankingServiceImpl implements RankingService {

    private final MemberRepository memberRepository;
    private final FoodGroupRepository foodGroupRepository;

    // 좋아요 랭킹
    @Override
    public List<RankingDto.Likes> showLikesRanking() {

        List<RankingDto.Likes> result = new ArrayList<>();

        List<Member> members = memberRepository.findTop10ByOrderByLikesDesc();

        for (Member member : members) {
            result.add(RankingDto.Likes.builder()
                    .memberId(member.getId())
                    .nickname(member.getNickname())
                    .image(member.getImage())
                    .likesCount(member.getLikes())
                    .build());
        }

        return result;
    }

    // 모임왕 랭킹
    @Override
    public List<RankingDto.Meeting> showMeetingRanking() {

        List<RankingDto.Meeting> result = new ArrayList<>();

        List<Object[]> list = memberRepository.findTop10MemberWithCount(PageRequest.of(0, 10));

        for (Object[] item : list) {
            result.add(RankingDto.Meeting.builder()
                    .memberId((Long) item[0])
                    .nickname((String) item[1])
                    .image((String) item[2])
                    .count((long) item[3])
                    .build());
        }

        return result;
    }

    // 많이찾는 식당 랭킹
    @Override
    public List<RankingDto.Store> showStoreRanking() {

        List<RankingDto.Store> result = new ArrayList<>();

        List<Object[]> list = foodGroupRepository.findTop10StoreWithCount(PageRequest.of(0, 10));

        for (Object[] item : list) {
            result.add(RankingDto.Store.builder()
                    .storeName((String) item[0])
                    .address((String) item[1])
                    .count((long) item[2])
                    .build());
        }

        return result;
    }

}
