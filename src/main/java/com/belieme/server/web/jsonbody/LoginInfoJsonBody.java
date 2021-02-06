package com.belieme.server.web.jsonbody;

public class LoginInfoJsonBody {
    private String univCode;
    private String apiToken;
        
    public LoginInfoJsonBody() {}
    
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