package com.hanyang.belieme.demoserver.item;

public class ItemNestedToEvent {
    private int id;
    private int num;
    
    private String currentStatus;

    
    public ItemNestedToEvent() {
    }

    public ItemNestedToEvent(ItemNestedToEvent oth) {
        this.id = oth.id;
        this.num = oth.num;
        this.currentStatus = oth.currentStatus;
    }
    
    public int getId() {
        return id;
    }
    
    public int getNum() {
        return num;
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
    
    public void setCurrentStatus(String currentStatus) {
        this.currentStatus = currentStatus;
    }
}