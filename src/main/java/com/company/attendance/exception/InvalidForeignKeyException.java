package com.company.attendance.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class InvalidForeignKeyException extends RuntimeException {
    public InvalidForeignKeyException(String message) {
        super(message);
    }
}
