package com.hanyang.belieme.demoserver.exception;

import org.springframework.http.HttpStatus;

public class ForbiddenException extends HttpException {
    public ForbiddenException(String message) {
        super();
        setHttpStatus(HttpStatus.FORBIDDEN);
        setMessage(message);
    }
}