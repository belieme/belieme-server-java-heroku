package com.hanyang.belieme.demoserver.item;

import javax.persistence.*;

import java.util.Optional;

import com.hanyang.belieme.demoserver.thing.*;
import com.hanyang.belieme.demoserver.event.*;


@Entity
public class Item {
    @Id @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;
    private int thingId;
    private int num;
    
    private int lastEventId;

    @Transient
    private String status;

    @Transient
    private ThingNestedToItem thing;
    
    @Transient
    private EventNestedToItem lastEvent;

    public Item() {
    }

    public Item(int thingId, int num) {
        this.thingId = thingId;
        this.num = num;
        this.lastEventId = -1;
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

    public void setThingId(int thingId) {
        this.thingId = thingId;
    }

    public void setNum(int num) {
        this.num = num;
    }

    public void setLastEventId(int lastEventId) {
        this.lastEventId = lastEventId;
    }

    public void setThing(Thing thing) {
        if(thing == null) {
            this.thing = null;
        } else {
            this.thing = thing.toThingNestedToItem();
        }
    }
    
    public void setLastEvent(Event event) {
        if(event == null) {
            this.lastEvent = null;
        } else {
            this.lastEvent = event.toEventNestedToItem();
        }
    }
    
    public int thingIdGetter() {
        return thingId;
    }
    
    public int lastEventIdGetter() {
        return lastEventId;
    }

    //대상이 저장된 정보 뿐만 아니라 다른 table로부터 derived 된 정보까 추가 하는 메소드(ex status ... )
    public void addInfo(ThingRepository thingRepository, EventRepository eventRepository) {
        Optional<Event> lastEventOptional = eventRepository.findById(lastEventId);
        if(lastEventOptional.isPresent()) {
            String lastEventStatus = lastEventOptional.get().getStatus();
            if(lastEventStatus.equals("EXPIRED")||lastEventStatus.equals("RETURNED")||lastEventStatus.equals("FOUND")||lastEventStatus.equals("FOUNDANDRETURNED")) {
                status = "USABLE";
            }
            else if (lastEventStatus.equals("LOST")){
                status = "INACTIVATE";
            } else {
                status = "UNUSABLE";
            }
            setLastEvent(lastEventOptional.get());
        }
        else {
            status = "USABLE";
            setLastEvent(null);
        }

        Optional<ThingDB> thingDBOptional = thingRepository.findById(thingIdGetter());
        if(thingDBOptional.isPresent()) {
            setThing(thingDBOptional.get().toThing());
        } else {
            setThing(null);
        }
    }
    
    public ItemNestedToThing toItemNestedToThing() {
        ItemNestedToThing output = new ItemNestedToThing();
        output.setId(id);
        output.setNum(num);
        output.setLastEvent(lastEvent);
        output.setStatus(status);
        return output;
    }
    
    public ItemNestedToEvent toItemNestedToEvent() {
        ItemNestedToEvent output = new ItemNestedToEvent();
        output.setId(id);
        output.setNum(num);
        output.setThing(thing);
        output.setCurrentStatus(status);
        
        return output;
    }
}