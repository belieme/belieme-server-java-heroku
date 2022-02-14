package com.belieme.server.web.jsonbody;

import java.util.*;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class UserJsonBodyWithoutToken {
    private String studentId;

    private String name;
    
    private int entranceYear;
    
    private long createTimeStamp;
    
    private long approvalTimeStamp;
    
    private Map<String, String> permissions;
    
    public UserJsonBodyWithoutToken() {
        permissions = new HashMap<>();
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
    
    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    public void setName(String name) {
        this.name = name;
    }
    
    public void setEntranceYear(int entranceYear) {
        this.entranceYear = entranceYear;
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
