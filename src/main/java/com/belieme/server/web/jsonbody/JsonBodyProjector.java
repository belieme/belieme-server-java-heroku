package com.belieme.server.web.jsonbody;

import java.util.List;
import java.util.Map;

import com.belieme.server.domain.department.*;
import com.belieme.server.domain.event.*;
import com.belieme.server.domain.item.*;
import com.belieme.server.domain.major.*;
import com.belieme.server.domain.thing.*;
import com.belieme.server.domain.university.*;
import com.belieme.server.domain.user.*;
import com.belieme.server.domain.permission.*;

import com.belieme.server.domain.exception.*;

// TODO try catch로 InternalDataBaseException으로 바꾼거 NotFoundOnDataBaseException로 다시 바꾸기

public class JsonBodyProjector {
    private UniversityDao univDao;
    private DepartmentDao deptDao;
    private MajorDao majorDao;
    private UserDao userDao;
    private PermissionDao permissionDao;
    private ThingDao thingDao;
    private ItemDao itemDao;
    private EventDao eventDao;
    
    public JsonBodyProjector(UniversityDao univDao, DepartmentDao deptDao, MajorDao majorDao, UserDao userDao, PermissionDao permissionDao, ThingDao thingDao, ItemDao itemDao, EventDao eventDao) {
        this.univDao = univDao;
        this.deptDao = deptDao;
        this.majorDao = majorDao;
        this.userDao = userDao;
        this.permissionDao = permissionDao;
        this.thingDao = thingDao;
        this.itemDao = itemDao;
        this.eventDao = eventDao;
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
    
    public DepartmentJsonBody toDepartmentJsonBody(DepartmentDto deptDto) throws InternalDataBaseException {
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
    
    private List<MajorDto> getMajorDtoList(DepartmentDto deptDto) throws InternalDataBaseException {
        return majorDao.findAllByUnivCodeAndDeptCode(deptDto.getUnivCode(), deptDto.getCode());
    }
    
    public MajorJsonBody toMajorJsonBody(MajorDto majorDto) throws InternalDataBaseException, NotFoundOnDataBaseException {
        if(majorDto == null) {
            return null;
        }
        MajorJsonBody output = new MajorJsonBody();
        
        output.setCode(majorDto.getCode());
        
        DepartmentDto dept = getDeptDto(majorDto);
        output.setDept(toDepartmentJsonBody(dept));
        
        return output;
    }
    
    private DepartmentDto getDeptDto(MajorDto majorDto) throws InternalDataBaseException, NotFoundOnDataBaseException { 
        return deptDao.findByUnivCodeAndDeptCode(majorDto.getUnivCode(), majorDto.getDeptCode());
    }
    
    public UserJsonBody toUserJsonBody(UserDto userDto) throws InternalDataBaseException, NotFoundOnDataBaseException {
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
    
    private UniversityDto getUnivDto(UserDto userDto) throws InternalDataBaseException, NotFoundOnDataBaseException {
        return univDao.findByCode(userDto.getUnivCode());
    }
    
    public UserJsonBodyWithoutToken toUserJsonBodyWithoutToken(UserDto userDto) throws InternalDataBaseException, NotFoundOnDataBaseException {
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
    
    public ThingJsonBody toThingJsonBody(ThingDto thingDto) throws InternalDataBaseException, NotFoundOnDataBaseException {
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
    
    private int getAmount(ThingDto thing) throws InternalDataBaseException, NotFoundOnDataBaseException {
        int amount = 0;
        
        List<ItemDto> items = getItems(thing);
        for(int i = 0; i < items.size(); i++) {
            if(getStatus(items.get(i)) == ItemStatus.UNUSABLE || getStatus(items.get(i)) == ItemStatus.USABLE) {
                amount++;
            }
        }
        
        return amount;
    }
    
    private int getCount(ThingDto thing) throws InternalDataBaseException, NotFoundOnDataBaseException {
        int count = 0;
        List<ItemDto> items = getItems(thing);
        for(int i = 0; i < items.size(); i++) {
            if(getStatus(items.get(i)) == ItemStatus.USABLE) {
                count++;
            }
        }
        return count;
    }
    
    private List<ItemDto> getItems(ThingDto thing) throws InternalDataBaseException {
        String univCode = thing.getUnivCode();
        String deptCode = thing.getDeptCode();
        String thingCode = thing.getCode();
        
        List<ItemDto> items = itemDao.findByUnivCodeAndDeptCodeAndThingCode(univCode, deptCode, thingCode);
        return items;
    }
    
    private ThingStatus getStatus(ThingDto thing) throws InternalDataBaseException, NotFoundOnDataBaseException {
        ThingStatus status;
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
        
        if(amount == 0) { // 여기도 생각할 여지가 필요할 듯, TODO deactivate 만들 때 쓰기
            status = ThingStatus.INACTIVATE;
        }
        else if(count == 0) {
            status = ThingStatus.UNUSABLE;
        }
        else if(amount >= count) {
            status = ThingStatus.USABLE;
        }
        else {
            throw new InternalDataBaseException("JsonBodyProjector.getStatus()");
        }
        return status;
    }
    
    private ItemStatus getStatus(ItemDto itemDto) throws InternalDataBaseException, NotFoundOnDataBaseException {
        int lastEventNum = itemDto.getLastEventNum();
        
        if(lastEventNum == 0) {
            return ItemStatus.USABLE;
        }
        
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
        } else {
            throw new InternalDataBaseException("JsonBodyProjector.getStatus()");
        }
    }
    
    public ThingJsonBodyWithItems toThingJsonBodyWithItems(ThingDto thingDto) throws InternalDataBaseException {
        if(thingDto == null) {
            return null;
        }
        ThingJsonBodyWithItems output = new ThingJsonBodyWithItems();
        
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
        
        List<ItemDto> itemDtoes = getItems(thingDto);
        for(int i = 0; i < itemDtoes.size(); i++) {
            output.addItem(toItemJsonBody(itemDtoes.get(i)));
        }
             
        return output;
    }
    
    public ItemJsonBody toItemJsonBody(ItemDto itemDto) throws InternalDataBaseException, NotFoundOnDataBaseException {
        if(itemDto == null) {
            return null;
        }
        ItemJsonBody output = new ItemJsonBody();
        output.setNum(itemDto.getNum());
        output.setLastEvent(toEventJsonBodyNestedToItem(getLastEvent(itemDto)));
        output.setStatus(getStatus(itemDto).name());
        
        return output;
    }
    
    private EventDto getLastEvent(ItemDto itemDto) throws InternalDataBaseException, NotFoundOnDataBaseException {
        if(itemDto.getLastEventNum() == 0) {
            return null;
        }
        return eventDao.findByUnivCodeAndDeptCodeAndThingCodeAndItemNumAndEventNum(itemDto.getUnivCode(), itemDto.getDeptCode(), itemDto.getThingCode(), itemDto.getNum(), itemDto.getLastEventNum());   
    }
    
    private EventJsonBodyNestedToItem toEventJsonBodyNestedToItem(EventDto eventDto) throws InternalDataBaseException, NotFoundOnDataBaseException {
        if(eventDto == null) {
            return null;
        }
        
        EventJsonBodyNestedToItem output = new EventJsonBodyNestedToItem();
        output.setNum(eventDto.getNum());
        
        UserDto user = getUser(eventDto);
        UserDto approveManager = getApproveManager(eventDto);
        UserDto returnManager = getReturnManager(eventDto);
        UserDto lostManager = getLostManager(eventDto);
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
    
    private UserDto getUser(EventDto eventDto) throws InternalDataBaseException, NotFoundOnDataBaseException {
        if(eventDto.getUserStudentId() == null) {
            return null;
        }
        return userDao.findByUnivCodeAndStudentId(eventDto.getUnivCode(), eventDto.getUserStudentId());    
    }
    
    private UserDto getApproveManager(EventDto eventDto) throws InternalDataBaseException, NotFoundOnDataBaseException {
        if(eventDto.getApproveManagerStudentId() == null) {
            return null;
        }
        return userDao.findByUnivCodeAndStudentId(eventDto.getUnivCode(), eventDto.getApproveManagerStudentId());          
    }
    
    private UserDto getReturnManager(EventDto eventDto) throws InternalDataBaseException, NotFoundOnDataBaseException {
        if(eventDto.getReturnManagerStudentId() == null) {
            return null;
        }
        return userDao.findByUnivCodeAndStudentId(eventDto.getUnivCode(), eventDto.getReturnManagerStudentId());
    }
    
    private UserDto getLostManager(EventDto eventDto) throws InternalDataBaseException, NotFoundOnDataBaseException {
        if(eventDto.getLostManagerStudentId() == null) {
            return null;
        }
        return userDao.findByUnivCodeAndStudentId(eventDto.getUnivCode(), eventDto.getLostManagerStudentId());
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
    
    public PermissionJsonBody toPermissionJsonBody(PermissionDto permissionDto) throws InternalDataBaseException, NotFoundOnDataBaseException {
        if(permissionDto == null) {
            return null;
        }
        
        PermissionJsonBody output = new PermissionJsonBody();
        output.setPermission(permissionDto.getPermission().name());
        
        DepartmentDto dept = getDeptDto(permissionDto);
        output.setDepartment(toDepartmentJsonBody(dept));
        
        return output;
    }
    
    private DepartmentDto getDeptDto(PermissionDto permissionDto) throws InternalDataBaseException, NotFoundOnDataBaseException {
        return deptDao.findByUnivCodeAndDeptCode(permissionDto.getUnivCode(), permissionDto.getDeptCode());
    }
        
    public EventJsonBody toEventJsonBody(EventDto eventDto) throws InternalDataBaseException, NotFoundOnDataBaseException {
        if(eventDto == null) {
            return null;
        }
        
        EventJsonBody output = new EventJsonBody();
        
        ThingDto thing = getThingDto(eventDto);
        ItemDto item = getItemDto(eventDto);
        output.setThing(toThingJsonBodyNestedToEvent(thing));
        output.setItem(toItemJsonBodyNestedToEvent(item));
        
        output.setNum(eventDto.getNum());
        
        UserDto user = getUser(eventDto);
        UserDto approveManager = getApproveManager(eventDto);
        UserDto returnManager = getReturnManager(eventDto);
        UserDto lostManager = getLostManager(eventDto);
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
    
    private ThingDto getThingDto(EventDto eventDto) throws InternalDataBaseException, NotFoundOnDataBaseException {
        return thingDao.findByUnivCodeAndDeptCodeAndCode(eventDto.getUnivCode(), eventDto.getDeptCode(), eventDto.getThingCode());
        
    }
    
    private ItemDto getItemDto(EventDto eventDto) throws InternalDataBaseException, NotFoundOnDataBaseException {
        return itemDao.findByUnivCodeAndDeptCodeAndThingCodeAndNum(eventDto.getUnivCode(), eventDto.getDeptCode(), eventDto.getThingCode(), eventDto.getItemNum());
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
    
    private ItemJsonBodyNestedToEvent toItemJsonBodyNestedToEvent(ItemDto itemDto) throws InternalDataBaseException {
        if(itemDto == null) {
            return null;
        }
        
        ItemJsonBodyNestedToEvent output = new ItemJsonBodyNestedToEvent();
        
        output.setNum(itemDto.getNum());
        output.setCurrentStatus(getStatus(itemDto).name());
        
        return output;
    }
}