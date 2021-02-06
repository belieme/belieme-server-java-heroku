package com.belieme.server.web.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class HttpException extends Exception {
    protected HttpStatus httpStatus;
    protected String message;
    
    protected HttpException() {
        
    }
    
    public HttpStatus getHttpStatus() {
        return httpStatus;
    }
    
    public String getMessage() {
        return message;
    }
    
    protected void setHttpStatus(HttpStatus httpStatus) {
        this.httpStatus = httpStatus;
    }
    
    protected void setMessage(String message) {
        this.message = message;
    }
    
    public ResponseEntity<ExceptionResponse> toResponseEntity() {
        return ResponseEntity.status(httpStatus).body(new ExceptionResponse(httpStatus.name(), message));
    } 
}