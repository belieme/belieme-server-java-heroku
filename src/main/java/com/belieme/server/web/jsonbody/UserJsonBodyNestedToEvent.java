package com.belieme.server.web.jsonbody;

public class UserJsonBodyNestedToEvent {
    private String studentId;

    private String name;
    
    private int entranceYear;
    
    
    public UserJsonBodyNestedToEvent() {
        
    }
    
    public String getStudentId() {
        return studentId;
    }

    public String getName() {
        return name;
    }
    
    public int getEntranceYear() {
        return entranceYear;
    }
    
    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    public void setName(String name) {
        this.name = name;
    }
    
    public void setEntranceYear(int entranceYear) {
        this.entranceYear = entranceYear;
    }
}
