package com.hanyang.belieme.demoserver.thing;

import javax.persistence.*;
import java.nio.charset.StandardCharsets;

@Entity
public class ThingDB {
    @Id @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;
    private String name;
    private int emojiByte;

    public ThingDB() {
    }

    public ThingDB(int id, String name, int emojiByte) {
        this.id = id;
        this.name = name;
        this.emojiByte = emojiByte;
    }

    public ThingDB(String name, int emojiByte) {
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


    public Thing toThing() {
        byte[] arr = getByteArrayFromInt(emojiByte);
        return new Thing(id, name, new String(arr, StandardCharsets.UTF_8));
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

        byte[] output = new byte[byteLength];
        shiftLength = 24;
        for(int i = 0; i < byteLength; i++) {
            output[i] = (byte)(value >> shiftLength);
            shiftLength -= 8;
        }
        return output;
    }
}
