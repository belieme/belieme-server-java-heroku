package com.hanyang.belieme.demoserver.department;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import com.hanyang.belieme.demoserver.department.major.Major;
import com.hanyang.belieme.demoserver.department.major.MajorRepository;
import com.hanyang.belieme.demoserver.university.University;
import com.hanyang.belieme.demoserver.university.UniversityRepository;


@Entity
public class DepartmentDB {
    @Id @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;
    
    private int universityId;
    
    private String code;

    private String name;
    
    private boolean available;
    
    public DepartmentDB() {
        
    }
    
    public DepartmentDB(String code, String name) {
        this.code = code;
        this.name = name;
        available = true;
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
    
    public int getUniversityId() {
        return universityId;
    }
    
    public boolean isAvailble() {
        return available;
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
    public void setUniversityId(int universityId) {
        this.universityId = universityId;
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
    
    public void unable() {
        available = false;
    }
    
    public void able() {
        available = true;
    }
    
    public Department toDepartment(UniversityRepository universityRepository, MajorRepository majorRepository) {
        Department output = new Department();
        
        Optional<University> universityOptional = universityRepository.findById(universityId);
        if(universityOptional.isPresent()) {
            output.setUniversity(universityOptional.get());
        }
        
        List<Major> majorsByDepartmentId = majorRepository.findByDepartmentId(id);
        ArrayList<String> majorCodes = new ArrayList<>();
        for(int i = 0; i < majorsByDepartmentId.size(); i++) {
            majorCodes.add(majorsByDepartmentId.get(i).getMajorCode());
        }
        
        output.setId(id);
        output.setCode(code);
        output.setName(name);
        output.setMajorCodes(majorCodes);
        output.setAvailable(isAvailble());
        
        return output;
    }
    
    public DepartmentNestedToUser toDepartmentNestedToUser() {
        DepartmentNestedToUser output = new DepartmentNestedToUser();
        output.setId(id);
        output.setCode(code);
        output.setName(name);
        output.setAvailable(isAvailble());
        return output;
    }
    
}
