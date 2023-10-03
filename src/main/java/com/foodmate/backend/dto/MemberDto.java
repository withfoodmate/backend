package com.foodmate.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
public class MemberDto {

    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class request{
        private String email;
    }
}
