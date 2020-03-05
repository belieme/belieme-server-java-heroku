package com.hanyang.belieme.demoserver;

import org.springframework.beans.factory.annotation.Autowired;

import javax.persistence.*;
import java.util.Optional;

@Entity
public class Item {

    @Id @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;
    private int typeId;
    private int num;
    private int lastHistoryId;

    @Transient
    private String status;

    @Transient
    private String typeName;

    @Transient
    private String typeEmoji;

    public Item() {
    }

    public Item(int typeId, int num, int lastHistoryId) {
        this.typeId = typeId;
        this.num = num;
        this.lastHistoryId = lastHistoryId;
    }

    public int getId() {
        return id;
    }

    public int getTypeId() {
        return typeId;
    }

    public int getNum() {
        return num;
    }

    public int getLastHistoryId() {
        return lastHistoryId;
    }

    public String getStatus() {
        return status;
    }

    public String getTypeName() {
        return typeName;
    }

    public String getTypeEmoji() {
        return typeEmoji;
    }

    public void setTypeId(int typeId) {
        this.typeId = typeId;
    }

    public void setNum(int num) {
        this.num = num;
    }

    public void setLastHistoryId(int lastHistoryId) {
        this.lastHistoryId = lastHistoryId;
    }

    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }

    public void setTypeEmoji(String typeEmoji) {
        this.typeEmoji = typeEmoji;
    }

    public void usableStatus() {
        status = "USABLE";
    }

    public void unusableStatus() {
        status = "UNUSABLE";
    }
}
