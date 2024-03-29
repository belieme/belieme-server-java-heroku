package com.belieme.server.data.user;

import javax.persistence.*;

@Entity
public class UserEntity {
    @Id @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;
    
    private int univId;
    
    private String token;
    
    private long createTimeStamp;
    
    private long approvalTimeStamp;

    private String studentId;

    private String name;
    
    private int entranceYear;
    
    public UserEntity() {
    }
    
    public int getId() {
        return id;
    }
    
    public int getUnivId() {
        return univId;
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
    
    public void setCreateTimeStamp(long createTimeStamp) {
        this.createTimeStamp = createTimeStamp;
    }
    
    public void setApprovalTimeStamp(long approvalTimeStamp) {
        this.approvalTimeStamp = approvalTimeStamp;
    }
    
    public void setToken(String token) {
        this.token = token;
    }
    
    public void setUnivId(int univId) {
        this.univId = univId;
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
}
