package com.belieme.server.domain.exception;

public class CodeDuplicationException extends ServerDomainException {
    public String getMessage() {
        return "Code";
    }
}