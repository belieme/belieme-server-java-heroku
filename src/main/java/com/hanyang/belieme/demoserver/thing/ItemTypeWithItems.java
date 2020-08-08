package com.hanyang.belieme.demoserver.thing;

import java.util.ArrayList;
import java.util.List;

import com.hanyang.belieme.demoserver.item.*;
import com.hanyang.belieme.demoserver.event.*;

public class ItemTypeWithItems {
    private int id;
    private String name;
    private String emoji;
    private List<ItemNestedToItemType> items;

    private int amount;
    private int count;
    private String status;
    
    public ItemTypeWithItems() {
        
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

    public int getAmount() {
        return amount;
    }

    public int getCount() {
        return count;
    }

    public String getStatus() {
        return status;
    }
    
    public List<ItemNestedToItemType> getItems() {
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

    public void addInfo(ItemTypeRepository itemTypeRepository, ItemRepository itemRepository, HistoryRepository historyRepository) {
        amount = 0;
        count = 0;
        List<Item> tmpItems = itemRepository.findByTypeId(id);
        items = new ArrayList<ItemNestedToItemType>();
        for(int i = 0; i < tmpItems.size(); i++) {
            tmpItems.get(i).addInfo(itemTypeRepository, historyRepository);
            items.add(tmpItems.get(i).toItemNestedToItemType());
            if(tmpItems.get(i).getStatus().equals("UNUSABLE")) {
                amount++;
            }
            else if(tmpItems.get(i).getStatus().equals("USABLE")) {
                amount++;
                count++;
            }
        }
        if(amount == 0) {
            status = "INACTIVE";
        }
        else if(count == 0) {
            status = "UNUSABLE";
        }
        else if(amount >= count) {
            status = "USABLE";
        }
        else {
            status = "ERROR";
        }
    }
}