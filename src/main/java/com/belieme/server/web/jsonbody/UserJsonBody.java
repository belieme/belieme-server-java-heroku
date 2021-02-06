package com.belieme.server.web.jsonbody;

import java.util.*;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class UserJsonBody {    
    private UniversityJsonBody university;
    
    private String studentId;

    private String name;
    
    private int entranceYear;
    
    private String token;
    
    private long createTimeStamp;
    
    private long approvalTimeStamp;
    
    private Map<String, String> permissions;
    
    public UserJsonBody() {
        permissions = new HashMap<>();
    }
    
    public UniversityJsonBody getUniversity() {
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

    public String getPermissions() {
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.writeValueAsString(permissions);     
        } catch(JsonProcessingException e) {
            e.printStackTrace();
            return null;
        }
    }
    
    public void setUniversity(UniversityJsonBody university) {
        this.university = university;
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
    
    public void addPermission(String deptCode, String permission) {
        permissions.put(deptCode, permission);
    }
}
