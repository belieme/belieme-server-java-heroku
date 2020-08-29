package com.hanyang.belieme.demoserver.item;

import com.hanyang.belieme.demoserver.thing.*;

public class ItemNestedToEvent {
    private int id;
    private int num;
    private ThingNestedToItem thing;
    
    private String currentStatus;
    
    public ItemNestedToEvent() {
    }

    public int getId() {
        return id;
    }
    
    public int getNum() {
        return num;
    }

    public ThingNestedToItem getThing() {
        return thing;
    }
    
    public String getCurrentStatus() {
        return currentStatus;
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
    public void setNum(int num) {
        this.num = num;
    }
    
    public void setThing(ThingNestedToItem thing) {
        this.thing = thing;     
    }
    
    public void setCurrentStatus(String currentStatus) {
        this.currentStatus = currentStatus;
    }
}