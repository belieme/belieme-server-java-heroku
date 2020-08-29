package com.hanyang.belieme.demoserver.item;

import javax.persistence.*;

import java.util.Optional;

import com.hanyang.belieme.demoserver.thing.*;
import com.hanyang.belieme.demoserver.university.UniversityRepository;
import com.hanyang.belieme.demoserver.department.DepartmentRepository;
import com.hanyang.belieme.demoserver.event.*;


@Entity
public class ItemDB {
    @Id @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;
    private int thingId;
    private int num;
    
    private int lastEventId;

    public ItemDB() {
    }

    public ItemDB(int thingId, int num) {
        this.thingId = thingId;
        this.num = num;
        this.lastEventId = 0; //TODO Last history id의 default는 0 or -1?
    }
    
    public int getId() {
        return id;
    }

    public int getNum() {
        return num;
    }
    
    public int getThingId() {
        return thingId;
    }
    
    public int getLastEventId() {
        return lastEventId;
    }
    
    public void setId(int id) {
        this.id = id;
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

    
    public Item toItem(UniversityRepository universityRepository, DepartmentRepository departmentRepository, ThingRepository thingRepository, EventRepository eventRepository) {
        Item output = new Item();
        String status;
        ThingNestedToItem thing;    
        EventNestedToItem lastEvent;
        
        Optional<EventDB> lastEventOptional = eventRepository.findById(lastEventId);
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
            lastEvent = lastEventOptional.get().toEventNestedToItem();
        }
        else {
            status = "USABLE";
            lastEvent = null;
        }

        Optional<ThingDB> thingDBOptional = thingRepository.findById(getThingId());
        System.out.println("now!!!!");
        if(thingDBOptional.isPresent()) {
            System.out.println("here!!!!");
            thing = thingDBOptional.get().toThingNestedToItem(universityRepository, departmentRepository);
        } else {
            System.out.println("here??!!");
            thing = null;
        }
        
        output.setId(id);
        output.setNum(num);
        output.setThing(thing);
        output.setStatus(status);
        output.setLastEvent(lastEvent);
        
        System.out.println(output.getThing().toString());
        
        return output;
    }
    
    public ItemNestedToThing toItemNestedToThing(EventRepository eventRepository) {
        ItemNestedToThing output = new ItemNestedToThing();
        
        String status;
        EventNestedToItem lastEvent;
        
        Optional<EventDB> lastEventOptional = eventRepository.findById(lastEventId);
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
            lastEvent = lastEventOptional.get().toEventNestedToItem();
        }
        else {
            status = "USABLE";
            lastEvent = null;
        }

        output.setId(id);
        output.setNum(num);
        output.setLastEvent(lastEvent);
        output.setStatus(status);
        return output;
    }
    
    public ItemNestedToEvent toItemNestedToEvent(UniversityRepository universityRepository, DepartmentRepository departmentRepository, ThingRepository thingRepository, EventRepository eventRepository) {
        ItemNestedToEvent output = new ItemNestedToEvent();
        
        String status;
        ThingNestedToItem thing;    
        
        Optional<EventDB> lastEventOptional = eventRepository.findById(lastEventId);
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
        }
        else {
            status = "USABLE";
        }

        Optional<ThingDB> thingDBOptional = thingRepository.findById(getThingId());
        if(thingDBOptional.isPresent()) {
            thing = thingDBOptional.get().toThingNestedToItem(universityRepository, departmentRepository);  
        } else {
            thing = null;
        }
        output.setId(id);
        output.setNum(num);
        output.setThing(thing);
        output.setCurrentStatus(status);
        
        return output;
    }
}