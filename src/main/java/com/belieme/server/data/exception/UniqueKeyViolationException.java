package com.belieme.server.data.exception;

public class UniqueKeyViolationException extends ServerDataBaseException {
    private String pinPoint;
    
    public UniqueKeyViolationException(String pinPoint) {
        this.pinPoint = pinPoint;
    }
    
    public String getMessage() {
        return "Violation Of unique key constraint is found in database.";
    }
    
    public String getPinPoint() {
        return pinPoint;
    }
}