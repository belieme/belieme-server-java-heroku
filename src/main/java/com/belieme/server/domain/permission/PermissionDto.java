package com.belieme.server.domain.permission;

public class PermissionDto {
    private String univCode;
    private String studentId;
    private String deptCode;
    private Permissions permission;
    
    public String getUnivCode() {
        return univCode;
    }

    public String getStudentId() {
        return studentId;
    }
    
    public String getDeptCode() {
        return deptCode;
    }
    
    public Permissions getPermission() {
        return permission;
    }
    
    public void setUnivCode(String univCode) {
        this.univCode = univCode;
    }

    public void setStudentId(String studentId){
        this.studentId = studentId;
    }
    
    public void setDeptCode(String deptCode) {
        this.deptCode = deptCode;
    }
    
    public void setPermission(Permissions permission) {
        this.permission = permission;        
    }
}