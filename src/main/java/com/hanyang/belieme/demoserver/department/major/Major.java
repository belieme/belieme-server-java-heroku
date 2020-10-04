package com.hanyang.belieme.demoserver.department.major;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import javax.persistence.Entity;

@Entity
public class Major {
    @Id @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;
    
    private String code;
    
    private int departmentId;
    
    public Major() {
    }
    
    public int getId() {
        return id;
    }
    
    public String getCode() {
        return code;
    }
    
    public int getDepartmentId() {
        return departmentId;
    }
    
    public void setCode(String code) {
        this.code = code;
    }
    
    public void setDepartmentId(int departmentId) {
        this.departmentId = departmentId;
    }
    
    public MajorResponse toMajorResponse() {
        MajorResponse output = new MajorResponse();
        output.setId(id);
        output.setCode(code);
        return output;
    }
}