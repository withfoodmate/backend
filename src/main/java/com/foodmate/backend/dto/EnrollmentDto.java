package com.foodmate.backend.dto;

import com.foodmate.backend.enums.EnrollmentStatus;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@Builder
public class EnrollmentDto {

    private Long enrollmentId;
    private Long foodGroupId;
    private String image;
    private String title;
    private String name;
    private String type;
    private LocalDateTime groupDateTime;
    private int maximum;
    private String storeName;
    private String storeAddress;

    @Enumerated(EnumType.STRING)
    private EnrollmentStatus status;

    // 생성자 추가
    public EnrollmentDto(Long enrollmentId, Long foodGroupId, String image, String title, String name, String type, LocalDateTime groupDateTime,int maximum, String storeName, String storeAddress, EnrollmentStatus status) {
        this.enrollmentId = enrollmentId;
        this.foodGroupId = foodGroupId;
        this.image = image;
        this.title = title;
        this.name = name;
        this.type = type;
        this.groupDateTime = groupDateTime;
        this.maximum = maximum;
        this.storeName = storeName;
        this.storeAddress = storeAddress;
        this.status = status;
    }
}


