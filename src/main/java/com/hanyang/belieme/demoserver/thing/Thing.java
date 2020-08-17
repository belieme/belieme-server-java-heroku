package com.hanyang.belieme.demoserver.thing;

import java.io.UnsupportedEncodingException;
import java.util.List;

import com.hanyang.belieme.demoserver.item.*;
import com.hanyang.belieme.demoserver.event.*;


public class Thing {
    private int id;
    private String name;
    private String emoji;

    private int amount;
    private int count;
    private String status;

    public Thing() {
    }

    public Thing(int id, String name, String emoji) {
        this.id = id;
        this.name = name;
        this.emoji = emoji;
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

    public void setName(String name) {
        this.name = name;
    }

    public void setEmoji(String emoji) {
        this.emoji = emoji;
    }

    public void addInfo(ThingRepository thingRepository, ItemRepository itemRepository, EventRepository eventRepository) {
        amount = 0;
        count = 0;
        List<Item> items = itemRepository.findByThingId(id);
        for(int i = 0; i < items.size(); i++) {
            items.get(i).addInfo(thingRepository, eventRepository);
            if(items.get(i).getStatus().equals("UNUSABLE")) {
                amount++;
            }
            else if(items.get(i).getStatus().equals("USABLE")) {
                amount++;
                count++;
            }
        }
        if(amount == 0) { // 여기도 생각할 여지가 필요할 듯, TODO deactivate 만들 때 쓰기
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

    public ThingDB toThingDB() {
        byte arr[];
        try {
            arr = emoji.getBytes("UTF-8");
            return new ThingDB(id, name, getIntFromByteArray(arr));
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
    
    public ThingNestedToItem toThingNestedToItem() {
        ThingNestedToItem output = new ThingNestedToItem();
        output.setId(id);
        output.setName(name);
        output.setEmoji(emoji);
        
        return output;
    }
    
    public ThingWithItems toThingWithItems() {
        ThingWithItems output = new ThingWithItems();
        output.setId(id);
        output.setName(name);
        output.setEmoji(emoji);
        return output;
    }
}