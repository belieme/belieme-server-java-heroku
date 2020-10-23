package com.hanyang.belieme.demoserver.exception;

import org.springframework.http.HttpStatus;

public class UnauthorizedException extends HttpException {
    public UnauthorizedException(String message) {
        super();
        setHttpStatus(HttpStatus.UNAUTHORIZED);
        setMessage(message);
    }
}