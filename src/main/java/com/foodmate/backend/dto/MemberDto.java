package com.foodmate.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class MemberDto {

    @Getter
    @AllArgsConstructor
    public static class Request{
        private String email;
    }
}
