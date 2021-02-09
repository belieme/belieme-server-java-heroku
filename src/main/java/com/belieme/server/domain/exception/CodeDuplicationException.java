package com.belieme.server.domain.exception;

public class CodeDuplicationException extends ServerDomainException {
    private String pinPoint;
    
    public CodeDuplicationException(String pinPoint) {
        this.pinPoint = pinPoint;
    }
    
    public String getMessage() {
        return "Code";
    }
    
    public String getPinPoint() {
        return pinPoint;
    }
}