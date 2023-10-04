package com.foodmate.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

public class ReplyDto {

    @Getter
    @NotNull
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Request {
        private String content;
    }

}
