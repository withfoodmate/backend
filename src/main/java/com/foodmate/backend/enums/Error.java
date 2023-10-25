package com.foodmate.backend.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum Error {

    // MemberException
    DELETED_USER("이미 삭제된 유저입니다.", HttpStatus.UNAUTHORIZED),
    LOGIN_FAILED("비밀번호를 확인하세요!", HttpStatus.UNAUTHORIZED),
    AUTHORIZATION_NOT_FOUND("권한이 존재하지 않습니다.", HttpStatus.UNAUTHORIZED),
    LOGIN_REQUIRED("로그인이 필요합니다.", HttpStatus.UNAUTHORIZED),
    TOKEN_INVALID("유효하지 않은 토큰 입니다.", HttpStatus.UNAUTHORIZED),
    USER_NOT_FOUND("해당 사용자가 없습니다.", HttpStatus.NOT_FOUND),
    PASSWORD_NOT_MATCH("패스워드가 일치하지 않습니다.", HttpStatus.UNAUTHORIZED),
    ACCESS_DENIED("해당 요청에 권한이 없습니다.", HttpStatus.FORBIDDEN),
    EMAIL_AUTH_FAILED("이메일 인증이 되지 않은 상태입니다.", HttpStatus.UNAUTHORIZED),
    USER_NOT_GENERAL("일반 로그인 사용자가 아닙니다.", HttpStatus.FORBIDDEN),
    USER_NOT_KAKAO("카카오 로그인 사용자가 아닙니다.", HttpStatus.FORBIDDEN),

    // FoodException
    FOOD_NOT_FOUND("입력한 음식은 DB에 존재하지 않습니다.", HttpStatus.BAD_REQUEST),

    // GroupException
    OUT_OF_DATE_RANGE("현재 시간으로부터 한 시간 이후 ~ 한 달 이내의 모임만 생성 가능합니다.", HttpStatus.BAD_REQUEST),
    GROUP_NOT_FOUND("해당 아이디의 모임은 존재하지 않습니다.", HttpStatus.NOT_FOUND),
    GROUP_DELETED("해당 모임은 삭제되었습니다.", HttpStatus.GONE),
    NO_MODIFY_PERMISSION_GROUP("해당 모임을 수정할 권한이 없습니다.", HttpStatus.FORBIDDEN),
    NO_DELETE_PERMISSION_GROUP("해당 모임을 삭제할 권한이 없습니다.", HttpStatus.FORBIDDEN),
    INVALID_DATE_RANGE("검색 시작일 혹은 종료일 정보가 올바르지 않습니다.", HttpStatus.BAD_REQUEST),

    // ChatException
    CHATROOM_NOT_FOUND("채팅방이 존재하지 않습니다.", HttpStatus.NOT_FOUND),
    USER_NOT_IN_CHATROOM("채팅에 해당 사용자가 없습니다", HttpStatus.NOT_FOUND),

    // EnrollmentException
    ENROLLMENT_NOT_FOUND("해당 신청이 존재하지 않습니다.", HttpStatus.NOT_FOUND),
    CANNOT_APPLY_TO_OWN_GROUP("본인이 생성한 모임에는 신청이 불가합니다.", HttpStatus.BAD_REQUEST),
    ENROLLMENT_HISTORY_EXISTS("해당 모임에 신청 이력이 존재합니다.", HttpStatus.BAD_REQUEST),
    GROUP_FULL("해당 모임의 정원이 다 찼습니다.", HttpStatus.BAD_REQUEST),
    REQUEST_NOT_FOUND("입력한 요청이 존재하지 않습니다.", HttpStatus.NOT_FOUND),
    ENROLLMENT_CANCEL_NOT_STATUS("해당 신청은 취소가능한 상태가 아닙니다.", HttpStatus.ACCEPTED),

    // CommentException
    COMMENT_NOT_FOUND("해당 아이디의 댓글은 존재하지 않습니다.", HttpStatus.NOT_FOUND),
    INVALID_ADDRESS("올바르지 않은 주소입니다.", HttpStatus.BAD_REQUEST),
    NO_MODIFY_PERMISSION_COMMENT("해당 댓글을 수정할 권한이 없습니다.", HttpStatus.FORBIDDEN),
    NO_DELETE_PERMISSION_COMMENT("해당 댓글을 삭제할 권한이 없습니다.", HttpStatus.FORBIDDEN),

    // ReplyException
    REPLY_NOT_FOUND("해당 아이디의 대댓글은 존재하지 않습니다.", HttpStatus.NOT_FOUND),
    NO_MODIFY_PERMISSION_REPLY("해당 대댓글을 수정할 권한이 없습니다.", HttpStatus.FORBIDDEN),
    NO_DELETE_PERMISSION_REPLY("해당 대댓글을 삭제할 권한이 없습니다.", HttpStatus.FORBIDDEN),

    // FileException
    S3_SERVICE_ERROR("S3 서비스 에러가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
    S3_CLIENT_ERROR("S3 클라이언트 에러가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
    DELETE_IMAGE_FILE_FAILED("이미지를 삭제하는데 실패했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
    UPLOAD_IMAGE_FILE_FAILED("이미지를 업로드하는데 실패했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),

    // WebSocketException
    MISSING_SESSION_ATTRIBUTE("세션 속성이 없습니다. 연결에 필요한 정보가 없습니다.", HttpStatus.BAD_REQUEST),
    MISSING_DESTINATION("웹소켓 요청에 목적지(destination) 정보가 비어 있습니다.", HttpStatus.BAD_REQUEST);

    private final String message;
    private final HttpStatus httpStatus;

}
