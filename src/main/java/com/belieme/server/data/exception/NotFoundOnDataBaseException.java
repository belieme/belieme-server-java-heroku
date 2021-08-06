package com.belieme.server.data.exception;

public class NotFoundOnDataBaseException extends ServerDataBaseException {
    private String pinPoint;
    
    public NotFoundOnDataBaseException(String pinPoint) {
        this.pinPoint = pinPoint;
    }
    
    public String getMessage() {
        return "Can Not Found On DataBase.";
    }
    
    public String getPinPoint() {
        return pinPoint;
    }
}