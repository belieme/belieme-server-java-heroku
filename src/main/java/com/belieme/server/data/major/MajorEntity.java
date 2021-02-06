package com.belieme.server.data.major;

import javax.persistence.*;

@Entity
public class MajorEntity {
    @Id @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;
    
    private String code;
    
    private int deptId;
    
    public MajorEntity() {
    }
    
    public int getId() {
        return id;
    }
    
    public String getCode() {
        return code;
    }
    
    public int getDeptId() {
        return deptId;
    }
    
    public void setCode(String code) {
        this.code = code;
    }
    
    public void setDeptId(int deptId) {
        this.deptId = deptId;
    }
}