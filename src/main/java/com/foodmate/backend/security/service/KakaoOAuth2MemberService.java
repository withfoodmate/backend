package com.foodmate.backend.security.service;


import com.foodmate.backend.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@RequiredArgsConstructor
@Slf4j
public class KakaoOAuth2MemberService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private final DefaultOAuth2UserService userService;

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {

        //기존 DefaultOAuth2UserService 로직
        OAuth2User oAuth2User = userService.loadUser(userRequest);

        //로그인 한 이메일 정보 가져오기
        Map<String, Object> memberInfo = oAuth2User.getAttribute("kakao_account");
        String email = (String) memberInfo.get("email");
        log.info(email);

        return oAuth2User;
    }


}