package com.hanyang.belieme.demoserver.item;

import javax.persistence.*;

import java.util.List;
import java.util.Optional;

import com.hanyang.belieme.demoserver.user.UserRepository;
import com.hanyang.belieme.demoserver.event.*;
import com.hanyang.belieme.demoserver.exception.HttpException;
import com.hanyang.belieme.demoserver.exception.InternalServerErrorException;
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
    
    public Item toItem(UserRepository userRepository, EventRepository eventRepository) {
        Item output = new Item();
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
        output.setStatus(status);
        output.setLastEvent(lastEvent);
        output.setThingId(thingId);
        
        return output;
    }
    
    public ItemNestedToThing toItemNestedToThing(EventRepository eventRepository, UserRepository userRepository) {
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
    
    public ItemNestedToEvent toItemNestedToEvent(EventRepository eventRepository) {
        ItemNestedToEvent output = new ItemNestedToEvent();
        String status;   
        
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

        output.setId(id);
        output.setNum(num);
        output.setCurrentStatus(status);
        
        return output;
    }
    
    public static ItemDB findByThingIdAndNum(ItemRepository itemRepository, int thingId, int num) throws HttpException {
        List<ItemDB> itemListByThingIdAndNum = itemRepository.findByThingIdAndNum(thingId, num);
        if(itemListByThingIdAndNum.size() == 0) {
            throw new NotFoundException("물품 id가 " + thingId + "이고, 물건 번호가 " + num + "인 물건을 찾을 수 없습니다.");
        } else if(itemListByThingIdAndNum.size() != 1) { //Warning 으로 바꿀까?? 그건 좀 귀찮긴 할 듯
            throw new InternalServerErrorException("물품 id가 " + thingId + "이고, 물건 번호가 " + num + "인 물건이 서버에 2개 이상 존재합니다.");
        }
        return itemListByThingIdAndNum.get(0);
    }
}