package com.foodmate.backend.service.impl;

import com.foodmate.backend.dto.RankingDto;
import com.foodmate.backend.entity.Member;
import com.foodmate.backend.repository.MemberRepository;
import com.foodmate.backend.service.RankingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RankingServiceImpl implements RankingService {

    private final MemberRepository memberRepository;

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

}
