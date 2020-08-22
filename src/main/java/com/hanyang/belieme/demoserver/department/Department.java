package com.hanyang.belieme.demoserver.department;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import com.hanyang.belieme.demoserver.common.StringListConverter;
import com.hanyang.belieme.demoserver.exception.NotFoundException;
import com.hanyang.belieme.demoserver.exception.WrongInDataBaseException;
import com.hanyang.belieme.demoserver.university.University;
import com.hanyang.belieme.demoserver.university.UniversityRepository;


@Entity
public class Department {
    @Id @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;
    
    private int universityId;
    
    @Column(name = "department_code", nullable = false)
    private String departmentCode;

    @Column(name = "department_name", nullable = false)
    private String departmentName;
    
    @Convert(converter = StringListConverter.class)
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
    
    public boolean isAvailble() {
        return available;
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
