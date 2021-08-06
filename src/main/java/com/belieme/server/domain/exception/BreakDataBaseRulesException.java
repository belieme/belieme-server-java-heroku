package com.belieme.server.domain.exception;

public class BreakDataBaseRulesException extends ServerDomainException {
    private String pinPoint;
    
    public BreakDataBaseRulesException(String pinPoint) {
        this.pinPoint = pinPoint;
    }
    
    public String getMessage() {
        return "This breakes rules of database.";
    }
    
    public String getPinPoint() {
        return pinPoint;
    }
}