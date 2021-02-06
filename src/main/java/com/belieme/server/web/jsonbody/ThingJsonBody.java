package com.belieme.server.web.jsonbody;

public class ThingJsonBody {
    private String code;
    private String name;
    private String emoji;
    private String description;

    private int amount;
    private int count;
    private String status;

    public ThingJsonBody() {
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
        if(description != null) {
            return description;    
        }
        return "자세한 설명은 생략한다!";
    }
    
    public int getAmount() {
        return amount;
    }

    public int getCount() {
        return count;
    }

    public String getStatus() {
        return status;
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
    
    public void setAmount(int amount) {
        this.amount = amount;
    }
    
    public void setCount(int count) {
        this.count = count;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
}