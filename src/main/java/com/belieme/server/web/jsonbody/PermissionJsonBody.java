package com.belieme.server.web.jsonbody;

public class PermissionJsonBody {
    private DepartmentJsonBody dept;
    private String permission;
    
    public DepartmentJsonBody getDept() {
        return dept;
    }
    
    public String getPermission() {
        return permission;
    }
    
    public void setDepartment(DepartmentJsonBody dept) {
        this.dept = dept;
    }
    
    public void setPermission(String permission) {
        this.permission = permission;
    }
}