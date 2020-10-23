package com.hanyang.belieme.demoserver.exception;

import org.springframework.http.HttpStatus;

public class MethodNotAllowedException extends HttpException {
    public MethodNotAllowedException(String message) {
        super();
        setHttpStatus(HttpStatus.METHOD_NOT_ALLOWED);
        setMessage(message);
    }
}