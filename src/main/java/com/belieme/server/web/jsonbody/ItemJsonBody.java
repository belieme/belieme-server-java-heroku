package com.belieme.server.web.jsonbody;

public class ItemJsonBody {
    private int num;
    private String status;
    private HistoryJsonBodyNestedToItem lastHistory;

    public ItemJsonBody() {
    }
    
    public int getNum() {
        return num;
    }

    public String getStatus() {
        return status;
    }
    
    public HistoryJsonBodyNestedToItem getLastHistory() {
        return lastHistory;
    }
    
    public void setNum(int num) {
        this.num = num;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public void setLastHistory(HistoryJsonBodyNestedToItem lastHistory) {
        this.lastHistory = lastHistory;
    }
}