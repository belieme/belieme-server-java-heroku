package com.hanyang.belieme.demoserver.user;

import com.hanyang.belieme.demoserver.university.University;

public class User {
    private int id;
    
    private University university;
    
    private String token;
    
    private long createTimeStamp;
    
    private long approvalTimeStamp;
    
    private String studentId;

    private String name;
    
    private String permission;
    
    public int getId() {
        return id;
    }
    
    public University getUniversity() {
        return university;
    }
    
    public String getToken() {
        return token;
    }
    
    public long getCreateTimeStamp() {
        return createTimeStamp;
    }
    
    public long getApprovalTimeStamp() {
        return approvalTimeStamp;
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
    
    public void setCreateTimeStamp(long createTimeStamp) {
        this.createTimeStamp = createTimeStamp;
    }
    
    public void setApprovalTimeStamp(long approvalTimeStamp) {
        this.approvalTimeStamp = approvalTimeStamp;
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
