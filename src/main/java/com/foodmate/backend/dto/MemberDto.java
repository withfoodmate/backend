package com.foodmate.backend.dto;

import lombok.*;

@Builder
@Getter
@Setter
@AllArgsConstructor
public class MemberDto {

    @Getter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Request{
        private String email;
    }
}
