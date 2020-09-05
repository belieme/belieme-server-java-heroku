package com.hanyang.belieme.demoserver.university;

import java.util.List;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import com.hanyang.belieme.demoserver.exception.NotFoundException;
import com.hanyang.belieme.demoserver.exception.WrongInDataBaseException;

@Entity
public class University { // TODO api url도 갖고 있게 하기
    @Id @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;
    private String universityCode;
    private String name;
    
    public University() {
    }
    
    public University(University oth) {
        this.id = oth.id;
        this.universityCode = oth.universityCode;
        this.name = oth.name;
    }
    
    public University(String universityCode, String name) {
        this.universityCode = universityCode;
        this.name = name;
    }
    
    public int getId() {
        return id;
    }
    
    public String getUniversityCode() {
        return universityCode;
    }
    
    public String getName() {
        return name;
    }

    public void setUniversityCode(String universityCode) {
        this.universityCode = universityCode;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public static int findIdByUniversityCode(UniversityRepository universityRepository, String universityCode) throws NotFoundException, WrongInDataBaseException {
        List<University> universityList = universityRepository.findByUniversityCode(universityCode);
        if(universityList.size() == 0) {
            throw new NotFoundException();
        } else if(universityList.size() == 1) {
            return universityList.get(0).getId();
        } else {
            throw new WrongInDataBaseException();
        }
    }
}
