package com.hanyang.belieme.demoserver.department.major;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import com.hanyang.belieme.demoserver.department.DepartmentDB;
import com.hanyang.belieme.demoserver.department.DepartmentRepository;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Entity;

@Entity
public class Major {
    @Id @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;
    
    private String majorCode;
    
    private int departmentId;
    
    public Major() {
    }
    
    public int getId() {
        return id;
    }
    
    public String getMajorCode() {
        return majorCode;
    }
    
    public int getDepartmentId() {
        return departmentId;
    }
    
    public void setMajorCode(String majorCode) {
        this.majorCode = majorCode;
    }
    
    public void setDepartmentId(int departmentId) {
        this.departmentId = departmentId;
    }
}