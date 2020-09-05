package com.hanyang.belieme.demoserver.user;

import com.hanyang.belieme.demoserver.department.Department;

public class User {
    private int id;
    
    private String token;
    
    private long issuedAt;
    
    private String studentId;

    private String name;
    
    private String permission;
    
    public int getId() {
        return id;
    }
    
    public String getToken() {
        return token;
    }
    
    public long getIssuedAt() {
        return issuedAt;
    }
    
    public String getStudentId() {
        return studentId;
    }

    public String getName() {
        return name;
    }

    public String getPermission() {
        return permission;
    }

    public void setName(String name) {
        this.name = name;
    }
    
    public void setToken(String token) {
        this.token = token;
    }
    
    public void setIssuedAt(long issuedAt) {
        this.issuedAt = issuedAt;
    }
    
    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    public void permissionSetUser() {
        permission = "USER";
    }
    
    public void permissionSetAdmin() {
        permission = "ADMIN";
    }

    public void permissionSetMaster() {
        permission = "MASTER";
    }

    public void permissionSetDeveloper() {
        permission = "DEVELOPER";
    }
}
