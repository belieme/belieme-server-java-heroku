package com.hanyang.belieme.demoserver.user;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
public class UserDB {
    @Id @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;
    
    private String token;
    
    private long createTimeStamp;
    
    private long approvalTimeStamp;
    
    @Column(name = "student_id", nullable = false)
    private String studentId;

    private String name;
    
    private int entranceYear;
    
    private String permission;
    
    public int getId() {
        return id;
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

    public int getEntranceYear() {
        return entranceYear;
    }
    
    public String getPermission() {
        return permission;
    }
    
    public void setCreateTimeStampNow() {
        createTimeStamp = System.currentTimeMillis()/1000;
    }
    
    public void setApprovalTimeStampNow() {
        approvalTimeStamp = System.currentTimeMillis()/1000;
    }
    
    public void setApprovalTimeStampZero() {
        approvalTimeStamp = 0;
    }
    
    public void setNewToken() {
        this.token = "A123456";
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
