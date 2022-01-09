
package com.belieme.server.web.common;

import java.util.List;
import java.util.Map;

import com.belieme.server.web.jsonbody.*;
import com.belieme.server.web.exception.*;

import com.belieme.server.domain.exception.*;

import com.belieme.server.domain.department.*;
import com.belieme.server.domain.event.*;
import com.belieme.server.domain.item.*;
import com.belieme.server.domain.major.*;
import com.belieme.server.domain.thing.*;
import com.belieme.server.domain.university.*;
import com.belieme.server.domain.user.*;
import com.belieme.server.domain.permission.*;

public class JsonBodyProjector {
    private DataAdapter dataAdapter;
    
    public JsonBodyProjector(DataAdapter dataAdapter) {
        this.dataAdapter = dataAdapter;
    }
    
    public UniversityJsonBody toUniversityJsonBody(UniversityDto univDto) {
        if(univDto == null) {
            return null;
        }
        
        UniversityJsonBody output = new UniversityJsonBody();
        output.setCode(univDto.getCode());
        output.setName(univDto.getName());
        output.setApiUrl(univDto.getApiUrl());
        
        return output;
    }
    
    public DepartmentJsonBody toDepartmentJsonBody(DepartmentDto deptDto) throws InternalServerErrorException {
        if(deptDto == null) {
            return null;
        }
        
        DepartmentJsonBody output = new DepartmentJsonBody();
        output.code = deptDto.getCode();
        output.name = deptDto.getName();
        output.available = deptDto.isAvailable();
        
        List<MajorDto> majorList = getMajorDtoList(deptDto);
        for(int i = 0; i < majorList.size(); i++) {
            output.majorCodes.add(majorList.get(i).getCode());
        }
        
        return output;
    }
    
    private List<MajorDto> getMajorDtoList(DepartmentDto deptDto) throws InternalServerErrorException {
        return dataAdapter.findAllMajorsByUnivCodeAndDeptCode(deptDto.getUnivCode(), deptDto.getCode());    
    }
    
    public MajorJsonBody toMajorJsonBody(MajorDto majorDto) throws NotFoundException, InternalServerErrorException {
        if(majorDto == null) {
            return null;
        }
        
        MajorJsonBody output = new MajorJsonBody();
        output.setCode(majorDto.getCode());
        
        DepartmentDto dept = getDeptDto(majorDto);
        output.setDept(toDepartmentJsonBody(dept));
        
        return output;
    }
    
    private DepartmentDto getDeptDto(MajorDto majorDto) throws NotFoundException, InternalServerErrorException {
        return dataAdapter.findDeptByUnivCodeAndDeptCode(majorDto.getUnivCode(), majorDto.getDeptCode());
    }
    
    public UserJsonBody toUserJsonBody(UserDto userDto) throws NotFoundException, InternalServerErrorException {
        if(userDto == null) {
            return null;
        }
        
        UserJsonBody output = new UserJsonBody();
    
        UniversityDto univ = getUnivDto(userDto);
        output.setUniversity(toUniversityJsonBody(univ));
        output.setStudentId(userDto.getStudentId());
        output.setName(userDto.getName());
        output.setEntranceYear(userDto.getEntranceYear());
        output.setCreateTimeStamp(userDto.getCreateTimeStamp());
        output.setApprovalTimeStamp(userDto.getApprovalTimeStamp());
        output.setToken(userDto.getToken());
        
        Map<String, Permissions> permissions = userDto.getPermissions();
        for (Map.Entry<String, Permissions> entry : permissions.entrySet()) {
            output.addPermission(entry.getKey(), entry.getValue().toString());
        }
        
        return output;
    }
    
    private UniversityDto getUnivDto(UserDto userDto) throws NotFoundException, InternalServerErrorException {
        return dataAdapter.findUnivByCode(userDto.getUnivCode());
    }
    
    public UserJsonBodyWithoutToken toUserJsonBodyWithoutToken(UserDto userDto) throws NotFoundException, InternalServerErrorException {
        if(userDto == null) {
            return null;
        }
        UserJsonBodyWithoutToken output = new UserJsonBodyWithoutToken();
        
        UniversityDto univ = getUnivDto(userDto);
        output.setUniversity(toUniversityJsonBody(univ));
        output.setStudentId(userDto.getStudentId());
        output.setName(userDto.getName());
        output.setEntranceYear(userDto.getEntranceYear());
        output.setCreateTimeStamp(userDto.getCreateTimeStamp());
        output.setApprovalTimeStamp(userDto.getApprovalTimeStamp());
        
        Map<String, Permissions> permissions = userDto.getPermissions();
        for (Map.Entry<String, Permissions> entry : permissions.entrySet()) {
            output.addPermission(entry.getKey(), entry.getValue().toString());
        }
        
        return output;
    }
    
    public ThingJsonBody toThingJsonBody(ThingDto thingDto) throws NotFoundException, InternalServerErrorException {
        if(thingDto == null) {
            return null;
        }
        ThingJsonBody output = new ThingJsonBody();
        
        int amount = getAmount(thingDto);
        int count = getCount(thingDto);
        ThingStatus status = getStatus(thingDto);
        
        output.setAmount(amount);
        output.setCount(count);
        output.setStatus(status.name());
        output.setCode(thingDto.getCode());
        output.setName(thingDto.getName());
        output.setDescription(thingDto.getDescription());
        output.setEmoji(thingDto.getEmoji());
             
        return output;
    }
    
    private int getAmount(ThingDto thing) throws NotFoundException, InternalServerErrorException{
        int amount = 0;
        
        List<ItemDto> items = getItems(thing);
        for(int i = 0; i < items.size(); i++) {
            if(getStatus(items.get(i)) == ItemStatus.UNUSABLE || getStatus(items.get(i)) == ItemStatus.USABLE) {
                amount++;
            }
        }
        
        return amount;
    }
    
    private int getCount(ThingDto thing) throws NotFoundException, InternalServerErrorException {
        int count = 0;
        List<ItemDto> items = getItems(thing);
        for(int i = 0; i < items.size(); i++) {
            if(getStatus(items.get(i)) == ItemStatus.USABLE) {
                count++;
            }
        }
        return count;
    }
    
    private List<ItemDto> getItems(ThingDto thing) throws InternalServerErrorException {
        String univCode = thing.getUnivCode();
        String deptCode = thing.getDeptCode();
        String thingCode = thing.getCode();
       
        return dataAdapter.findAllItemsByUnivCodeAndDeptCodeAndThingCode(univCode, deptCode, thingCode); 
    }
    
    private ThingStatus getStatus(ThingDto thing) throws NotFoundException, InternalServerErrorException {
        int amount = 0;
        int count = 0;
        
        List<ItemDto> items = getItems(thing);
        for(int i = 0; i < items.size(); i++) {
            if(getStatus(items.get(i)) == ItemStatus.UNUSABLE) {
                amount++;
            }
            else if(getStatus(items.get(i)) == ItemStatus.USABLE) {
                amount++;
                count++;
            }
        }
        
        if(amount == 0) { // 여기도 생각할 여지가 필요할 듯, TODO deactivate 만들 때 쓰기기...(9)
            return ThingStatus.INACTIVATE;
        }
        else if(count == 0) {
            return ThingStatus.UNUSABLE;
        }
        else { // amount > count
            return ThingStatus.USABLE;
        }
    }
    
    private ItemStatus getStatus(ItemDto itemDto) throws NotFoundException, InternalServerErrorException {        
        EventDto lastEvent = getLastEvent(itemDto);
        if(lastEvent != null) {
            String lastEventStatus = lastEvent.getStatus();
            if(lastEventStatus.equals("EXPIRED")||lastEventStatus.equals("RETURNED")||lastEventStatus.equals("FOUND")||lastEventStatus.equals("FOUNDANDRETURNED")) {
                return ItemStatus.USABLE;
            }
            else if (lastEventStatus.equals("LOST")){
                return ItemStatus.INACTIVATE;
            } else {
                return ItemStatus.UNUSABLE;
            }    
        } else { // lastEventNum이 0일 때
            return ItemStatus.USABLE;
        }
    }
    
    public ThingJsonBodyWithItems toThingJsonBodyWithItems(ThingDto thingDto) throws NotFoundException, InternalServerErrorException {
        if(thingDto == null) {
            return null;
        }
        
        int amount = getAmount(thingDto);
        int count = getCount(thingDto);
        ThingStatus status = getStatus(thingDto);
        List<ItemDto> itemDtoes = getItems(thingDto);
        
        ThingJsonBodyWithItems output = new ThingJsonBodyWithItems();

        output.setAmount(amount);
        output.setCount(count);
        output.setStatus(status.name());
        output.setCode(thingDto.getCode());
        output.setName(thingDto.getName());
        output.setDescription(thingDto.getDescription());
        output.setEmoji(thingDto.getEmoji());
        
        for(int i = 0; i < itemDtoes.size(); i++) {
            output.addItem(toItemJsonBody(itemDtoes.get(i)));
        }
             
        return output;
    }
    
    public ItemJsonBody toItemJsonBody(ItemDto itemDto) throws NotFoundException, InternalServerErrorException {
        if(itemDto == null) {
            return null;
        }
        
        EventDto lastEvent = getLastEvent(itemDto);
        
        ItemJsonBody output = new ItemJsonBody();
        
        output.setNum(itemDto.getNum());
        output.setLastEvent(toEventJsonBodyNestedToItem(lastEvent));
        output.setStatus(getStatus(itemDto).name());
        
        return output;
    }
    
    private EventDto getLastEvent(ItemDto itemDto) throws NotFoundException, InternalServerErrorException {
        if(itemDto.getLastEventNum() == 0) {
            return null;
        }
        
        return dataAdapter.findEventByUnivCodeAndDeptCodeAndThingCodeAndItemNumAndEventNum(itemDto.getUnivCode(), itemDto.getDeptCode(), itemDto.getThingCode(), itemDto.getNum(), itemDto.getLastEventNum()); 
    }
    
    private EventJsonBodyNestedToItem toEventJsonBodyNestedToItem(EventDto eventDto) throws NotFoundException, InternalServerErrorException {
        if(eventDto == null) {
            return null;
        }
        
        UserDto user = getUser(eventDto);
        UserDto approveManager = getApproveManager(eventDto);
        UserDto returnManager = getReturnManager(eventDto);
        UserDto lostManager = getLostManager(eventDto);
        
        EventJsonBodyNestedToItem output = new EventJsonBodyNestedToItem();
        
        output.setNum(eventDto.getNum());
        output.setUser(toUserJsonBodyNestedToEvent(user));
        output.setApproveManager(toUserJsonBodyNestedToEvent(approveManager));
        output.setReturnManager(toUserJsonBodyNestedToEvent(returnManager));
        output.setLostManager(toUserJsonBodyNestedToEvent(lostManager));
        output.setReserveTimeStamp(eventDto.getReserveTimeStamp());
        output.setApproveTimeStamp(eventDto.getApproveTimeStamp());
        output.setCancelTimeStamp(eventDto.getCancelTimeStamp());
        output.setReturnTimeStamp(eventDto.getReturnTimeStamp());
        output.setLostTimeStamp(eventDto.getLostTimeStamp());
        
        return output;
    }
    
    private UserDto getUser(EventDto eventDto) throws NotFoundException, InternalServerErrorException {
        if(eventDto.getUserStudentId() == null) {
            return null;
        }
        
        return dataAdapter.findUserByUnivCodeAndStudentId(eventDto.getUnivCode(), eventDto.getUserStudentId());
    }
    
    private UserDto getApproveManager(EventDto eventDto) throws NotFoundException, InternalServerErrorException {
        if(eventDto.getApproveManagerStudentId() == null) {
            return null;
        }

        return dataAdapter.findUserByUnivCodeAndStudentId(eventDto.getUnivCode(), eventDto.getApproveManagerStudentId());          
    }
    
    private UserDto getReturnManager(EventDto eventDto) throws NotFoundException, InternalServerErrorException {
        if(eventDto.getReturnManagerStudentId() == null) {
            return null;
        }
        
        return dataAdapter.findUserByUnivCodeAndStudentId(eventDto.getUnivCode(), eventDto.getReturnManagerStudentId());
    }
    
    private UserDto getLostManager(EventDto eventDto) throws NotFoundException, InternalServerErrorException {
        if(eventDto.getLostManagerStudentId() == null) {
            return null;
        }
        
        return dataAdapter.findUserByUnivCodeAndStudentId(eventDto.getUnivCode(), eventDto.getLostManagerStudentId());
    }
    
    private UserJsonBodyNestedToEvent toUserJsonBodyNestedToEvent(UserDto userDto) {
        if(userDto == null) {
            return null;
        }
        
        UserJsonBodyNestedToEvent output = new UserJsonBodyNestedToEvent();
        
        output.setStudentId(userDto.getStudentId());
        output.setName(userDto.getName());
        output.setEntranceYear(userDto.getEntranceYear());
        
        return output;
    }
    
    public PermissionJsonBody toPermissionJsonBody(PermissionDto permissionDto) throws NotFoundException, InternalServerErrorException {
        if(permissionDto == null) {
            return null;
        }
        DepartmentDto dept = getDeptDto(permissionDto);
        
        PermissionJsonBody output = new PermissionJsonBody();
        output.setPermission(permissionDto.getPermission().name());
        output.setDepartment(toDepartmentJsonBody(dept));
        
        return output;
    }
    
    private DepartmentDto getDeptDto(PermissionDto permissionDto) throws NotFoundException, InternalServerErrorException {
        return dataAdapter.findDeptByUnivCodeAndDeptCode(permissionDto.getUnivCode(), permissionDto.getDeptCode());
    }
        
    public EventJsonBody toEventJsonBody(EventDto eventDto) throws NotFoundException, InternalServerErrorException {
        if(eventDto == null) {
            return null;
        }

        ThingDto thing = getThingDto(eventDto);
        ItemDto item = getItemDto(eventDto);
        UserDto user = getUser(eventDto);
        UserDto approveManager = getApproveManager(eventDto);
        UserDto returnManager = getReturnManager(eventDto);
        UserDto lostManager = getLostManager(eventDto);
        
        EventJsonBody output = new EventJsonBody();
        
        output.setNum(eventDto.getNum());
        output.setThing(toThingJsonBodyNestedToEvent(thing));
        output.setItem(toItemJsonBodyNestedToEvent(item));
        output.setUser(toUserJsonBodyNestedToEvent(user));
        output.setApproveManager(toUserJsonBodyNestedToEvent(approveManager));
        output.setReturnManager(toUserJsonBodyNestedToEvent(returnManager));
        output.setLostManager(toUserJsonBodyNestedToEvent(lostManager));
        output.setReserveTimeStamp(eventDto.getReserveTimeStamp());
        output.setApproveTimeStamp(eventDto.getApproveTimeStamp());
        output.setCancelTimeStamp(eventDto.getCancelTimeStamp());
        output.setReturnTimeStamp(eventDto.getReturnTimeStamp());
        output.setLostTimeStamp(eventDto.getLostTimeStamp());
        
        return output;
    }
    
    private ThingDto getThingDto(EventDto eventDto) throws NotFoundException, InternalServerErrorException {
        return dataAdapter.findThingByUnivCodeAndDeptCodeAndThingCode(eventDto.getUnivCode(), eventDto.getDeptCode(), eventDto.getThingCode());
    }
    
    private ItemDto getItemDto(EventDto eventDto) throws NotFoundException, InternalServerErrorException {
        return dataAdapter.findItemByUnivCodeAndDeptCodeAndThingCodeAndItemNum(eventDto.getUnivCode(), eventDto.getDeptCode(), eventDto.getThingCode(), eventDto.getItemNum());
    }
    
    private ThingJsonBodyNestedToEvent toThingJsonBodyNestedToEvent(ThingDto thingDto) {
        if(thingDto == null) {
            return null;
        }
        
        ThingJsonBodyNestedToEvent output = new ThingJsonBodyNestedToEvent();
        
        output.setCode(thingDto.getCode());
        output.setName(thingDto.getName());
        output.setDescription(thingDto.getDescription());
        output.setEmoji(thingDto.getEmoji());
             
        return output;
    }
    
    private ItemJsonBodyNestedToEvent toItemJsonBodyNestedToEvent(ItemDto itemDto) throws NotFoundException, InternalServerErrorException {
        if(itemDto == null) {
            return null;
        }
        
        ItemJsonBodyNestedToEvent output = new ItemJsonBodyNestedToEvent();
        
        output.setNum(itemDto.getNum());
        output.setCurrentStatus(getStatus(itemDto).name());
        
        return output;
    }
}