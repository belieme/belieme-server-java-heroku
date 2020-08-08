package com.hanyang.belieme.demoserver.item;

import com.hanyang.belieme.demoserver.event.*;

public class ItemNestedToItemType {
    private int num;
    private HistoryNestedToItem lastHistory;
    private String status;
    
    public ItemNestedToItemType() {
    }

    public int getNum() {
        return num;
    }
    
    public HistoryNestedToItem getLastHistory() {
        return lastHistory;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setNum(int num) {
        this.num = num;
    }
    
    public void setLastHistory(HistoryNestedToItem lastHistory) {
        if(lastHistory != null) {
            this.lastHistory = new HistoryNestedToItem(lastHistory);
        } else {
            this.lastHistory = null;
        }
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
}    