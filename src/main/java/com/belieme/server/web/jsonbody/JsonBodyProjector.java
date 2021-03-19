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
        UniversityJsonBody output = new UniversityJsonBody();
        output.setCode(univDto.getCode());
        output.setName(univDto.getName());
        output.setApiUrl(univDto.getApiUrl());
        
        return output;
    }
    
    public DepartmentJsonBody toDepartmentJsonBody(DepartmentDto departmentDto) throws ServerDomainException {
        DepartmentJsonBody output = new DepartmentJsonBody();
        output.code = departmentDto.getCode();
        output.name = departmentDto.getName();
        output.available = departmentDto.isAvailable();
        
        List<MajorDto> majorList = majorDao.findAllByUnivCodeAndDeptCode(departmentDto.getUnivCode(), departmentDto.getCode());
        for(int i = 0; i < majorList.size(); i++) {
            output.majorCodes.add(majorList.get(i).getCode());
        }
        
        return output;
    }
    
    public MajorJsonBody toMajorJsonBody(MajorDto majorDto) throws ServerDomainException {
        MajorJsonBody output = new MajorJsonBody();
        
        output.setCode(majorDto.getCode());
        
        DepartmentDto dept = deptDao.findByUnivCodeAndDeptCode(majorDto.getUnivCode(), majorDto.getDeptCode());
        output.setDept(toDepartmentJsonBody(dept));
        
        return output;
    }
    
    public UserJsonBody toUserJsonBody(UserDto userDto) throws ServerDomainException {
        UserJsonBody output = new UserJsonBody();
        
        UniversityDto univ = univDao.findByCode(userDto.getUnivCode());
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
    
    public UserJsonBodyWithoutToken toUserJsonBodyWithoutToken(UserDto userDto) throws ServerDomainException {
        UserJsonBodyWithoutToken output = new UserJsonBodyWithoutToken();
        
        UniversityDto univ = univDao.findByCode(userDto.getUnivCode());
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
    
    public ThingJsonBody toThingJsonBody(ThingDto thingDto) throws ServerDomainException {
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
    
    private int getAmount(ThingDto thing) throws ServerDomainException {
        int amount = 0;
        
        List<ItemDto> items = getItems(thing);
        for(int i = 0; i < items.size(); i++) {
            if(getStatus(items.get(i)) == ItemStatus.UNUSABLE || getStatus(items.get(i)) == ItemStatus.USABLE) {
                amount++;
            }
        }
        
        return amount;
    }
    
    private int getCount(ThingDto thing) throws ServerDomainException {
        int count = 0;
        List<ItemDto> items = getItems(thing);
        for(int i = 0; i < items.size(); i++) {
            if(getStatus(items.get(i)) == ItemStatus.USABLE) {
                count++;
            }
        }
        return count;
    }
    
    private List<ItemDto> getItems(ThingDto thing) throws ServerDomainException {
        String univCode = thing.getUnivCode();
        String deptCode = thing.getDeptCode();
        String thingCode = thing.getCode();
        
        List<ItemDto> items = itemDao.findByUnivCodeAndDeptCodeAndThingCode(univCode, deptCode, thingCode);
        return items;
    }
    
    private ThingStatus getStatus(ThingDto thing) throws ServerDomainException {
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
    
    private ItemStatus getStatus(ItemDto itemDto) throws ServerDomainException {
        int lastEventNum = itemDto.getLastEventNum();
        
        if(lastEventNum == 0) { // TODO default 이걸로??
            return ItemStatus.USABLE;
        }
        
        EventDto lastEvent = eventDao.findByUnivCodeAndDeptCodeAndThingCodeAndItemNumAndEventNum(itemDto.getUnivCode(), itemDto.getDeptCode(), itemDto.getThingCode(), itemDto.getNum(), lastEventNum);
        if(lastEvent != null) {
            String lastEventStatus = lastEvent.getStatus();
            if(lastEventStatus.equals("EXPIRED")||lastEventStatus.equals("RETURNED")||lastEventStatus.equals("FOUND")||lastEventStatus.equals("FOUNDANDRETURNED")) {
                return ItemStatus.UNUSABLE;
            }
            else if (lastEventStatus.equals("LOST")){
                return ItemStatus.INACTIVATE;
            } else {
                return ItemStatus.USABLE;
            }    
        } else {
            throw new InternalDataBaseException("JsonBodyProjector.getStatus()");
        }
    }
    
    public ThingJsonBodyWithItems toThingJsonBodyWithItems(ThingDto thingDto) throws ServerDomainException {
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
    
    public ItemJsonBody toItemJsonBody(ItemDto itemDto) throws ServerDomainException {
        ItemJsonBody output = new ItemJsonBody();
        output.setNum(itemDto.getNum());
        output.setLastEvent(toEventJsonBodyNestedToItem(getLastEvent(itemDto)));
        output.setStatus(getStatus(itemDto).name());
        
        return output;
    }
    
    private EventDto getLastEvent(ItemDto itemDto) throws ServerDomainException {
        try {
            return eventDao.findByUnivCodeAndDeptCodeAndThingCodeAndItemNumAndEventNum(itemDto.getUnivCode(), itemDto.getDeptCode(), itemDto.getThingCode(), itemDto.getNum(), itemDto.getLastEventNum());   
        } catch(NotFoundOnDataBaseException e) {
            return null;
        }
    }
    
    private EventJsonBodyNestedToItem toEventJsonBodyNestedToItem(EventDto eventDto) throws ServerDomainException {
        if(eventDto == null) {
            return null;
        }
        
        EventJsonBodyNestedToItem output = new EventJsonBodyNestedToItem();
        output.setNum(eventDto.getNum());
        
        UserDto user = userDao.findByUnivCodeAndStudentId(eventDto.getUnivCode(), eventDto.getUserStudentId());
        UserDto approveManager = userDao.findByUnivCodeAndStudentId(eventDto.getUnivCode(), eventDto.getApproveManagerStudentId());
        UserDto returnManager = userDao.findByUnivCodeAndStudentId(eventDto.getUnivCode(), eventDto.getReturnManagerStudentId());
        UserDto lostManager = userDao.findByUnivCodeAndStudentId(eventDto.getUnivCode(), eventDto.getLostManagerStudentId());
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
    
    private UserJsonBodyNestedToEvent toUserJsonBodyNestedToEvent(UserDto userDto) {
        UserJsonBodyNestedToEvent output = new UserJsonBodyNestedToEvent();
        output.setStudentId(userDto.getStudentId());
        output.setName(userDto.getName());
        output.setEntranceYear(userDto.getEntranceYear());
        
        return output;
    }
    
    public PermissionJsonBody toPermissionJsonBody(PermissionDto permissionDto) throws ServerDomainException {
        PermissionJsonBody output = new PermissionJsonBody();
        output.setPermission(permissionDto.getPermission().name());
        
        DepartmentDto dept = deptDao.findByUnivCodeAndDeptCode(permissionDto.getUnivCode(), permissionDto.getDeptCode());
        output.setDepartment(toDepartmentJsonBody(dept));
        
        return output;
    }
    
    public EventJsonBody toEventJsonBody(EventDto eventDto) throws ServerDomainException {
        EventJsonBody output = new EventJsonBody();
        
        ThingDto thing = thingDao.findByUnivCodeAndDeptCodeAndCode(eventDto.getUnivCode(), eventDto.getDeptCode(), eventDto.getThingCode());
        ItemDto item = itemDao.findByUnivCodeAndDeptCodeAndThingCodeAndNum(eventDto.getUnivCode(), eventDto.getDeptCode(), eventDto.getThingCode(), eventDto.getItemNum());
        output.setThing(toThingJsonBodyNestedToEvent(thing));
        output.setItem(toItemJsonBodyNestedToEvent(item));
        
        output.setNum(eventDto.getNum());
        
        UserDto user = userDao.findByUnivCodeAndStudentId(eventDto.getUnivCode(), eventDto.getUserStudentId());
        UserDto approveManager = userDao.findByUnivCodeAndStudentId(eventDto.getUnivCode(), eventDto.getApproveManagerStudentId());
        UserDto returnManager = userDao.findByUnivCodeAndStudentId(eventDto.getUnivCode(), eventDto.getReturnManagerStudentId());
        UserDto lostManager = userDao.findByUnivCodeAndStudentId(eventDto.getUnivCode(), eventDto.getLostManagerStudentId());
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
    
    private ThingJsonBodyNestedToEvent toThingJsonBodyNestedToEvent(ThingDto thingDto) {
        ThingJsonBodyNestedToEvent output = new ThingJsonBodyNestedToEvent();
        
        output.setCode(thingDto.getCode());
        output.setName(thingDto.getName());
        output.setDescription(thingDto.getDescription());
        output.setEmoji(thingDto.getEmoji());
             
        return output;
    }
    
    private ItemJsonBodyNestedToEvent toItemJsonBodyNestedToEvent(ItemDto itemDto) throws ServerDomainException {
        ItemJsonBodyNestedToEvent output = new ItemJsonBodyNestedToEvent();
        
        output.setNum(itemDto.getNum());
        output.setCurrentStatus(getStatus(itemDto).name());
        
        return output;
    }
}