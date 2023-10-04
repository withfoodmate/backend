package com.foodmate.backend.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Getter;

import javax.validation.constraints.Max;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalTime;

public class GroupDto {

    @Getter
    @NotNull
    @AllArgsConstructor
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

}
