package com.foodmate.backend.exception;

import com.foodmate.backend.enums.Error;
import lombok.Getter;
import org.springframework.http.HttpStatus;

import java.io.IOException;

@Getter
public class FileException extends RuntimeException {

    private final Error error;
    private final String message;
    private final HttpStatus httpStatus;

    public FileException(Error error) {
        this.error = error;
        this.message = error.getMessage();
        this.httpStatus = error.getHttpStatus();
    }

}
