package com.foodmate.backend.service;

import com.foodmate.backend.dto.RankingDto;

import java.util.List;

public interface RankingService {

    // 좋아요 랭킹
    List<RankingDto.Likes> showLikesRanking();

}
