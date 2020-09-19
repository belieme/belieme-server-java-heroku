package com.hanyang.belieme.demoserver.user;

public class UserNestedToEvent {
    private int id;
    
    private String studentId;

    private String name;
    
    private int entranceYear;
    
    
    public UserNestedToEvent() {
        
    }
    
    public UserNestedToEvent(UserNestedToEvent oth) {
        this.id = oth.id;
        this.studentId = oth.studentId;
        this.name = oth.name;
        this.entranceYear = oth.entranceYear;
    }
    
    public int getId() {
        return id;
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
    
    public void setId(int id) {
        this.id = id;
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
