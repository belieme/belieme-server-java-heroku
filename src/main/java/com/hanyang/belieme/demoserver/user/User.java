package com.hanyang.belieme.demoserver.user;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
public class User {
    @Id @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;
    
    private String token;
    
    private int departmentId;
    
    @Column(name = "student_id", nullable = false)
    private String studentId;

    private String name;
    
    private String permission;
    
    public int getId() {
        return id;
    }
    
    public String getToken() {
        return token;
    }
    
    public int departmentIdGetter() {
        return departmentId;
    }
    
    public String getStudentId() {
        return studentId;
    }

    public String getName() {
        return name;
    }

    public String getPermission() {
        return permission;
    }

    public void setName(String name) {
        this.name = name;
    }
    
    public void setToken(String token) {
        this.token = token;
    }
    
    public void setDepartmentId(int departmentId) {
        this.departmentId = departmentId;
    }
    
    public void setStudentId(String studentId) {
        this.studentId = studentId;
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
