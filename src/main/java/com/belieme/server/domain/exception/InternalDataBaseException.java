package com.belieme.server.domain.exception;

public class InternalDataBaseException extends ServerDomainException {
    private String pinPoint;
    
    public InternalDataBaseException(String pinPoint) {
        this.pinPoint = pinPoint;
    }
    
    public String getMessage() {
        return "Internal";
    }
    
    public String getPinPoint() {
        return pinPoint;
    }
}