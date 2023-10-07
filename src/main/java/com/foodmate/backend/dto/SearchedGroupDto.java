package com.foodmate.backend.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.foodmate.backend.entity.FoodGroup;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Getter
public class SearchedGroupDto {

    private Long groupId;
    private Long memberId;
    private String nickname;
    private String image;
    private String title;
    private String name;
    private String food;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate date;
    @JsonFormat(pattern = "HH:mm", timezone = "Asia/Seoul")
    private LocalTime time;
    private int maximum;
    private int current;
    private String storeName;
    private String storeAddress;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm", timezone = "Asia/Seoul")
    private LocalDateTime createdDate;

    public SearchedGroupDto(FoodGroup foodGroup) {
        this.groupId = foodGroup.getId();
        this.memberId = foodGroup.getMember().getId();
        this.nickname = foodGroup.getMember().getNickname();
        this.image = foodGroup.getMember().getImage();
        this.title = foodGroup.getTitle();
        this.name = foodGroup.getName();
        this.food = foodGroup.getFood().getType();
        this.date = foodGroup.getGroupDateTime().toLocalDate();
        this.time = foodGroup.getGroupDateTime().toLocalTime();
        this.maximum = foodGroup.getMaximum();
        this.current = foodGroup.getAttendance();
        this.storeName = foodGroup.getStoreName();
        this.storeAddress = foodGroup.getStoreAddress();
        this.createdDate = foodGroup.getCreatedDate();
    }

}
