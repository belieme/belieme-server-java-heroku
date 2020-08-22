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
    
    @Column(name = "student_id", nullable = false)
    private String studentId;

    @Column(name = "name", nullable = false)
    private String name;

    //TODO department 연결 및 한양 API정보 추가
    
    @Column(name = "permission", nullable = false)
    private String permission;

    public int getId() {
        return id;
    }
    
    public String token() {
        return token;
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
