package com.hanyang.belieme.demoserver;

import javax.persistence.*;
import java.nio.charset.StandardCharsets;

@Entity
public class ItemTypeDB {
    @Id @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;
    private String name;
    private int emojiByte;

    public ItemTypeDB() {
    }

    public ItemTypeDB(int id, String name, int emojiByte) {
        this.id = id;
        this.name = name;
        this.emojiByte = emojiByte;
    }

    public ItemTypeDB(String name, int emojiByte) {
        this.name = name;
        this.emojiByte = emojiByte;
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


    public ItemType toItemType() {
        byte[] arr = getByteArrayFromInt(emojiByte);
        return new ItemType(id, name, new String(arr, StandardCharsets.UTF_8), 0, 0);
    }

    public byte[] getByteArrayFromInt(int value) {
        int byteLength = 4;
        int shiftLength = 24;
        for(int i = 0; i < 4; i++) {
            if(((byte)(value >> shiftLength)) == 0) {
                byteLength = i;
                break;
            }
            shiftLength -= 8;
        }

        byte[] result = new byte[byteLength];
        shiftLength = 24;
        for(int i = 0; i < byteLength; i++) {
            result[i] = (byte)(value >> shiftLength);
            shiftLength -= 8;
        }
        return result;
    }
}
