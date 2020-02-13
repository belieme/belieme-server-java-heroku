package com.hanyang.belieme.demoserver;

import java.io.UnsupportedEncodingException;

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

    public void setCount(int count) {
        this.count = count;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public ItemTypeDB toItemTypeDB() {
        byte arr[];
        try {
            arr = emoji.getBytes("UTF-8");
            return new ItemTypeDB(id, name, getIntFromByteArray(arr), count, amount);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return null;
    }

    int getIntFromByteArray(byte[] bytes) {
        return ((bytes[0] & 0xFF) << 24) |
                ((bytes[1] & 0xFF) << 16) |
                ((bytes[2] & 0xFF) << 8 ) |
                ((bytes[3] & 0xFF) << 0 );
    }
}