package com.hanyang.belieme.demoserver;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class Admin {
    @Id
    @Column(name = "student_id")
    private int studentId;

    @Column(name = "name")
    private String name;

    @Column(name = "permission")
    private String permission;

    public int getStudentId() {
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

    public void permissionSetAdmin() {
        permission = "admin";
    }

    public void permissionSetMaster() {
        permission = "master";
    }

    public void permissionSetDeveloper() {
        permission = "developer";
    }
}
