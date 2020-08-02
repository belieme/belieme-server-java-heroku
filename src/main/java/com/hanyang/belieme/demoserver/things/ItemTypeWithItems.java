package com.hanyang.belieme.demoserver.things;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import com.hanyang.belieme.demoserver.items.*;
import com.hanyang.belieme.demoserver.events.*;

public class ItemTypeWithItems {
    private int id;
    private String name;
    private String emoji;
    private List<ItemNestedToItemType> items;

    private int amount;
    private int count;
    private String status;

    public ItemTypeWithItems(ItemTypeDB itemType) {
        if(itemType != null) {
            this.id = itemType.getId();
            this.name = new String(itemType.getName());
            this.emoji = new String(itemType.getByteArrayFromInt(itemType.getEmojiByte()), StandardCharsets.UTF_8);
        } else {
            this.id = -1;
            this.name = "";
            this.emoji = "";
        }
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
            items.add(new ItemNestedToItemType(tmpItems.get(i)));
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

    public ItemTypeDB toItemTypeDB() {
        byte arr[];
        try {
            arr = emoji.getBytes("UTF-8");
            return new ItemTypeDB(id, name, getIntFromByteArray(arr));
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