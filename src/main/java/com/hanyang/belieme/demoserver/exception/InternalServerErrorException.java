package com.hanyang.belieme.demoserver.exception;

import org.springframework.http.HttpStatus;

public class InternalServerErrorException extends HttpException {
    public InternalServerErrorException(String message) {
        super();
        setHttpStatus(HttpStatus.INTERNAL_SERVER_ERROR);
        setMessage(message);
    }
}