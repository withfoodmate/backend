package com.foodmate.backend.security.config;

import com.foodmate.backend.repository.MemberRepository;
import com.foodmate.backend.security.service.KakaoOAuth2MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityOAuthConfig {

    private final MemberRepository memberRepository;

    @Bean
    public DefaultOAuth2UserService defaultOAuth2UserService() {
        return new DefaultOAuth2UserService();
    }

    @Bean
    KakaoOAuth2MemberService kakaoOAuth2MemberService() {
        return new KakaoOAuth2MemberService(defaultOAuth2UserService(), memberRepository);
    }
}