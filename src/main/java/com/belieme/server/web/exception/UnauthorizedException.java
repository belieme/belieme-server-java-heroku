package com.belieme.server.web.exception;

import org.springframework.http.HttpStatus;

public class UnauthorizedException extends HttpException {
    public UnauthorizedException(String message) {
        super();
        setHttpStatus(HttpStatus.UNAUTHORIZED);
        setMessage(message);
    }
}