package com.hanyang.belieme.demoserver.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class InternalServerException extends Exception {
    
    protected HttpStatus httpStatus;
    protected int code;
    protected String name;
    protected String desc;
    protected String message;
    
    protected InternalServerException() {
        
    }
    
    public HttpStatus getHttpStatus() {
        return httpStatus;
    }
    
    public int getCode() {
        return code;
    }
    
    public String getName() {
        return name;        
    }
    
    public String getDesc() {
        return desc;
    }
    
    public String getMessage() {
        return message;
    }
    
    protected void setHttpStatus(HttpStatus httpStatus) {
        this.httpStatus = httpStatus;
    }
    
    protected void setCode(int code) {
        this.code = code;
    }
    
    protected void setName(String name) {
        this.name = name;
    }
    
    protected void setDesc(String desc) {
        this.desc = desc;
    }
    
    protected void setMessage(String message) {
        this.message = message;
    }
    
    public ResponseEntity<ExceptionResponse> toResponseEntity() {
        return ResponseEntity.status(httpStatus).body(new ExceptionResponse(code, name, desc, message));
    } 
}