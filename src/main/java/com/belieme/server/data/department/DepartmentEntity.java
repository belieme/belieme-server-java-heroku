package com.belieme.server.data.department;

import javax.persistence.*;

@Entity
public class DepartmentEntity {
    @Id @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;
    private int univId;
    private String code;
    private String name;
    private boolean available;
    
    public DepartmentEntity() {
    }

    public int getId() {
        return id;
    }
    
    public String getCode() {
        return code;
    }
    
    public String getName() {
        return name;
    }
    
    public int getUnivId() {
        return univId;
    }
    
    public boolean isAvailble() {
        return available;
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
    public void setUnivId(int univId) {
        this.univId = univId;
    }
    
    public void setCode(String code) {
        this.code = code;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public void setAvailable(boolean available) {
        this.available = available;
    }
}
