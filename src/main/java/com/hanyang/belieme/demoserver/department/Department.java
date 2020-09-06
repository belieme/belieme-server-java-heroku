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
    
    private List<String> majorCodes;
    
    private boolean available;
    
    public Department() {
    }
    
    public Department(Department oth) {
        this.id = oth.id;
        this.university = new University(oth.university);
        this.departmentCode = oth.departmentCode;
        this.departmentName = oth.departmentName;
        this.majorCodes = new ArrayList<>(oth.majorCodes);
        this.available = oth.available;
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
    
    public void setDepartmentCode(String departmentCode) {
        this.departmentCode = departmentCode;
    }
    
    public void setDepartmentName(String departmentName) {
        this.departmentName = departmentName;
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
        output.setDepartmentCode(departmentCode);
        output.setDepartmentName(departmentName);
        if(university != null) {
            output.setUniversityId(university.getId());
        } else {
            output.setUniversityId(0);
        }
        output.setAvailable(getAvailble());
        
        return output;
    }
    
    public static int findIdByUniversityCodeAndDepartmentCode(UniversityRepository universityRepository, DepartmentRepository departmentRepository, String univCode, String departmentCode) throws NotFoundException, WrongInDataBaseException {
        List<DepartmentDB> tmpList = departmentRepository.findByUniversityIdAndDepartmentCode(University.findIdByUniversityCode(universityRepository, univCode), departmentCode);
        if(tmpList.size() == 0) {
            throw new NotFoundException();
        } else if(tmpList.size() == 1) {
            return tmpList.get(0).getId();
        } else {
            throw new WrongInDataBaseException();
        }
    }
}
