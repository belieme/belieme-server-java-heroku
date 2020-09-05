package com.hanyang.belieme.demoserver.item;

import com.hanyang.belieme.demoserver.thing.*;

import java.util.List;

import com.hanyang.belieme.demoserver.event.*;
import com.hanyang.belieme.demoserver.exception.NotFoundException;
import com.hanyang.belieme.demoserver.exception.WrongInDataBaseException;

public class Item {
    private int id;
    private int num;

    private String status;

    private ThingNestedToItem thing;
    
    private EventNestedToItem lastEvent;

    public Item() {
    }
    
    public int getId() {
        return id;
    }

    public int getNum() {
        return num;
    }

    public String getStatus() {
        return status;
    }

    public ThingNestedToItem getThing() {
        return thing;
    }
    
    public EventNestedToItem getLastEvent() {
        return lastEvent;
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
    public void setNum(int num) {
        this.num = num;
    }
    
    public void setThing(ThingNestedToItem othThing) { // TODO 무엇이 문제인가...
        this.thing = new ThingNestedToItem(othThing);   
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public void setLastEvent(EventNestedToItem lastEvent) {
        if(lastEvent != null) {
            this.lastEvent = new EventNestedToItem(lastEvent);
        } else {
            this.lastEvent = null;
        }
    }
    
    public ItemDB toItemDB() {
        ItemDB output = new ItemDB();
        output.setId(id);
        output.setNum(num);
        output.setThingId(thing.getId());
        output.setLastEventId(lastEvent.getId());
        
        return output;
    }
    
    public static int findItemIdByThingIdAndItemNum(ItemRepository itemRepository, int thingId, int itemNum) throws NotFoundException, WrongInDataBaseException {
        List<ItemDB> itemListByThingIdAndNum = itemRepository.findByThingIdAndNum(thingId, itemNum);
        if(itemListByThingIdAndNum.size() == 0) {
            throw new NotFoundException();
        } else if(itemListByThingIdAndNum.size() != 1) { //Warning 으로 바꿀까?? 그건 좀 귀찮긴 할 듯
            throw new WrongInDataBaseException();
        }
        return itemListByThingIdAndNum.get(0).getId();
    }
}