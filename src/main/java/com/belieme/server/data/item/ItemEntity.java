package com.belieme.server.data.item;

import javax.persistence.*;

@Entity
public class ItemEntity {
    @Id @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;
    private int thingId;
    private int num;
    
    private int lastHistoryId;

    public ItemEntity() {
    }
    
    public int getId() {
        return id;
    }
    
    public int getThingId() {
        return thingId;
    }

    public int getNum() {
        return num;
    }
    
    public int getLastHistoryId() {
        return lastHistoryId;
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
    public void setThingId(int thingId) {
        this.thingId = thingId;
    }

    public void setNum(int num) {
        this.num = num;
    }

    public void setLastHistoryId(int lastHistoryId) {
        this.lastHistoryId = lastHistoryId;
    }
}