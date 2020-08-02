package com.hanyang.belieme.demoserver.thing;

import java.nio.charset.StandardCharsets;

public class ItemTypeNestedToItem {
    private int id;
    private String name;
    private String emoji;
        
    public ItemTypeNestedToItem(ItemTypeDB itemType) {
        if(itemType != null) {
            this.id = itemType.getId();
            this.name = new String(itemType.getName());
            this.emoji = new String(itemType.getByteArrayFromInt(itemType.getEmojiByte()), StandardCharsets.UTF_8);
        } else {
            this.id = -1;
            this.name = "";
            this.emoji = "";
        }
    }
        
    public int getId(){
        return id;
    }
    
    public String getName() {
        return name;
    }
        
    public String getEmoji() {
        return emoji;
    }
}