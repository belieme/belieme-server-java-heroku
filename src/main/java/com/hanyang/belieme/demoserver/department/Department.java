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
    
    private String departmentCode;

    private String departmentName;
    
    private ArrayList<String> majorCodes;
    
    private boolean available;
    
    public Department(String departmentId, String departmentName) {
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
    
    public University getUniversity() {
        return university;
    }
    
    public boolean isAvailble() {
        return available;
    }
    
    public void setUniversity(University university) {
        this.university = university; //TODO copyConstructor이용
    }
    
    public void setDepartmentCode(String departmentCode) {
        this.departmentCode = departmentCode;
    }
    
    public void setDepartmentName(String departmentName) {
        this.departmentName = departmentName;
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
    
    public static int findIdByUniversityCodeAndDepartmentCode(UniversityRepository universityRepository, DepartmentRepository departmentRepository, String univCode, String departmentCode) throws NotFoundException, WrongInDataBaseException {
        List<Department> tmpList = departmentRepository.findByUniversityIdAndDepartmentCode(University.findIdByUniversityCode(universityRepository, univCode), departmentCode);
        if(tmpList.size() == 0) {
            throw new NotFoundException();
        } else if(tmpList.size() == 1) {
            return tmpList.get(0).getId();
        } else {
            throw new WrongInDataBaseException();
        }
    }
}
