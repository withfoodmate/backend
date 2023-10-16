package com.foodmate.backend.security.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.foodmate.backend.security.filter.ExceptionHandlerFilter;
import com.foodmate.backend.security.filter.JwtAuthenticationProcessingFilter;
import com.foodmate.backend.security.filter.handler.ApiAccessDeniedHandler;
import com.foodmate.backend.security.filter.handler.ApiAuthenticationEntryPoint;
import com.foodmate.backend.security.filter.handler.OAuth2LoginFailureHandler;
import com.foodmate.backend.security.filter.handler.OAuth2LoginSuccessHandler;
import com.foodmate.backend.security.service.KakaoOAuth2MemberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import javax.servlet.http.HttpServletResponse;


@Configuration
@EnableWebSecurity(debug = true)
@RequiredArgsConstructor
@Slf4j
public class SecurityConfig {

    private final KakaoOAuth2MemberService kakaoOAuth2MemberService;
    private final ObjectMapper objectMapper;
    private final OAuth2LoginSuccessHandler oAuth2LoginSuccessHandler;
    private final OAuth2LoginFailureHandler oAuth2LoginFailureHandler;
    private final JwtAuthenticationProcessingFilter jwtAuthenticationProcessingFilter;
    private final ExceptionHandlerFilter exceptionHandlerFilter;
    private final ApiAuthenticationEntryPoint apiAuthenticationEntryPoint;
    private final ApiAccessDeniedHandler apiAccessDeniedHandler;


    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
                .csrf().disable()
                .formLogin().disable()
                .httpBasic().disable() // httpBasic 사용 X
                .headers().frameOptions().disable()
                .and()
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);
        http
                .authorizeRequests()
                .mvcMatchers("/test", "/test2").authenticated()
                .anyRequest().permitAll();
        http
                .oauth2Login()
                .userInfoEndpoint().userService(kakaoOAuth2MemberService)
                .and()
                .successHandler(oAuth2LoginSuccessHandler)
                .failureHandler(oAuth2LoginFailureHandler);

        // 필터 순서를 설정하여 정상작동 및 Filter에서 예외처리 진행
        http
                .addFilterBefore(jwtAuthenticationProcessingFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(exceptionHandlerFilter, JwtAuthenticationProcessingFilter.class)
                .exceptionHandling()
                .authenticationEntryPoint(apiAuthenticationEntryPoint) //AuthenticationException
                .accessDeniedHandler(apiAccessDeniedHandler);     //AccessDeniedException

        http
                .logout()
                .logoutUrl("/member/logout")
                .logoutSuccessHandler((request, response, authentication) -> {
                    // 로그아웃 성공 후에 추가 작업을 수행하고 리디렉션을 막음
                    response.setStatus(HttpServletResponse.SC_OK);
                })
                .invalidateHttpSession(true) // http 세션 무효와
                .deleteCookies("JSESSIONID"); // 쿠키 삭제
        return http.build();
    }

    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return (web) -> web.ignoring()
                .requestMatchers(PathRequest.toStaticResources().atCommonLocations());
    }

}