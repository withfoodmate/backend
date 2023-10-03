package com.foodmate.backend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/ranking")
public class RankingController {

    @GetMapping("/like")
    public ResponseEntity<?> showLikeRanking() {
        return ResponseEntity.ok("");
    }

    @GetMapping("/meeting")
    public ResponseEntity<?> showMeetingRanking() {
        return ResponseEntity.ok("");
    }

    @GetMapping("/store")
    public ResponseEntity<?> showStoreRanking() {
        return ResponseEntity.ok("");
    }

    @GetMapping("/food")
    public ResponseEntity<?> showFoodRanking() {
        return ResponseEntity.ok("");
    }

}
