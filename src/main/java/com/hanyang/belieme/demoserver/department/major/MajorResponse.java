package com.hanyang.belieme.demoserver.department.major;

public class MajorResponse {
    private int id;
    
    private String code;
    
    public MajorResponse() {
    }
    
    public MajorResponse(MajorResponse oth) {
        this.id = oth.id;
        this.code = oth.code;
    }
    
    public int getId() {
        return id;
    }
    
    public String getCode() {
        return code;
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
    public void setCode(String code) {
        this.code = code;
    }
}