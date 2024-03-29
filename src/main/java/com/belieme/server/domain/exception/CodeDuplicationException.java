package com.belieme.server.domain.exception;

public class CodeDuplicationException extends ServerDomainException { //DuplicateKeyException
    private String pinPoint;
    
    public CodeDuplicationException(String pinPoint) {
        this.pinPoint = pinPoint;
    }
    
    public String getMessage() {
        return "This code is already on database.";
    }
    
    public String getPinPoint() {
        return pinPoint;
    }
}