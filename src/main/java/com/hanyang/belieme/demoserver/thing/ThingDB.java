package com.hanyang.belieme.demoserver.thing;

import javax.persistence.*;

import com.hanyang.belieme.demoserver.event.EventRepository;
import com.hanyang.belieme.demoserver.exception.NotFoundException;
import com.hanyang.belieme.demoserver.item.ItemDB;
import com.hanyang.belieme.demoserver.item.ItemNestedToThing;
import com.hanyang.belieme.demoserver.item.ItemRepository;
import com.hanyang.belieme.demoserver.user.UserRepository;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;

@Entity
public class ThingDB {
    @Id @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;
    private String name;
    private int emojiByte;
    private String description;
    
    private int departmentId; /****/

    public ThingDB() {
    }

    public ThingDB(int id, String name, int emojiByte, String description, int departmentId) {
        this.id = id;
        this.name = name;
        this.emojiByte = emojiByte;
        this.description = description;
        this.departmentId = departmentId;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }
    
    public int getEmojiByte() {
        return emojiByte;
    }
    
    public String getDescription() {
        return description;
    }
    
    public int getDepartmentId() {
        return departmentId;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setEmojiByte(int emojiByte) {
        this.emojiByte = emojiByte;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public void setDepartmentId(int departmentId) {
        this.departmentId = departmentId;
    }

    public byte[] getByteArrayFromInt(int value) {
        int byteLength = 4;
        int shiftLength = 24;
        for(int i = 0; i < 4; i++) {
            if(((byte)(value >> shiftLength)) == 0) {
                byteLength = i;
                break;
            }
            shiftLength -= 8;
        }

        byte[] output = new byte[byteLength];
        shiftLength = 24;
        for(int i = 0; i < byteLength; i++) {
            output[i] = (byte)(value >> shiftLength);
            shiftLength -= 8;
        }
        return output;
    }

    public Thing toThing(UserRepository userRepository, ItemRepository itemRepository, EventRepository eventRepository) {
        Thing output = new Thing();
        
        int amount = 0;
        int count = 0;
        String status = "ERROR";
        List<ItemDB> items = itemRepository.findByThingId(id);
        for(int i = 0; i < items.size(); i++) {
            ItemNestedToThing tmp = items.get(i).toItemNestedToThing(eventRepository, userRepository);
            if(tmp.getStatus().equals("UNUSABLE")) {
                amount++;
            }
            else if(tmp.getStatus().equals("USABLE")) {
                amount++;
                count++;
            }
        }
        if(amount == 0) { // 여기도 생각할 여지가 필요할 듯, TODO deactivate 만들 때 쓰기
            status = "INACTIVE";
        }
        else if(count == 0) {
            status = "UNUSABLE";
        }
        else if(amount >= count) {
            status = "USABLE";
        }
        else {
            status = "ERROR";
        }
        output.setId(id);
        output.setName(name);
        if(emojiByte == 0) {
            output.setEmoji(null);
        } else {
            output.setEmoji(new String(getByteArrayFromInt(emojiByte), StandardCharsets.UTF_8));
        }
        output.setDescription(description);
        output.setAmount(amount);
        output.setCount(count);
        output.setStatus(status);
        output.setDeptId(departmentId);
        return output;
    }
    
    public ThingNestedToEvent toThingNestedToEvent() {
        ThingNestedToEvent output = new ThingNestedToEvent();
        
        output.setId(id);
        output.setName(name);
        output.setEmoji(new String(getByteArrayFromInt(emojiByte), StandardCharsets.UTF_8));
        output.setDescription(description);
        output.setDeptId(departmentId);
        
        return output;
    }
    
    public ThingWithItems toThingWithItems(UserRepository userRepository, ItemRepository itemRepository, EventRepository eventRepository) {
        ThingWithItems output = new ThingWithItems();
        
        List<ItemDB> itemListByThingId = itemRepository.findByThingId(id);
        int amount = 0;
        int count = 0;
        String status = "ERROR";
        
        for(int i = 0; i < itemListByThingId.size(); i++) {
            ItemNestedToThing tmp = itemListByThingId.get(i).toItemNestedToThing(eventRepository, userRepository);
            output.getItems().add(tmp);
            if(tmp.getStatus().equals("UNUSABLE")) {
                amount++;
            }
            else if(tmp.getStatus().equals("USABLE")) {
                amount++;
                count++;
            }
        }
        if(amount == 0) { // 여기도 생각할 여지가 필요할 듯, TODO deactivate 만들 때 쓰기
            status = "INACTIVE";
        }
        else if(count == 0) {
            status = "UNUSABLE";
        }
        else if(amount >= count) {
            status = "USABLE";
        }
        else {
            status = "ERROR";
        }
        
        output.setId(id);
        output.setName(name);
        output.setEmoji(new String(getByteArrayFromInt(emojiByte), StandardCharsets.UTF_8));
        output.setAmount(amount);
        output.setCount(count);
        output.setStatus(status);
        output.setDescription(description);
        output.setDeptId(departmentId);
        
        return output;
    }
    
    public static ThingDB findByThingIdAndDeptId(ThingRepository thingRepository, int thingId, int deptId) throws NotFoundException {
        Optional<ThingDB> targetThingOptional = thingRepository.findById(thingId);
        if(!targetThingOptional.isPresent()) {
            throw new NotFoundException(thingId + "를 id로 갖는 물건을 찾을 수 없습니다.");
        } else if(targetThingOptional.get().getDepartmentId() != deptId) {
            throw new NotFoundException(thingId + "를 id로 갖는 물건은 이 학과의 물건이 아닙니다."); //TODO Exception바꿀까?
        }
        return targetThingOptional.get();
    }
}
