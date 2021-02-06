package com.belieme.server.data.permission;

import javax.persistence.*;

@Entity
public class PermissionEntity {
    @Id @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;
    private int userId;
    private int deptId;
    private String permission;
    
    public int getId() {
        return id;
    }
    
    public int getUserId() {
        return userId;
    }
    
    public int getDeptId() {
        return deptId;
    }
    
    public String getPermission() {
        return permission;
    }
    
    public void setUserId(int userId) {
        this.userId = userId;
    }
    
    public void setDeptId(int deptId) {
        this.deptId = deptId;
    }
    
    public void setPermission(String permission) {
        this.permission = permission;
    }
}