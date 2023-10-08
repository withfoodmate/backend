package com.foodmate.backend.dto;

import com.foodmate.backend.enums.EnrollmentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Getter
@NoArgsConstructor
@Builder
public class EnrollmentDto {

    @Getter
    @AllArgsConstructor
    @Builder
    public static class myEnrollmentResponse{
        private Long id;
        private Long foodGroupId;
        private String foodGroupMemberImage;
        private String foodGroupTitle;
        private String foodGroupName;
        private String foodGroupFoodType;
        private LocalDateTime foodGroupGroupDateTime;
        private int foodGroupMaximum;
        private String foodGroupStoreName;
        private String foodGroupStoreAddress;

        @Enumerated(EnumType.STRING)
        private EnrollmentStatus status;

    }


    // 생성자 추가

    @Getter
    @Builder
    public static class RequestList {
        private Long enrollmentId;
        private Long groupId;
        private Long memberId;
        private String nickname;
        private String image;
        private String title;
        private String name;
        private String food;
        private LocalDate date;
        private LocalTime time;
        private int maximum;
        private String storeName;
        private String storeAddress;
    }
}


