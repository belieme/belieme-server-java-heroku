package com.belieme.server.web.exception;

import org.springframework.http.HttpStatus;

public class GateWayTimeOutException extends HttpException {
    public GateWayTimeOutException(String message) {
        super();
        setHttpStatus(HttpStatus.GATEWAY_TIMEOUT);
        setMessage(message);
    }
}