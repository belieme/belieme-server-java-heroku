package com.belieme.server.web.jsonbody;

public class ThingJsonBodyNestedToEvent {
    private String code;
    private String name;
    private String emoji;
    private String description;
        
    public ThingJsonBodyNestedToEvent() {
    }
        
    public String getCode(){
        return code;
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
    
    public void setCode(String code) {
        this.code = code;
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
}