package com.foodmate.backend.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum Error {

    // MemberException
    DELETED_USER("이미 삭제된 유저입니다.", HttpStatus.UNAUTHORIZED),
    LOGIN_FAILED("로그인에 실패하였습니다.", HttpStatus.UNAUTHORIZED),
    AUTHORIZATION_NOT_FOUND("권한이 존재하지 않습니다.", HttpStatus.UNAUTHORIZED),
    LOGIN_REQUIRED("로그인이 필요합니다.",HttpStatus.UNAUTHORIZED),
    TOKEN_INVALID("유효하지 않은 토큰 입니다.", HttpStatus.UNAUTHORIZED),
    USER_NOT_FOUND("해당 아이디의 사용자가 없습니다.", HttpStatus.NOT_FOUND),

    // FoodException
    FOOD_NOT_FOUND("입력한 음식은 DB에 존재하지 않습니다.", HttpStatus.BAD_REQUEST),

    // GroupException
    OUT_OF_DATE_RANGE("현재 시간으로부터 한 시간 이후 ~ 한 달 이내의 모임만 생성 가능합니다.", HttpStatus.BAD_REQUEST);

    private final String message;
    private final HttpStatus httpStatus;

}
