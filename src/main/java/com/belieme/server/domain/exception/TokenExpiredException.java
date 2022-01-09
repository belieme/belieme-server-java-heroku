package com.belieme.server.domain.exception;

public class TokenExpiredException extends ServerDomainException {

    private String pinPoint;

    public TokenExpiredException(String pinPoint) {
        this.pinPoint = pinPoint;
    }

    public String getMessage() {
        return "This token has expired.";
    }

    public String getPinPoint() {
        return pinPoint;
    }
}
