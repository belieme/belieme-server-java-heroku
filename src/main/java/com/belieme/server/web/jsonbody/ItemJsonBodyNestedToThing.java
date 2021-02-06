package com.belieme.server.web.jsonbody;

public class ItemJsonBodyNestedToThing {
    private int num;
    private EventJsonBodyNestedToItem lastEvent;
    private String status;
    
    public ItemJsonBodyNestedToThing() {
    }

    public int getNum() {
        return num;
    }
    
    public EventJsonBodyNestedToItem getLastEvent() {
        return lastEvent;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setNum(int num) {
        this.num = num;
    }
    
    public void setLastEvent(EventJsonBodyNestedToItem lastEvent) {
        this.lastEvent = lastEvent;    
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
}    