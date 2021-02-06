package com.belieme.server.domain.department;

public class DepartmentDto {   
    private String univCode;
    
    private String code;

    private String name;
    
    private boolean available;
    
    public DepartmentDto() {
    }

    public String getUnivCode() {
        return univCode;
    }
    
    public String getCode() {
        return code;
    }
    
    public String getName() {
        return name;
    }
    
    public boolean isAvailable() {
        return available;
    }
    
    public void setUnivCode(String univCode) {
        this.univCode = univCode;
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