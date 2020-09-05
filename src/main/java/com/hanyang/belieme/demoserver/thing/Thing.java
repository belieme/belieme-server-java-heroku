package com.hanyang.belieme.demoserver.thing;

import java.io.UnsupportedEncodingException;

import com.hanyang.belieme.demoserver.department.Department;


public class Thing {
    private int id;
    private String name;
    private String emoji;
    private String description;
    
    private Department department;

    private int amount;
    private int count;
    private String status;

    public Thing() {
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
    
    public String getDescription() { //문제가 있누...
        if(description != null) {
            return description;    
        }
        return "자세한 설명은 생략한다!";
    }
    
    public Department getDepartment() {
        return department;
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
    
    public void setDepartment(Department department) {
        this.department = new Department(department);
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
    
    public ThingDB toThingDB() {
        if(emoji == null) {
            return new ThingDB(id, name, 0, description);
        }
        byte arr[];
        try {
            arr = emoji.getBytes("UTF-8");
            return new ThingDB(id, name, getIntFromByteArray(arr), description);
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