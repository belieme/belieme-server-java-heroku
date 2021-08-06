package com.belieme.server.domain.exception;

public class NotFoundOnServerException extends ServerDomainException {
    private String pinPoint;
    
    public NotFoundOnServerException(String pinPoint) {
        this.pinPoint = pinPoint;
    }
    
    public String getMessage() {
        return "NotFound";
    }
    
    public String getPinPoint() {
        return pinPoint;
    }
}