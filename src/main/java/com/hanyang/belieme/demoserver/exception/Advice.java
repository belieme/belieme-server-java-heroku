package com.hanyang.belieme.demoserver.exception;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@ControllerAdvice  
@RestController 
public class Advice { 
    @ExceptionHandler(NotFoundException.class) 
    public String custom() {
        return "hello custom"; 
    }
    
    @ExceptionHandler(WrongInDataBaseException.class)
    public String custom2() {
        return "hello custom2"; 
    }
}