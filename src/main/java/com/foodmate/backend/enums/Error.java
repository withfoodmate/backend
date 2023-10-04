package com.foodmate.backend.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum Error {

    DELETED_USER("이미 삭제된 유저입니다.", HttpStatus.UNAUTHORIZED),
    LOGIN_FAILED("로그인에 실패하였습니다.", HttpStatus.UNAUTHORIZED),
    AUTHORIZATION_NOT_FOUND("권한이 존재하지 않습니다.", HttpStatus.UNAUTHORIZED),
    LOGIN_REQUIRED("로그인이 필요합니다.",HttpStatus.UNAUTHORIZED),
    TOKEN_INVALID("유효하지 않은 토큰 입니다.", HttpStatus.UNAUTHORIZED),
    USER_NOT_FOUND("해당 아이디의 사용자가 없습니다.", HttpStatus.NOT_FOUND);

    private final String message;
    private final HttpStatus httpStatus;

}
