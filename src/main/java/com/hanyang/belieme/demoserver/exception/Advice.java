package com.hanyang.belieme.demoserver.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;

@ControllerAdvice  
@RestController 
public class Advice { 
    @ExceptionHandler(InternalServerException.class) 
    public ResponseEntity<ExceptionResponse> exceptionResponse(InternalServerException e) {
        return e.toResponseEntity();
    }
}