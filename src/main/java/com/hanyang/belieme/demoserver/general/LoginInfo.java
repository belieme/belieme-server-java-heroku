package com.hanyang.belieme.demoserver.general;

public class LoginInfo {
    private String univCode;
    private String apiToken;
        
    public LoginInfo() {}
    
    public String getUnivCode() {
        return univCode;
    }
        
    public String getApiToken() {
        return apiToken;
    }
        
    public void setUnivCode(String univCode) {
        this.univCode = univCode;
    }
        
    public void setApiToken(String apiToken) {
        this.apiToken = apiToken;
    }
}