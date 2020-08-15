package com.hanyang.belieme.demoserver.thing;

public class ThingNestedToItem {
    private int id;
    private String name;
    private String emoji;
        
    public ThingNestedToItem() {
    }
    
    public ThingNestedToItem(ThingNestedToItem oth) {
        this.id = oth.id;
        this.name = oth.name;
        this.emoji = oth.emoji;
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
    
    public void setId(int id) {
        this.id = id;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public void setEmoji(String emoji) {
        this.emoji = emoji;
    }
}