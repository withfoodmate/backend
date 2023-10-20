package com.foodmate.backend.security.filter.handler;


import com.foodmate.backend.entity.Member;
import com.foodmate.backend.enums.Error;
import com.foodmate.backend.exception.MemberException;
import com.foodmate.backend.repository.MemberRepository;
import com.foodmate.backend.security.service.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
@Component
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {

    private final MemberRepository memberRepository;
    private final JwtTokenProvider jwtTokenProvider;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {

        OAuth2User memberInfo = (OAuth2User) authentication.getPrincipal();
        String email = (String) ((Map) memberInfo.getAttribute("kakao_account")).get("email");
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new MemberException(Error.USER_NOT_FOUND));
        String accessToken = jwtTokenProvider.createAccessToken(member.getId());
        String refreshToken = jwtTokenProvider.createRefreshToken();

        loginSuccess(response, accessToken, refreshToken, member);
        String jsonResponse = "{\"accessToken\":\"" + accessToken + "\", \"refreshToken\":\"" + refreshToken + "\"}";

        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setStatus(HttpServletResponse.SC_OK);
        response.getWriter().write(jsonResponse);

    }

    private void loginSuccess(HttpServletResponse response, String accessToken, String refreshToken, Member member){
        jwtTokenProvider.sendAccessAndRefreshToken(response, accessToken, refreshToken);
        jwtTokenProvider.updateRefreshToken(member.getEmail(), refreshToken);
    }

}