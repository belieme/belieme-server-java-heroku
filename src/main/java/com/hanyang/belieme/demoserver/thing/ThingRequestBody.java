package com.hanyang.belieme.demoserver.thing;

import java.io.UnsupportedEncodingException;

public class ThingRequestBody {
    private int id;
    private String name;
    private String emoji;
    private String description;
    private int amount;
    
    public ThingRequestBody() {
        
    }
    
    public int getId() {
        return id;
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
    
    public int getAmount() {
        return amount;
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
    
    public void setAmount(int amount) {
        this.amount = amount;
    }
    
    public ThingDB toThingDB() {
        if(emoji == null) {
            return new ThingDB(id, name, 0, description, 0);
        }
        byte arr[];
        try {
            arr = emoji.getBytes("UTF-8");
            return new ThingDB(id, name, getIntFromByteArray(arr), description, 0);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return null;
    }

    int getIntFromByteArray(byte[] bytes) {
        if(bytes.length >= 4) {
            return ((bytes[0] & 0xFF) << 24) |
                    ((bytes[1] & 0xFF) << 16) |
                    ((bytes[2] & 0xFF) << 8 ) |
                    ((bytes[3] & 0xFF) << 0 );
        }
        else {
            int result = 0;
            int shiftLength = 24;
            for(int i = 0; i < bytes.length; i++) {
                result |= ((bytes[i] & 0xFF) << shiftLength);
                shiftLength -= 8;
            }
            return result;
        }
    }
}