package com.belieme.server.web.exception;

import org.springframework.http.HttpStatus;

public class ConflictException extends HttpException {
    public ConflictException(Exception e) {
        super(e);
        setHttpStatus(HttpStatus.CONFLICT);
    }
    
    public ConflictException(String message) {
        super();
        setHttpStatus(HttpStatus.CONFLICT);
        setMessage(message);
    }
}