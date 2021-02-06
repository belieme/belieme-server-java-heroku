package com.belieme.server.web.jsonbody;

public class ItemInfoJsonBody {
    private String thingCode;
    private Integer itemNum;
    
    public String getThingCode() {
        return thingCode;
    }
    
    public Integer getItemNum() {
        return itemNum;
    }
    
    public void setThingCode(String thingCode) {
        this.thingCode = thingCode;
    }
    
    public void setItemNum(Integer itemNum) {
        this.itemNum = itemNum;
    }
}