package com.foodmate.backend.service;

import com.foodmate.backend.dto.RankingDto;

import java.util.List;

public interface RankingService {

    // 좋아요 랭킹
    List<RankingDto.Likes> showLikesRanking();

    // 모임왕 랭킹
    List<RankingDto.Meeting> showMeetingRanking();

    // 많이찾는 식당 랭킹
    List<RankingDto.Store> showStoreRanking();

    // 음식 카테고리 랭킹
    List<RankingDto.Food> showFoodRanking();

}
