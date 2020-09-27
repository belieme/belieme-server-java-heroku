package com.hanyang.belieme.demoserver.university;

import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import com.hanyang.belieme.demoserver.exception.NotFoundException;
import com.hanyang.belieme.demoserver.exception.WrongInDataBaseException;

@Entity
public class University {
    @Id @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;
    private String code;
    private String name;
    @Column(nullable = true)
    private String apiUrl;
    
    public University() {
    }
    
    public University(University oth) {
        this.id = oth.id;
        this.code = oth.code;
        this.name = oth.name;
        this.apiUrl = oth.apiUrl;
    }
    
    public University(String code, String name) {
        this.code = code;
        this.name = name;
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
    
    public String getApiUrl() {
        return apiUrl;
    }

    public void setCode(String code) {
        this.code = code;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public void setApiUrl(String apiUrl) {
        this.apiUrl = apiUrl;
    }
    
    public static University findByUnivCode(UniversityRepository universityRepository, String univCode) throws NotFoundException, WrongInDataBaseException {
        List<University> univList = universityRepository.findByCode(univCode);
        if(univList.size() == 0) {
            throw new NotFoundException();
        } else if(univList.size() == 1) {
            return univList.get(0);
        } else {
            throw new WrongInDataBaseException();
        }
    }
}
