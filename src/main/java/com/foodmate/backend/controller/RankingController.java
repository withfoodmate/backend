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
    public ResponseEntity<List<RankingDto.Likes>> getLikesRanking() {
        return ResponseEntity.ok(rankingService.getLikesRanking());
    }

    // 모임왕 랭킹
    @GetMapping("/meeting")
    public ResponseEntity<List<RankingDto.Meeting>> getMeetingRanking() {
        return ResponseEntity.ok(rankingService.getMeetingRanking());
    }

    // 많이찾는 식당 랭킹
    @GetMapping("/store")
    public ResponseEntity<List<RankingDto.Store>> getStoreRanking() {
        return ResponseEntity.ok(rankingService.getStoreRanking());
    }

    // 음식 카테고리 랭킹
    @GetMapping("/food")
    public ResponseEntity<List<RankingDto.Food>> getFoodRanking() {
        return ResponseEntity.ok(rankingService.getFoodRanking());
    }

}
