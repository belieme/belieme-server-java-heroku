package com.belieme.server.web.jsonbody;

public class MajorJsonBody {
    private String code;
    private DepartmentJsonBody dept;
    
    public String getCode() {
         return code;
    }
    
    public DepartmentJsonBody getDept() {
         return dept;
    }
    
    public void setCode(String code) {
        this.code = code;
    }
    
    public void setDept(DepartmentJsonBody dept) {
        this.dept = dept;
    }
}