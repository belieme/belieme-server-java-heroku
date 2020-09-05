package com.hanyang.belieme.demoserver.thing;

import java.util.ArrayList;
import java.util.List;

import com.hanyang.belieme.demoserver.item.*;
import com.hanyang.belieme.demoserver.department.Department;

public class ThingWithItems {
    private int id;
    private String name;
    private String emoji;
    private String description;
    private int amount;
    private int count;
    private String status;
    
    private Department department;
    private List<ItemNestedToThing> items;

    
    public ThingWithItems() {
        items = new ArrayList<>();
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
    
    public List<ItemNestedToThing> getItems() {
        return items;
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
    
    public void setDepartment(Department department) {
        this.department = new Department(department);
    }

    // public void addInfo(ThingRepository thingRepository, ItemRepository itemRepository, EventRepository eventRepository) {
    //     amount = 0;
    //     count = 0;
    //     List<Item> itemListByThingId = itemRepository.findByThingId(id);
    //     this.items = new ArrayList<ItemNestedToThing>();
    //     for(int i = 0; i < itemListByThingId.size(); i++) {
    //         itemListByThingId.get(i).addInfo(thingRepository, eventRepository);
    //         this.items.add(itemListByThingId.get(i).toItemNestedToThing());
    //         if(itemListByThingId.get(i).getStatus().equals("UNUSABLE")) {
    //             amount++;
    //         }
    //         else if(itemListByThingId.get(i).getStatus().equals("USABLE")) {
    //             amount++;
    //             count++;
    //         }
    //     }
    //     if(amount == 0) { //TODO deactivate구현할때 바꾸기
    //         status = "INACTIVE";
    //     }
    //     else if(count == 0) {
    //         status = "UNUSABLE";
    //     }
    //     else if(amount >= count) {
    //         status = "USABLE";
    //     }
    //     else {
    //         status = "ERROR";
    //     }
    // }
}