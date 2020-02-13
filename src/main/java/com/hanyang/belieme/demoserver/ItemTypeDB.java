package com.hanyang.belieme.demoserver;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.nio.charset.StandardCharsets;

@Entity
public class ItemTypeDB {
    @Id @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;
    private String name;
    private int emojiByte;
    private int count;
    private int amount;

    public ItemTypeDB() {
    }

    public ItemTypeDB(int id, String name, int emojiByte, int count, int amount) {
        this.id = id;
        this.name = name;
        this.emojiByte = emojiByte;
        this.count = count;
        this.amount = amount;
    }

    public ItemTypeDB(String name, int emojiByte, int count, int amount) {
        this.name = name;
        this.emojiByte = emojiByte;
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

    public int getEmojiByte() {
        return emojiByte;
    }

    public void setEmojiByte(int emojiByte) {
        this.emojiByte = emojiByte;
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

    public ItemType toItemType() {
        byte[] arr = getByteArrayFromInt(emojiByte);
        return new ItemType(id, name, new String(arr, StandardCharsets.UTF_8), count, amount);
    }

    byte[] getByteArrayFromInt(int value) {
        return new byte[] {
                (byte)(value >> 24),
                (byte)(value >> 16),
                (byte)(value >> 8),
                (byte)value };
    }
}
