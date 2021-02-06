package com.belieme.server.domain.university;

public class UniversityDto {
    private String code;
    private String name;
    private String apiUrl;
        
    public UniversityDto() {    
    }
        
    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }
        
    public String getApiUrl() {
        return apiUrl;
    }
    
    public void setCode(String code) {
        this.code = code;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public void setApiUrl(String apiUrl) {
        this.apiUrl = apiUrl;
    }
}