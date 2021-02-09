package com.belieme.server.domain.exception;

import com.belieme.server.web.exception.ExceptionResponse;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public abstract class ServerDomainException extends Exception {
    public abstract String getMessage();
    
    public ResponseEntity<ExceptionResponse> toResponseEntity() {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ExceptionResponse(getMessage(), "?"));
    } 
}