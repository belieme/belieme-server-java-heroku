package com.belieme.server.domain.exception;

public class NotFoundOnDataBaseException extends ServerDomainException {
    private String pinPoint;
    
    public NotFoundOnDataBaseException(String pinPoint) {
        this.pinPoint = pinPoint;
    }
    
    public String getMessage() {
        return "NotFound";
    }
    
    public String getPinPoint() {
        return pinPoint;
    }
}