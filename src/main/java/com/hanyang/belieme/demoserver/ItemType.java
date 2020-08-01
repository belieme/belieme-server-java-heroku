package com.hanyang.belieme.demoserver;

import java.io.UnsupportedEncodingException;
import java.util.List;

public class ItemType {
    private int id;
    private String name;
    private String emoji;

    int amount;
    int count;
    String status;

    public ItemType() {
    }

    public ItemType(String name, String emoji) {
        this.name = name;
        this.emoji = emoji;
    }

    public ItemType(int id, String name, String emoji) {
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
    
    public String toString() {
        return "{" +
                "\"id\": \"" + id + "\"" +
                "\"name\": \"" + name + "\"" +
                "\"emoji\": \"" + emoji + "\"" +
                "}";
    }

    public void addInfo(ItemTypeRepository itemTypeRepository, ItemRepository itemRepository, HistoryRepository historyRepository) {
        amount = 0;
        count = 0;
        List<Item> items = itemRepository.findByTypeId(id);
        for(int i = 0; i < items.size(); i++) {
            items.get(i).addInfo(itemTypeRepository, historyRepository);
            if(items.get(i).getStatus().equals("UNUSABLE")) {
                amount++;
            }
            else if(items.get(i).getStatus().equals("USABLE")) {
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