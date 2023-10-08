package com.foodmate.backend.dto;

import lombok.Builder;
import lombok.Getter;

public class RankingDto {

    @Getter
    @Builder
    public static class Likes {
        private Long memberId;
        private String nickname;
        private String image;
        private long likesCount;
    }

    @Getter
    @Builder
    public static class Meeting {
        private Long memberId;
        private String nickname;
        private String image;
        private long count;
    }

    @Getter
    @Builder
    public static class Store {
        private String storeName;
        private String address;
        private long count;
    }

    @Getter
    @Builder
    public static class Food {
        private String foodName;
        private String image;
        private long count;
    }

}
