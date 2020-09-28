package com.hanyang.belieme.demoserver.department;

import java.util.ArrayList;
import java.util.List;

import com.hanyang.belieme.demoserver.exception.NotFoundException;
import com.hanyang.belieme.demoserver.exception.WrongInDataBaseException;
import com.hanyang.belieme.demoserver.university.University;
import com.hanyang.belieme.demoserver.university.UniversityRepository;

public class Department {
    private int id;
    
    private University university;
    
    private String code;

    private String name;
    
    private List<String> majorCodes;
    
    private boolean available;
    
    public Department() {
    }
    
    public Department(Department oth) {
        this.id = oth.id;
        this.university = new University(oth.university);
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
    
    public University getUniversity() {
        return university;
    }
    
    public List<String> getMajorCodes() {
        return majorCodes;
    }
    
    public boolean getAvailble() {
        return available;
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
    public void setUniversity(University university) {
        this.university = new University(university);
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
    
    public DepartmentDB toDepartmentDB() {
        DepartmentDB output = new DepartmentDB();
        
        output.setId(id);
        output.setCode(code);
        output.setName(name);
        if(university != null) {
            output.setUniversityId(university.getId());
        } else {
            output.setUniversityId(0);
        }
        output.setAvailable(getAvailble());
        
        return output;
    }
}
