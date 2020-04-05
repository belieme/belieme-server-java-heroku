package com.hanyang.belieme.demoserver;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class Admin {
    @Id
    @Column(name = "student_id", nullable = false)
    private int studentId;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "permission", nullable = false)
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
        permission = "ADMIN";
    }

    public void permissionSetMaster() {
        permission = "MASTER";
    }

    public void permissionSetDeveloper() {
        permission = "DEVELOPER";
    }
}
