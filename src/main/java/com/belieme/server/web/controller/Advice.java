package com.belieme.server.web.controller;

import com.belieme.server.domain.exception.*;
import com.belieme.server.web.exception.*;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@ControllerAdvice  
@RestController 
public class Advice { 
    //TODO 최종적으로는 다음과 같은 ExceptionJsonBody를 response로 하기
    //{
    //    "timestamp": "2021-03-24T14:18:39.140+0000",
    //    "status": 405,
    //    "error": "Method Not Allowed",
    //    "message": "Request method 'GET' not supported",
    //    "path": "/univs/HYU/users/2018008886/permissions"
    //}
    @ExceptionHandler(HttpException.class) 
    public ResponseEntity<ExceptionResponse> exceptionResponse(HttpException e) {
        return e.toResponseEntity();
    }
    
    @ExceptionHandler(ServerDomainException.class) // TODO 이거 없애고 ServerDomainException들은 ApiController에서 HTTPException으로 변환하기
    public ResponseEntity<ExceptionResponse> exceptionResponse2(ServerDomainException e) {
        return e.toResponseEntity();
    }
}