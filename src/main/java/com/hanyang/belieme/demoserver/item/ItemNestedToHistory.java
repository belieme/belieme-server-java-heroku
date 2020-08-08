package com.hanyang.belieme.demoserver.item;

import com.hanyang.belieme.demoserver.thing.*;

public class ItemNestedToHistory {
    private int id;
    private int num;
    private ItemTypeNestedToItem itemType;
    
    private String currentStatus;
    
    public ItemNestedToHistory() {
    }
    // public ItemNestedToHistory(int num, ItemType itemType) {
    //     this.num = num;
    //     if(itemType != null) {
    //         this.itemType = itemType.toItemTypeNestedToItem();
    //     } else {
    //         this.itemType = null;
    //     }
    // }

    public int getId() {
        return id;
    }
    
    public int getNum() {
        return num;
    }

    public ItemTypeNestedToItem getItemType() {
        return itemType;
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
    
    public void setItemType(ItemTypeNestedToItem itemType) {
        if(itemType != null) {
            this.itemType = new ItemTypeNestedToItem(itemType);    
        }
        this.itemType = null;        
    }
    
    public void setCurrentStatus(String currentStatus) {
        this.currentStatus = currentStatus;
    }
}