package com.belieme.server.web.jsonbody;

public class MajorInfoJsonBody {
    private String deptCode;
    private String majorCode;
    
    public String getDeptCode() {
        return deptCode;
    }
    
    public String getMajorCode() {
        return majorCode;
    }
    
    public void setDeptCode(String deptCode) {
        this.deptCode = deptCode;
    }
    
    public void setMajorCode(String majorCode) {
        this.majorCode = majorCode;
    }
}