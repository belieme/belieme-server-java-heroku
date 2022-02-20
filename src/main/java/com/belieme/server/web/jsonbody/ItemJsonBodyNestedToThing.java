package com.belieme.server.web.jsonbody;

public class ItemJsonBodyNestedToThing {
    private int num;
    private HistoryJsonBodyNestedToItem lastHistory;
    private String status;
    
    public ItemJsonBodyNestedToThing() {
    }

    public int getNum() {
        return num;
    }
    
    public HistoryJsonBodyNestedToItem getLastHistory() {
        return lastHistory;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setNum(int num) {
        this.num = num;
    }
    
    public void setLastHistory(HistoryJsonBodyNestedToItem lastHistory) {
        this.lastHistory = lastHistory;    
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
}    