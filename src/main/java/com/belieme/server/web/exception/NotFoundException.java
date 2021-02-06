package com.belieme.server.web.exception;

import org.springframework.http.HttpStatus;

public class NotFoundException extends HttpException {
    public NotFoundException(String message) {
        super();
        setHttpStatus(HttpStatus.NOT_FOUND);
        setMessage(message);
    }
}