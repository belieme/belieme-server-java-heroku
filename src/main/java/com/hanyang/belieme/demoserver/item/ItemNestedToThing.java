package com.hanyang.belieme.demoserver.item;

import com.hanyang.belieme.demoserver.event.*;

public class ItemNestedToThing {
    private int id;
    private int num;
    private EventNestedToItem lastEvent;
    private String status;
    
    public ItemNestedToThing() {
    }
    
    public int getId() {
        return id;
    }

    public int getNum() {
        return num;
    }
    
    public EventNestedToItem getLastEvent() {
        if(lastEvent == null) {
            return null;
        }
        return new EventNestedToItem(lastEvent);
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setId(int id) {
        this.id = id;
    }    
    
    public void setNum(int num) {
        this.num = num;
    }
    
    public void setLastEvent(EventNestedToItem lastEvent) {
        if(lastEvent == null) {
            this.lastEvent = null;
        } else {
            this.lastEvent = new EventNestedToItem(lastEvent);    
        }
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
}    