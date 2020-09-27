package com.hanyang.belieme.demoserver.thing;

import java.util.ArrayList;
import java.util.List;

import com.hanyang.belieme.demoserver.item.*;

public class ThingWithItems {
    private int id;
    private String name;
    private String emoji;
    private String description;
    private int amount;
    private int count;
    private String status;
    
    private List<ItemNestedToThing> items;
    
    private int deptId;

    
    public ThingWithItems() {
        items = new ArrayList<>();
    }
    
    public ThingWithItems(ThingWithItems oth) {
        this.id = oth.id;
        this.name = oth.name;
        this.emoji = oth.emoji;
        this.description = oth.description;
        this.amount = oth.amount;
        this.count = oth.count;
        this.status = oth.status;
        this.items = new ArrayList<>(oth.items);
        this.deptId = oth.deptId;
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
    
    public List<ItemNestedToThing> getItems() {
        return items;
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
    
    public void setAmount(int amount) {
        this.amount = amount;
    }
    
    public void setCount(int count) {
        this.count = count;
    }

    public void setStatus(String status) {
        this.status = status;
    }
    
    public void setDeptId(int deptId) {
        this.deptId = deptId;
    }
}