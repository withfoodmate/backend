package com.foodmate.backend.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
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

    // fg.id, fg.title, fg.name, fg.groupDateTime, fg.maximum, fg.storeName, fg.storeAddress, fg.createdDate,
    // m.id, m.nickname, m.image, f.type, COUNT(e.id)
    public SearchedGroupDto(Long groupId, String title, String name, LocalDateTime dateTime,
                            int maximum, String storeName, String storeAddress, LocalDateTime createdDate,
                            Long memberId, String nickname, String image, String food, Long current) {
        this.groupId = groupId;
        this.memberId = memberId;
        this.nickname = nickname;
        this.image = image;
        this.title = title;
        this.name = name;
        this.food = food;
        this.date = dateTime.toLocalDate();
        this.time = dateTime.toLocalTime();
        this.maximum = maximum;
        this.current = current.intValue() + 1;   // 현재 인원은 모임 생성자 포함이니까 +1 해줘야
        this.storeName = storeName;
        this.storeAddress = storeAddress;
        this.createdDate = createdDate;
    }

}
