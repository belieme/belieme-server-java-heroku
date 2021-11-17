package com.belieme.server.web.exception;

import org.springframework.http.HttpStatus;

public class UnauthorizedException extends HttpException {
    public UnauthorizedException(Exception e) {
        super(e);
        setHttpStatus(HttpStatus.UNAUTHORIZED);
    }
    
    public UnauthorizedException(String message) {
        super();
        setHttpStatus(HttpStatus.UNAUTHORIZED);
        setMessage(message);
    }
}