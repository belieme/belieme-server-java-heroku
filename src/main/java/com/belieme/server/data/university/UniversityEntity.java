package com.belieme.server.data.university;

import javax.persistence.*;

@Entity
public class UniversityEntity {
    @Id @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;
    private String code;
    private String name;
    @Column(nullable = true)
    private String apiUrl;
    
    public UniversityEntity() {
    }
    
    public UniversityEntity(UniversityEntity oth) {
        this.id = oth.id;
        this.code = oth.code;
        this.name = oth.name;
        this.apiUrl = oth.apiUrl;
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
}
