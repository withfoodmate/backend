package com.foodmate.backend.service.impl;

import com.foodmate.backend.repository.FoodRepository;
import com.foodmate.backend.repository.GroupRepository;
import com.foodmate.backend.repository.MemberRepository;
import com.foodmate.backend.service.RankingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RankingServiceImpl implements RankingService {

    private final MemberRepository memberRepository;
    private final GroupRepository groupRepository;
    private final FoodRepository foodRepository;

}
