package com.hanyang.belieme.demoserver.thing;

public class ThingNestedToEvent {
    private int id;
    private String name;
    private String emoji;
    private String description;
    
    private int deptId;
        
    public ThingNestedToEvent() {
    }
    
    public ThingNestedToEvent(ThingNestedToEvent oth) {
        this.id = oth.id;
        this.name = oth.name;
        this.emoji = oth.emoji;
        this.description = oth.description;
        this.deptId = oth.deptId;
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
    
    public String getDescription() {
        if(description != null) {
            return description;    
        }
        return "자세한 설명은 생략한다!";
    }
        
    public int deptIdGetter() {
        return deptId;
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
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public void setDeptId(int deptId) {
        this.deptId = deptId;
    }

}