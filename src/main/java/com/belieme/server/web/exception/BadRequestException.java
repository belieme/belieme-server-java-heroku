package com.belieme.server.web.exception;

import org.springframework.http.HttpStatus;

public class BadRequestException extends HttpException {
    public BadRequestException(String message) {
        super();
        setHttpStatus(HttpStatus.BAD_REQUEST);
        setMessage(message);
    }
}