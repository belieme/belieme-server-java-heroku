package com.belieme.server.web;

import com.belieme.server.domain.exception.*;
import com.belieme.server.web.exception.*;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@ControllerAdvice  
@RestController 
public class Advice { 
    @ExceptionHandler(HttpException.class) 
    public ResponseEntity<ExceptionResponse> exceptionResponse(HttpException e) {
        return e.toResponseEntity();
    }
    
    @ExceptionHandler(ServerDomainException.class) 
    public ResponseEntity<ExceptionResponse> exceptionResponse2(ServerDomainException e) {
        return e.toResponseEntity();
    }
}