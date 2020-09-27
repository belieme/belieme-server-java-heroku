package com.hanyang.belieme.demoserver.user;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hanyang.belieme.demoserver.department.DepartmentNestedToUser;

public class UserResponse {
    private int id;
    
    private List<String> majorCodes;
    
    private List<DepartmentNestedToUser> departments;
    
    private String studentId;

    private String name;
    
    private int entranceYear;
    
    private long createTimeStamp;
    
    private long approvalTimeStamp;
    
    private Map<String, String> permissions;
    
    public UserResponse() {
        permissions = new HashMap<>();
    }
    
    public int getId() {
        return id;
    }
    
    public List<String> getMajorCodes() {
        return new ArrayList<String>(majorCodes);
    }
    
    public List<DepartmentNestedToUser> getDepartments() {
        return new ArrayList<DepartmentNestedToUser>(departments);
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
    
    public boolean permissionsContainsKey(String key) {
        return permissions.containsKey(key);
    }
    
    public boolean hasUserPermission(String deptCode) {
        if(permissions.get(deptCode) == null) {
            return false;
        }
        switch(permissions.get(deptCode)) {
            case "MASTER" :
            case "STAFF" :
            case "USER" :
                return true;
            default :
                return false;
        }
    }
    
    public boolean hasStaffPermission(String deptCode) {
        if(permissions.get(deptCode) == null) {
            return false;
        }
        switch(permissions.get(deptCode)) {
            case "MASTER" :
            case "STAFF" :
                return true;
            default :
                return false;
        }
    }
    
    public boolean hasMasterPermission(String deptCode) {
        if(permissions.get(deptCode) == null) {
            return false;
        }
        switch(permissions.get(deptCode)) {
            case "MASTER" :
                return true;
            default :
                return false;
        }
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
    public void setMajorCodes(List<String> majorCodes) {
        this.majorCodes = new ArrayList<String>(majorCodes);
    }
    
    public void setDepartments(List<DepartmentNestedToUser> departments) {
        this.departments = new ArrayList<DepartmentNestedToUser>(departments);
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
