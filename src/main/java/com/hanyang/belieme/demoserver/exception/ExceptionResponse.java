package com.hanyang.belieme.demoserver.exception;

public class ExceptionResponse {
    private String name;
    private String message;
    public ExceptionResponse(String name, String message) {
        this.name = name;
        this.message = message;
    }

    public String getName() { return name; }
    public String getMessage() { return message; }
}