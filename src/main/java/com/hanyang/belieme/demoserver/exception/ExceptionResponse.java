package com.hanyang.belieme.demoserver.exception;

public class ExceptionResponse {
    private int code; 
    private String name;
    private String desc;
    private String message;
    public ExceptionResponse(int code, String name, String desc, String message) {
        this.code = code;
        this.name = name;
        this.desc = desc;
        this.message = message;
    }

    public int getCode() { return code; } 
    public String getName() { return name; }
    public String getDesc() { return desc; }
    public String getMessage() { return message; }
}