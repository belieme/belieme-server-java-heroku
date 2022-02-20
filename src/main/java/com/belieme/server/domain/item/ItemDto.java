package com.belieme.server.domain.item;

public class ItemDto {
    private String univCode;
    private String deptCode;
    private String thingCode;
    private int num;
    
    private int lastHistoryNum;

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

    public int getLastHistoryNum() {
        return lastHistoryNum;
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
    
    public void setLastHistoryNum(int lastHistoryNum) {
        this.lastHistoryNum = lastHistoryNum;
    }
}