package com.belieme.server.web.jsonbody;

public class ThingInfoJsonBody {
    private String code;
    private String name;
    private String emoji;
    private String description;
    private Integer amount;

    public ThingInfoJsonBody() {
    }
    
    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public String getEmoji() {
        return emoji;
    }
    
    public String getDescription() {
            return description;    
    }
    
    public Integer getAmount() {
        return amount;
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
    
    public void setAmount(Integer amount) {
        this.amount = amount;
    }
}