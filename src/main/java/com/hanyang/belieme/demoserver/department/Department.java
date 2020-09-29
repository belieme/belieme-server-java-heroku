package com.hanyang.belieme.demoserver.department;

import java.util.ArrayList;
import java.util.List;

public class Department {
    private int id;
    
    private int univId;
    
    private String code;

    private String name;
    
    private List<String> majorCodes;
    
    private boolean available;
    
    public Department() {
    }
    
    public Department(Department oth) {
        this.id = oth.id;
        this.code = oth.code;
        this.name = oth.name;
        this.majorCodes = new ArrayList<>(oth.majorCodes);
        this.available = oth.available;
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
    
    public List<String> getMajorCodes() {
        return majorCodes;
    }
    
    public boolean getAvailble() {
        return available;
    }
    
    public int univIdGetter() {
        return univId;
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
    public void setCode(String code) {
        this.code = code;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public void setMajorCodes(List<String> majorCodes) {
        this.majorCodes = new ArrayList<String>(majorCodes);
    }
    
    public void setAvailable(boolean available) {
        this.available = available;
    }
    
    public void setUnivId(int univId) {
        this.univId = univId;
    }
    
    public DepartmentDB toDepartmentDB() {
        DepartmentDB output = new DepartmentDB();
        
        output.setId(id);
        output.setCode(code);
        output.setName(name);
        output.setUniversityId(univId);
        output.setAvailable(getAvailble());
        
        return output;
    }
}