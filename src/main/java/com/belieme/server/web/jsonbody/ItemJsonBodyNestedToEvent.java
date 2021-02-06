package com.belieme.server.web.jsonbody;

public class ItemJsonBodyNestedToEvent {
    private int num;
    
    private String currentStatus;

    
    public ItemJsonBodyNestedToEvent() {
    }
    
    public int getNum() {
        return num;
    }
    
    public String getCurrentStatus() {
        return currentStatus;
    }
    
    public void setNum(int num) {
        this.num = num;
    }
    
    public void setCurrentStatus(String currentStatus) {
        this.currentStatus = currentStatus;
    }
}