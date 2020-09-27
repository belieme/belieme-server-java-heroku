package com.hanyang.belieme.demoserver.item;

import com.hanyang.belieme.demoserver.event.*;

public class Item {
    private int id;
    private int num;
    private String status;
    
    private EventNestedToItem lastEvent;
    
    private int thingId;

    public Item() {
    }
    
    public Item(Item oth) {
        this.id = oth.id;
        this.num = oth.num;
        this.status = oth.status;
        this.lastEvent = new EventNestedToItem(oth.lastEvent);
        this.thingId = oth.thingId;
    }
    
    public int getId() {
        return id;
    }
    
    public int getNum() {
        return num;
    }

    public String getStatus() {
        return status;
    }
    
    public EventNestedToItem getLastEvent() {
        if(lastEvent == null) {
            return null;
        }
        return new EventNestedToItem(lastEvent);
    }
    
    public int thingIdGetter() {
        return thingId;
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
    public void setNum(int num) {
        this.num = num;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public void setLastEvent(EventNestedToItem lastEvent) {
        if(lastEvent == null) {
            this.lastEvent = null;
        } else {
            this.lastEvent = new EventNestedToItem(lastEvent);    
        }
    }
    
    public void setThingId(int thingId) {
        this.thingId = thingId;
    }
}