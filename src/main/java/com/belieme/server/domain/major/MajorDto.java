package com.belieme.server.domain.major;

public class MajorDto {
    private String univCode;
    private String deptCode;
    private String code;
    
    public MajorDto() {
    }
    
    public String getUnivCode() {
        return univCode;
    }
    
    public String getDeptCode() {
        return deptCode;
    }
    
    public String getCode() {
        return code;
    }
    
    public void setUnivCode(String univCode) {
        this.univCode = univCode;
    }
    
    public void setDeptCode(String deptCode) {
        this.deptCode = deptCode;
    }
    
    public void setCode(String code) {
        this.code = code;
    }
}