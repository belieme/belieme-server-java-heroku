package com.belieme.server.data.thing;

import javax.persistence.*;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;

@Entity
public class ThingEntity {
    @Id @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;
    private String code;
    private String name;
    private int emojiByte;
    private String description;
    
    private int deptId; 

    public ThingEntity() {
    }

    public int getId() {
        return id;
    }
    
    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }
    
    public String getEmoji() {
        return new String(getByteArrayFromInt(emojiByte), StandardCharsets.UTF_8);
    }
    
    public String getDescription() {
        return description;
    }
    
    public int getDeptId() {
        return deptId;
    }
    
    public void setCode(String code) {
        this.code = code;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setEmoji(String emoji) {
        byte arr[];
        try {
            arr = emoji.getBytes("UTF-8");   
            this.emojiByte = getIntFromByteArray(arr);
        } catch (UnsupportedEncodingException e) {
            this.emojiByte = 0;
            e.printStackTrace();
        }
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public void setDeptId(int deptId) {
        this.deptId = deptId;
    }

    private byte[] getByteArrayFromInt(int value) {
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
    
    private int getIntFromByteArray(byte[] bytes) {
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
