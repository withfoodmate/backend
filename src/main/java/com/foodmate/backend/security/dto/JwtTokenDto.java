package com.foodmate.backend.security.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
public class JwtTokenDto {

    private final String accessToken;
    private final String refreshToken;

    @Builder
    public JwtTokenDto(String accessToken, String refreshToken) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
    }
}