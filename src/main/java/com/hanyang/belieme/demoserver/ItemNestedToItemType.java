package com.hanyang.belieme.demoserver;

public class ItemNestedToItemType {
    private int num;
    private HistoryNestedToItem lastHistory;
    private String status;
    
    public ItemNestedToItemType(Item item) {
        if(item != null) {
            this.num = item.getNum();
            this.lastHistory = new HistoryNestedToItem(item.getLastHistory());
            this.status = item.getStatus();
        }
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
}