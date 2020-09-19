package com.hanyang.belieme.demoserver.user.permission;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
public class PermissionDB {
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
    
    public void setPermissionUser() {
        permission = "USER";
    }
    
    public void setPermissionStaff() {
        permission = "STAFF";
    }
    
    public void setPermissionMaster() {
        permission = "MASTER";
    }
}