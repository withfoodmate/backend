package com.foodmate.backend.controller;

import com.foodmate.backend.dto.RankingDto;
import com.foodmate.backend.service.RankingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/ranking")
public class RankingController {

    private final RankingService rankingService;

    // 좋아요 랭킹
    @GetMapping("/likes")
    public ResponseEntity<List<RankingDto.Likes>> showLikesRanking() {
        return ResponseEntity.ok(rankingService.showLikesRanking());
    }

    // 모임왕 랭킹
    @GetMapping("/meeting")
    public ResponseEntity<List<RankingDto.Meeting>> showMeetingRanking() {
        return ResponseEntity.ok(rankingService.showMeetingRanking());
    }

    // 많이찾는 식당 랭킹
    @GetMapping("/store")
    public ResponseEntity<List<RankingDto.Store>> showStoreRanking() {
        return ResponseEntity.ok(rankingService.showStoreRanking());
    }

    // 음식 카테고리 랭킹
    @GetMapping("/food")
    public ResponseEntity<List<RankingDto.Food>> showFoodRanking() {
        return ResponseEntity.ok(rankingService.showFoodRanking());
    }

}
