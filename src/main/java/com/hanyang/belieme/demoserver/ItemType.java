package com.hanyang.belieme.demoserver;

import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Optional;

public class ItemType {
    private int id;
    private String name;
    private String emoji;

    private int count;
    private int amount;

    public ItemType() {
    }

    public ItemType(String name, String emoji, int count, int amount) {
        this.name = name;
        this.emoji = emoji;
        this.count = count;
        this.amount = amount;
    }

    public ItemType(int id, String name, String emoji, int count, int amount) {
        this.id = id;
        this.name = name;
        this.emoji = emoji;
        this.count = count;
        this.amount = amount;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmoji() {
        return emoji;
    }

    public void setEmoji(String emoji) {
        this.emoji = emoji;
    }

    public int getCount() {
        return count;
    }

    public void resetCount() {
        this.count = 0;
    }

    public void increaseCount() {
        this.count++;
    }

    public int getAmount() {
        return amount;
    }

    public void resetAmount() {
        this.amount = 0;
    }

    public void increaseAmount() {
        this.amount++;
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