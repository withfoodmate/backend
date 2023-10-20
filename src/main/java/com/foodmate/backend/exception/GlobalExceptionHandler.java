package com.foodmate.backend.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MemberException.class)
    public ResponseEntity<String> handleMemberException(MemberException e) {
        log.error("MemberException", e);
        return ResponseEntity.status(e.getHttpStatus()).body(e.getMessage());
    }

    @ExceptionHandler(FoodException.class)
    public ResponseEntity<String> handleFoodException(FoodException e) {
        log.error("FoodException", e);
        return ResponseEntity.status(e.getHttpStatus()).body(e.getMessage());
    }

    @ExceptionHandler(GroupException.class)
    public ResponseEntity<String> handleGroupException(GroupException e) {
        log.error("GroupException", e);
        return ResponseEntity.status(e.getHttpStatus()).body(e.getMessage());
    }

    @ExceptionHandler(ChatException.class)
    public ResponseEntity<String> handleChatException(ChatException e) {
        log.error("ChatException", e);
        return ResponseEntity.status(e.getHttpStatus()).body(e.getMessage());
    }

    @ExceptionHandler(EnrollmentException.class)
    public ResponseEntity<String> handleEnrollmentException(EnrollmentException e) {
        log.error("EnrollmentException", e);
        return ResponseEntity.status(e.getHttpStatus()).body(e.getMessage());
    }

    @ExceptionHandler(CommentException.class)
    public ResponseEntity<String> handleCommentException(CommentException e) {
        log.error("CommentException", e);
        return ResponseEntity.status(e.getHttpStatus()).body(e.getMessage());
    }

    @ExceptionHandler(ReplyException.class)
    public ResponseEntity<String> handleReplyException(ReplyException e) {
        log.error("ReplyException", e);
        return ResponseEntity.status(e.getHttpStatus()).body(e.getMessage());
    }

    @ExceptionHandler(WebSocketException.class)
    public ResponseEntity<String> handleWebSocketException(WebSocketException e) {
        log.error("WebSocketException", e);
        return ResponseEntity.status(e.getHttpStatus()).body(e.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<String> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        log.error("MethodArgumentNotValidException", e);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
    }

    @ExceptionHandler(AuthException.class)
    public ResponseEntity<String> handleAuthException(AuthException e) {
        log.error("AuthException", e);
        return ResponseEntity.status(e.getHttpStatus()).body(e.getMessage());
    }

    @ExceptionHandler(FileException.class)
    public ResponseEntity<String> handleFileException(FileException e) {
        log.error("FileException", e);
        return ResponseEntity.status(e.getHttpStatus()).body(e.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleException(Exception e) {
        log.error("Exception", e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
    }

}
