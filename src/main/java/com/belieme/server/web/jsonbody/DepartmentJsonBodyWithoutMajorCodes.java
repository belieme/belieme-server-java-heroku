package com.belieme.server.web.jsonbody;

public class DepartmentJsonBodyWithoutMajorCodes {
    private int id;
    
    private String code;

    private String name;
    
    private boolean available;
    
    public DepartmentJsonBodyWithoutMajorCodes() {
    }
    
    public int getId() {
        return id;
    }
    
    public String getCode() {
        return code;
    }
    
    public String getName() {
        return name;
    }
    
    public boolean getAvailble() {
        return available;
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
    
    public void setCode(String code) {
        this.code = code;
    }
    
    public void setName(String name) {
        this.name = name;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }
}
