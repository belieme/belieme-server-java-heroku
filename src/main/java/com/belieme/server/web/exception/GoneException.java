package com.belieme.server.web.exception;

import org.springframework.http.HttpStatus;

public class GoneException extends HttpException {
    public GoneException(Exception e) {
        super(e);
        setHttpStatus(HttpStatus.GONE);
    }
    
    public GoneException(String message) {
        super();
        setHttpStatus(HttpStatus.GONE);
        setMessage(message);
    }
}