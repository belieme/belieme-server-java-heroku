package com.belieme.server.web.exception;

import org.springframework.http.HttpStatus;

public class ForbiddenException extends HttpException {
    public ForbiddenException(String message) {
        super();
        setHttpStatus(HttpStatus.FORBIDDEN);
        setMessage(message);
    }
}