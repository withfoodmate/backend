package com.foodmate.backend.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.foodmate.backend.entity.ChatRoom;
import com.foodmate.backend.entity.FoodGroup;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import javax.validation.constraints.Max;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

public class GroupDto {

    @Getter
    @NotNull
    @AllArgsConstructor
    @Builder
    public static class Request {
        private String title;
        private String name;
        private String content;
        private String food;
        @JsonFormat(pattern = "yyyy-MM-dd")
        private LocalDate date;
        @JsonFormat(pattern = "HH:mm", timezone = "Asia/Seoul")
        private LocalTime time;
        @Max(value = 8, message = "최대 인원은 8명까지 가능합니다.")
        private int maximum;
        private String storeName;
        private String storeAddress;
        private String latitude;
        private String longitude;
    }

    @Getter
    @Builder
    public static class DetailResponse {
        private Long groupId;
        private Long memberId;
        private String nickname;
        private String image;
        private String title;
        private String name;
        private String content;
        private String food;
        @JsonFormat(pattern = "yyyy-MM-dd")
        private LocalDate date;
        @JsonFormat(pattern = "HH:mm", timezone = "Asia/Seoul")
        private LocalTime time;
        private int maximum;
        private int current;
        private String storeName;
        private String storeAddress;
        private String latitude;
        private String longitude;
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm", timezone = "Asia/Seoul")
        private LocalDateTime createdDate;
        private Long chatRoomId;

        public static DetailResponse createGroupDetailResponse(FoodGroup foodGroup, ChatRoom chatRoom) {
            return DetailResponse.builder()
                    .groupId(foodGroup.getId())
                    .memberId(foodGroup.getMember().getId())
                    .nickname(foodGroup.getMember().getNickname())
                    .image(foodGroup.getMember().getImage())
                    .title(foodGroup.getTitle())
                    .name(foodGroup.getName())
                    .content(foodGroup.getContent())
                    .food(foodGroup.getFood().getType())
                    .date(foodGroup.getGroupDateTime().toLocalDate())
                    .time(foodGroup.getGroupDateTime().toLocalTime())
                    .maximum(foodGroup.getMaximum())
                    .current(foodGroup.getAttendance())
                    .storeName(foodGroup.getStoreName())
                    .storeAddress(foodGroup.getStoreAddress())
                    .latitude(Double.toString(foodGroup.getLocation().getY()))
                    .longitude(Double.toString(foodGroup.getLocation().getX()))
                    .createdDate(foodGroup.getCreatedDate())
                    .chatRoomId(chatRoom.getId())
                    .build();
        }
    }

    @Getter
    public static class AcceptedGroup {
        private List<Long> enrollmentList;

        public AcceptedGroup(List<Long> enrollmentList) {
            this.enrollmentList = enrollmentList;
        }
    }

}
