package com.hanyang.belieme.demoserver.exception;

import org.springframework.http.HttpStatus;

public class GateWayTimeOutException extends HttpException {
    public GateWayTimeOutException(String message) {
        super();
        setHttpStatus(HttpStatus.GATEWAY_TIMEOUT);
        setMessage(message);
    }
}