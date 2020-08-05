package com.hanyang.belieme.demoserver.item;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;

@Embeddable
public class ItemPK implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    @Column(name="type_id", nullable=false)
    private int typeId;
    
    @Column(name="num", nullable=false)
    private int num;
    
    public ItemPK() {
    }
    
    public ItemPK(int typeId, int num) {
        super();
        this.typeId = typeId;
        this.num = num;
    }
    
    public int getTypeId() {
        return typeId;
    }
    
    public int getNum() {
        return num;
    }
    
    public void setTypeId(int typeId) {
        this.typeId = typeId;
    }
    
    public void setNum(int num) {
        this.num = num;
    }
}