package com.belieme.server.domain.item;

public class ItemDto {
    private String univCode;
    private String deptCode;
    private String thingCode;
    private int num;
    
    private int lastEventNum;

    public ItemDto() {
    }
    
    public String getUnivCode() {
        return univCode;
    }
    
    public String getDeptCode() {
        return deptCode;
    }
    
    public String getThingCode() {
        return thingCode;
    }
    
    public int getNum() {
        return num;
    }

    public int getLastEventNum() {
        return lastEventNum;
    }
    
    public void setUnivCode(String univCode) {
        this.univCode = univCode;
    }
    
    public void setDeptCode(String deptCode) {
        this.deptCode = deptCode;
    }
    
    public void setThingCode(String thingCode) {
        this.thingCode = thingCode;
    }
    
    public void setNum(int num) {
        this.num = num;
    }
    
    public void setLastEventNum(int lastEventNum) {
        this.lastEventNum = lastEventNum;
    }
}