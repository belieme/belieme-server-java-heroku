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
    
    private String departmentCode;

    private String departmentName;
    
    private boolean available;
    
    public DepartmentDB() {
        
    }
    
    public DepartmentDB(String departmentId, String departmentName) {
        this.departmentCode = departmentId;
        this.departmentName = departmentName;
        available = true;
    }

    public int getId() {
        return id;
    }
    
    public String getDepartmentCode() {
        return departmentCode;
    }
    
    public String getDepartmentName() {
        return departmentName;
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
    
    public void setDepartmentCode(String departmentCode) {
        this.departmentCode = departmentCode;
    }
    
    public void setDepartmentName(String departmentName) {
        this.departmentName = departmentName;
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
        output.setDepartmentCode(departmentCode);
        output.setDepartmentName(departmentName);
        output.setMajorCodes(majorCodes);
        output.setAvailable(isAvailble());
        
        return output;
    }
}
