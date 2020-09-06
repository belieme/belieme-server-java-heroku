package com.hanyang.belieme.demoserver.event;

public class EventRequestBody {
    private String requesterStudentId;
    private String responseManagerStudentId;
    private String returnManagerStudentId;
    private String lostManagerStudentId;
        
    public EventRequestBody(){}
        
    public String getRequesterStudentId() {
        return requesterStudentId;
    }
        
    public String getResponseManagerStudentId() {
        return responseManagerStudentId;
    }
        
    public String getReturnManagerStudentId() {
        return returnManagerStudentId; 
    }
        
    public String getLostManagerStudentId() {
        return lostManagerStudentId;
    }
}