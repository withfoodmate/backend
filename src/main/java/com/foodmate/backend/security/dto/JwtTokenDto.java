package com.foodmate.backend.security.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JwtTokenDto {

    private String accessToken;
    private String refreshToken;

    public static JwtTokenDto createJwtToken(String accessToken, String refreshToken){
        return JwtTokenDto.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();

    }
}