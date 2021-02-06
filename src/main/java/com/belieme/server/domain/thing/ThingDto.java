package com.belieme.server.domain.thing;

public class ThingDto { 
    private String univCode;
    private String deptCode;
    
    private String code;
    private String name;
    private String emoji;
    private String description;

    public ThingDto() {
    }

    public String getUnivCode() {
        return univCode;
    }
    
    public String getDeptCode() {
        return deptCode;
    }
    
    public String getCode() {
        return code;    
    }
    
    public String getName() {
        return name;
    }

    public String getEmoji() {
        return emoji;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setUnivCode(String univCode) {
        this.univCode = univCode;
    }
    
    public void setDeptCode(String deptCode) {
        this.deptCode = deptCode;
    }
    
    public void setCode(String code) {
        this.code = code;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public void setEmoji(String emoji) {
        this.emoji = emoji;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
}