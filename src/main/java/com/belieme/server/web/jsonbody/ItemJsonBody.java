package com.belieme.server.web.jsonbody;

public class ItemJsonBody {
    private int num;
    private String status;
    private EventJsonBodyNestedToItem lastEvent;

    public ItemJsonBody() {
    }
    
    public int getNum() {
        return num;
    }

    public String getStatus() {
        return status;
    }
    
    public EventJsonBodyNestedToItem getLastEvent() {
        return lastEvent;
    }
    
    public void setNum(int num) {
        this.num = num;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public void setLastEvent(EventJsonBodyNestedToItem lastEvent) {
        this.lastEvent = lastEvent;
    }
}