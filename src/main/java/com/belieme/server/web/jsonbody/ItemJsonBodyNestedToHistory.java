package com.belieme.server.web.jsonbody;

public class ItemJsonBodyNestedToHistory {
    private int num;
    
    private String currentStatus;

    
    public ItemJsonBodyNestedToHistory() {
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