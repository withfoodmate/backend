package com.foodmate.backend.exception;

import com.foodmate.backend.enums.Error;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class ReplyException extends RuntimeException {

    private final Error error;
    private final String message;
    private final HttpStatus httpStatus;

    public ReplyException(Error error) {
        this.error = error;
        this.message = error.getMessage();
        this.httpStatus = error.getHttpStatus();
    }

}
