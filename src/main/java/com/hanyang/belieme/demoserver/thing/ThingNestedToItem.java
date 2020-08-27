package com.hanyang.belieme.demoserver.thing;

import com.hanyang.belieme.demoserver.department.Department;

public class ThingNestedToItem {
    private int id;
    private String name;
    private String emoji;
    private String description;
    
    private Department department;
        
    public ThingNestedToItem() {
    }
    
    public ThingNestedToItem(ThingNestedToItem oth) {
        this.id = oth.id;
        this.name = oth.name;
        this.emoji = oth.emoji;
        this.description = oth.description;
        this.department = oth.department; // copy constructor로 만들기
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
        return description;
    }
    
    public Department getDepartment() {
        return department;
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
        this.department = department;  // copy constructor로 만들기
    }
}