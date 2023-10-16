package com.foodmate.backend.security.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.foodmate.backend.enums.Error;
import com.foodmate.backend.exception.AuthException;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;


//Filter 에서 올라오는 예외 처리 담당,
//주로 JwtAuthenticationProcessingFilter 예외 처리

@RequiredArgsConstructor
@Slf4j
@Component
public class ExceptionHandlerFilter extends OncePerRequestFilter {

    private final ObjectMapper objectMapper;
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        try {
            filterChain.doFilter(request, response);
        } catch (JwtException  e) {
            handleJwtException(response, e);
        }
    }

    private void handleJwtException(HttpServletResponse response, JwtException e) throws IOException {
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.getWriter().write(objectMapper.writeValueAsString(new AuthException(Error.TOKEN_INVALID)));
        log.error("JwtException 발생", e);
    }
}