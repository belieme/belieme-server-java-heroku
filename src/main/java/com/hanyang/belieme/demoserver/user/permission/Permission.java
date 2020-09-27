package com.hanyang.belieme.demoserver.user.permission;

import com.hanyang.belieme.demoserver.department.Department;
import com.hanyang.belieme.demoserver.user.User;

public class Permission { //TODO 어떠한 형식의 POST PERMISSION의 output으로 할 것인가...
    private int id;
    private User user;
    private Department dept;
    private String permission;
    
    public int getId() {
        return id;
    }
    
    public User getUser() {
        return user;
    }
    
    public Department getDept() {
        return dept;
    }
    
    public String getPermission() {
        return permission;
    }
    
    public void setUser(User user) {
        this.user = user;
    }
    
    public void setDepartment(Department dept) {
        this.dept = dept;
    }
    
    public void setPermission(String permission) {
        this.permission = permission;
    }
}