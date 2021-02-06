package com.belieme.server.web.jsonbody;

public class PermissionInfoJsonBody {
    private String deptCode;
    private String permission;
    
    public String getDeptCode() {
        return deptCode;
    }
    
    public String getPermission() {
        return permission;
    }
    
    public void setDeptCode(String deptCode) {
        this.deptCode = deptCode;
    }
    
    public void setPermission(String permission) {
        this.permission = permission;
    }
}