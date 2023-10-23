package com.foodmate.backend.dto;

import com.foodmate.backend.entity.Enrollment;
import com.foodmate.backend.enums.EnrollmentStatus;
import lombok.*;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@Builder
public class EnrollmentDto {

    @Getter
    @Setter
    @AllArgsConstructor
    @Builder
    public static class myEnrollmentResponse {
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

    @Getter
    @Setter
    @AllArgsConstructor
    @Builder
    public static class myReceiveEnrollmentResponse {
        private Long id;
        private Long foodGroupId;
        private Long memberId;
        private String memberNickname;
        private String MemberImage;
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
    @Getter
    @AllArgsConstructor
    @Builder
    public static class myEnrollmentReceiveResponse {
        private Long enrollmentId;
        private Long groupId;
        private Long memberId;
        private String nickname;
        private String image;
        private String title;
        private String name;
        private String food;
        private LocalDateTime foodGroupGroupDateTime;
        private int maximum;
        private String storeName;
        private String storeAddress;

        public static EnrollmentDto.myEnrollmentReceiveResponse createMyEnrollmentReceiveResponse(Enrollment enrollment) {
            return myEnrollmentReceiveResponse.builder()
                    .enrollmentId(enrollment.getId())
                    .groupId(enrollment.getFoodGroup().getId())
                    .memberId(enrollment.getMember().getId())
                    .nickname(enrollment.getMember().getNickname())
                    .image(enrollment.getMember().getImage())
                    .title(enrollment.getFoodGroup().getTitle())
                    .name(enrollment.getFoodGroup().getName())
                    .food(enrollment.getFoodGroup().getFood().getType())
                    .foodGroupGroupDateTime(enrollment.getFoodGroup().getGroupDateTime())
                    .maximum(enrollment.getFoodGroup().getMaximum())
                    .storeName(enrollment.getFoodGroup().getStoreName())
                    .storeAddress(enrollment.getFoodGroup().getStoreAddress())
                    .build();
        }

    }
}


