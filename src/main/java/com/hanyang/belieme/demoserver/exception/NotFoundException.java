package com.hanyang.belieme.demoserver.exception;

import org.springframework.http.HttpStatus;

public class NotFoundException extends InternalServerException {
    public NotFoundException(String message) {
        super();
        setHttpStatus(HttpStatus.NOT_FOUND);
        setCode(ErrorCodes.NOT_FOUND_EXCEPTION);
        setName("Not Found");
        setDesc("Can not found it.");
        setMessage(message);
    }
}