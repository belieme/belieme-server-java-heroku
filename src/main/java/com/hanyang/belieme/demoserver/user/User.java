package com.hanyang.belieme.demoserver.user;

import com.hanyang.belieme.demoserver.university.University;

public class User {
    private int id;
    
    private University university;
    
    private String studentId;

    private String name;
    
    private int entranceYear;
    
    private String token;
    
    private long createTimeStamp;
    
    private long approvalTimeStamp;
    
    private String permission;
    
    public int getId() {
        return id;
    }
    
    public University getUniversity() {
        return university;
    }
    
    public String getStudentId() {
        return studentId;
    }

    public String getName() {
        return name;
    }
    
    public int getEntranceYear() {
        return entranceYear;
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

    public String getPermission() {
        return permission;
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
    public void setUniversity(University university) {
        this.university = new University(university);
    }
    
    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    public void setName(String name) {
        this.name = name;
    }
    
    public void setEntranceYear(int entranceYear) {
        this.entranceYear = entranceYear;
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
    
    public void setPermission(String permission) {
        this.permission = permission;
    }
}
