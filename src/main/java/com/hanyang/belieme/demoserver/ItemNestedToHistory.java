package com.hanyang.belieme.demoserver;

public class ItemNestedToHistory {
    private int num;
    private ItemTypeNestedToItem itemType;
    
    public ItemNestedToHistory(int num, ItemTypeDB itemType) {
        if(itemType != null) {
            this.num = num;
            this.itemType = new ItemTypeNestedToItem(itemType);
        }
    }

    public int getNum() {
        return num;
    }

    public ItemTypeNestedToItem getItemType() {
        return itemType;
    }
}