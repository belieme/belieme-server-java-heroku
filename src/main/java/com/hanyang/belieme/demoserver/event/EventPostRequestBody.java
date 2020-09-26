package com.hanyang.belieme.demoserver.event;

public class EventPostRequestBody {
    private Integer thingId;
    private Integer itemNum;
    
    public Integer getThingId() {
        return thingId;
    }
    
    public Integer getItemNum() {
        return itemNum;
    }
    
    public void setThingId(Integer thingId) {
        this.thingId = thingId;
    }
    
    public void setItemNum(Integer itemNum) {
        this.itemNum = itemNum;
    }
}