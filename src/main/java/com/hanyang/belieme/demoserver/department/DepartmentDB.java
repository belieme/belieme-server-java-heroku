package com.hanyang.belieme.demoserver.department;

import java.util.ArrayList;
import java.util.Optional;

import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import com.hanyang.belieme.demoserver.common.StringListConverter;
import com.hanyang.belieme.demoserver.university.University;
import com.hanyang.belieme.demoserver.university.UniversityRepository;


@Entity
public class DepartmentDB {
    @Id @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;
    
    private int universityId;
    
    private String departmentCode;

    private String departmentName;
    
    @Convert(converter = StringListConverter.class)
    private ArrayList<String> majorCodes;
    
    private boolean available;
    
    public DepartmentDB() {
        
    }
    
    public DepartmentDB(String departmentId, String departmentName) {
        this.departmentCode = departmentId;
        this.departmentName = departmentName;
        majorCodes = new ArrayList<String>();
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
    
    public ArrayList<String> getMajorCodes() {
        return new ArrayList<String>(majorCodes);
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
    
    public void setMajorCodes(ArrayList<String> majorCodes) {
        this.majorCodes = majorCodes;
    }
    
    public void setAvailable(boolean available) {
        this.available = available;
    }
    
    public void addMajor(String majorCode) {
        majorCodes.add(majorCode);
    }
    
    public boolean deleteMajor(String majorCode) {
        return majorCodes.remove(majorCode);
    }
    
    public void unable() {
        available = false;
    }
    
    public void able() {
        available = true;
    }
    
    public Department toDepartment(UniversityRepository universityRepository) {
        Department output = new Department();
        
        Optional<University> universityOptional = universityRepository.findById(universityId);
        if(universityOptional.isPresent()) {
            output.setUniversity(universityOptional.get());
        }
        output.setId(id);
        output.setDepartmentCode(departmentCode);
        output.setDepartmentName(departmentName);
        output.setMajorCodes(new ArrayList<String>(majorCodes));
        output.setAvailable(isAvailble());
        
        return output;
    }
}
