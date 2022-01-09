package com.belieme.server.domain.user;

import java.util.*;

import com.belieme.server.domain.permission.*;

//TODO userApiController에는 permission 추가하기, 가계정 만들기, permission 박탈 / 증여하기 등 만들면 될 듯기...(3)
        
public class UserDto {
    private String univCode;
    
    private String studentId;

    private String name;
    
    private int entranceYear;
    
    private long createTimeStamp;
    
    private long approvalTimeStamp;
    
    private Map<String, Permissions> permissions;
    
    private String token;
    
    public UserDto() {
        permissions = new HashMap<>();
    }
    
    public String getUnivCode() {
        return univCode;
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

    public Map<String, Permissions> getPermissions() {
        return permissions;
    }
    
    public String getToken() {
        return token;
    }
    
    public boolean permissionsContainsKey(String key) {
        return permissions.containsKey(key);
    }

    public boolean hasDeveloperPermission() {
        return permissions.get("DEV") == Permissions.DEVELOPER;
    }

    public boolean hasUserPermission(String deptCode) {
        if(hasDeveloperPermission()) {
            return true;
        }

        if(permissions.get(deptCode) == null) {
            return false;
        }
        switch(permissions.get(deptCode)) {
            case MASTER :
            case STAFF :
            case USER :
                return true;
            case BANNED :
            default :
                return false;
        }
    }
    
    public boolean hasStaffPermission(String deptCode) {
        if(hasDeveloperPermission()) {
            return true;
        }

        if(permissions.get(deptCode) == null) {
            return false;
        }
        switch(permissions.get(deptCode)) {
            case DEVELOPER :
            case MASTER :
            case STAFF :
                return true;
            case USER :
            case BANNED :
            default :
                return false;
        }
    }
    
    public boolean hasMasterPermission(String deptCode) {
        if(hasDeveloperPermission()) {
            return true;
        }
        if(permissions.get(deptCode) == null) {
            return false;
        }
        switch(permissions.get(deptCode)) {
            case DEVELOPER :
            case MASTER :
                return true;
            case STAFF :
            case USER:
            case BANNED :
            default :
                return false;
        }
    }
    
    public long tokenExpiredTime() {
        if(approvalTimeStamp != 0) {
            TimeZone timeZone = TimeZone.getTimeZone("Asia/Seoul");
            Calendar tmp = Calendar.getInstance();
            tmp.setTime(new Date(approvalTimeStamp*1000));
            tmp.setTimeZone(timeZone);
            int year = tmp.get(Calendar.YEAR);
            if(tmp.get(Calendar.MONTH) >= Calendar.MARCH && tmp.get(Calendar.MONTH) < Calendar.SEPTEMBER) {
                tmp.set(year, Calendar.SEPTEMBER, 1, 0, 0, 0);
            } else {
                tmp.set(year+1, Calendar.MARCH, 1, 0, 0, 0);
            }
            return tmp.getTimeInMillis()/1000;
        }
        return 0;
    }
    
    public void setUnivCode(String univCode) {
        this.univCode = univCode;
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
    
    public void setToken(String token) {
        this.token = token;
    }
    
    public void setPermissions(Map<String, Permissions> permissions) {
        this.permissions = permissions;
    }
    
    public void addPermission(String deptCode , Permissions permission) {
        this.permissions.put(deptCode, permission);
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
        this.token = UUID.randomUUID().toString();
    }
    
    public void resetToken() {
        this.token = null;
    }
}
