package com.hanyang.belieme.demoserver.item;

import javax.persistence.*;

import java.util.Optional;

import com.hanyang.belieme.demoserver.thing.*;
import com.hanyang.belieme.demoserver.university.UniversityRepository;
import com.hanyang.belieme.demoserver.user.UserRepository;
import com.hanyang.belieme.demoserver.department.DepartmentRepository;
import com.hanyang.belieme.demoserver.department.major.MajorRepository;
import com.hanyang.belieme.demoserver.event.*;
import com.hanyang.belieme.demoserver.exception.NotFoundException;


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

    
    public Item toItem(UniversityRepository universityRepository, DepartmentRepository departmentRepository, MajorRepository majorRepository, UserRepository userRepository, ThingRepository thingRepository, EventRepository eventRepository) throws NotFoundException {
        Item output = new Item();
        String status;
        ThingNestedToItem thing = null;    
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
            lastEvent = lastEventOptional.get().toEventNestedToItem(userRepository);
        }
        else {
            status = "USABLE";
            lastEvent = null;
        }

        Optional<ThingDB> thingDBOptional = thingRepository.findById(getThingId());
        if(thingDBOptional.isPresent()) {
            thing = thingDBOptional.get().toThingNestedToItem(universityRepository, departmentRepository, majorRepository);
        } else {
            thing = null;
        }
        
        output.setId(id);
        output.setNum(num);
        output.setThing(thing);
        output.setStatus(status);
        output.setLastEvent(lastEvent);
        
        System.out.println(thing);
        System.out.println(output.getThing().toString());
        
        return output;
    }
    
    public ItemNestedToThing toItemNestedToThing(EventRepository eventRepository, UserRepository userRepository) throws NotFoundException {
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
            lastEvent = lastEventOptional.get().toEventNestedToItem(userRepository);
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
    
    public ItemNestedToEvent toItemNestedToEvent(UniversityRepository universityRepository, DepartmentRepository departmentRepository, MajorRepository majorRepository, ThingRepository thingRepository, EventRepository eventRepository) {
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
            thing = thingDBOptional.get().toThingNestedToItem(universityRepository, departmentRepository, majorRepository);  
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