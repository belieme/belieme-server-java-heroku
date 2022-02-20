
package com.belieme.server.web.common;

import java.util.List;
import java.util.Map;

import com.belieme.server.web.jsonbody.*;
import com.belieme.server.web.exception.*;

import com.belieme.server.domain.exception.*;

import com.belieme.server.domain.department.*;
import com.belieme.server.domain.history.*;
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

    public DepartmentJsonBody toDepartmentJsonBodyForDev(DepartmentDto deptDto) throws InternalServerErrorException {
        if(deptDto == null) {
            return null;
        }

        DepartmentJsonBody output = new DepartmentJsonBody();
        output.code = deptDto.getUnivCode() + "/" + deptDto.getCode();
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
    
    public UserJsonBody toUserJsonBody(UserDto userDto) {
        if(userDto == null) {
            return null;
        }
        
        UserJsonBody output = new UserJsonBody();
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
    
    public UserJsonBodyWithoutToken toUserJsonBodyWithoutToken(UserDto userDto) {
        if(userDto == null) {
            return null;
        }
        UserJsonBodyWithoutToken output = new UserJsonBodyWithoutToken();

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
        HistoryDto lastHistory = getLastHistory(itemDto);
        if(lastHistory != null) {
            String lastHistoryStatus = lastHistory.getStatus();
            if(lastHistoryStatus.equals("EXPIRED")||lastHistoryStatus.equals("RETURNED")||lastHistoryStatus.equals("FOUND")||lastHistoryStatus.equals("FOUNDANDRETURNED")) {
                return ItemStatus.USABLE;
            }
            else if (lastHistoryStatus.equals("LOST")){
                return ItemStatus.INACTIVATE;
            } else {
                return ItemStatus.UNUSABLE;
            }    
        } else { // lastHistoryNum이 0일 때
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
        
        HistoryDto lastHistory = getLastHistory(itemDto);
        
        ItemJsonBody output = new ItemJsonBody();
        
        output.setNum(itemDto.getNum());
        output.setLastHistory(toHistoryJsonBodyNestedToItem(lastHistory));
        output.setStatus(getStatus(itemDto).name());
        
        return output;
    }
    
    private HistoryDto getLastHistory(ItemDto itemDto) throws NotFoundException, InternalServerErrorException {
        if(itemDto.getLastHistoryNum() == 0) {
            return null;
        }
        
        return dataAdapter.findHistoryByUnivCodeAndDeptCodeAndThingCodeAndItemNumAndHistoryNum(itemDto.getUnivCode(), itemDto.getDeptCode(), itemDto.getThingCode(), itemDto.getNum(), itemDto.getLastHistoryNum()); 
    }
    
    private HistoryJsonBodyNestedToItem toHistoryJsonBodyNestedToItem(HistoryDto historyDto) throws NotFoundException, InternalServerErrorException {
        if(historyDto == null) {
            return null;
        }
        
        UserDto user = getUser(historyDto);
        UserDto approveManager = getApproveManager(historyDto);
        UserDto returnManager = getReturnManager(historyDto);
        UserDto lostManager = getLostManager(historyDto);
        
        HistoryJsonBodyNestedToItem output = new HistoryJsonBodyNestedToItem();
        
        output.setNum(historyDto.getNum());
        output.setUser(toUserJsonBodyNestedToHistory(user));
        output.setApproveManager(toUserJsonBodyNestedToHistory(approveManager));
        output.setReturnManager(toUserJsonBodyNestedToHistory(returnManager));
        output.setLostManager(toUserJsonBodyNestedToHistory(lostManager));
        output.setReserveTimeStamp(historyDto.getReserveTimeStamp());
        output.setApproveTimeStamp(historyDto.getApproveTimeStamp());
        output.setCancelTimeStamp(historyDto.getCancelTimeStamp());
        output.setReturnTimeStamp(historyDto.getReturnTimeStamp());
        output.setLostTimeStamp(historyDto.getLostTimeStamp());
        
        return output;
    }
    
    private UserDto getUser(HistoryDto historyDto) throws NotFoundException, InternalServerErrorException {
        if(historyDto.getUserStudentId() == null) {
            return null;
        }
        
        return dataAdapter.findUserByUnivCodeAndStudentId(historyDto.getUnivCode(), historyDto.getUserStudentId());
    }
    
    private UserDto getApproveManager(HistoryDto historyDto) throws NotFoundException, InternalServerErrorException {
        if(historyDto.getApproveManagerStudentId() == null) {
            return null;
        }

        return dataAdapter.findUserByUnivCodeAndStudentId(historyDto.getUnivCode(), historyDto.getApproveManagerStudentId());          
    }
    
    private UserDto getReturnManager(HistoryDto historyDto) throws NotFoundException, InternalServerErrorException {
        if(historyDto.getReturnManagerStudentId() == null) {
            return null;
        }
        
        return dataAdapter.findUserByUnivCodeAndStudentId(historyDto.getUnivCode(), historyDto.getReturnManagerStudentId());
    }
    
    private UserDto getLostManager(HistoryDto historyDto) throws NotFoundException, InternalServerErrorException {
        if(historyDto.getLostManagerStudentId() == null) {
            return null;
        }
        
        return dataAdapter.findUserByUnivCodeAndStudentId(historyDto.getUnivCode(), historyDto.getLostManagerStudentId());
    }
    
    private UserJsonBodyNestedToHistory toUserJsonBodyNestedToHistory(UserDto userDto) {
        if(userDto == null) {
            return null;
        }
        
        UserJsonBodyNestedToHistory output = new UserJsonBodyNestedToHistory();
        
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
        
    public HistoryJsonBody toHistoryJsonBody(HistoryDto historyDto) throws NotFoundException, InternalServerErrorException {
        if(historyDto == null) {
            return null;
        }

        ThingDto thing = getThingDto(historyDto);
        ItemDto item = getItemDto(historyDto);
        UserDto user = getUser(historyDto);
        UserDto approveManager = getApproveManager(historyDto);
        UserDto returnManager = getReturnManager(historyDto);
        UserDto lostManager = getLostManager(historyDto);
        
        HistoryJsonBody output = new HistoryJsonBody();
        
        output.setNum(historyDto.getNum());
        output.setThing(toThingJsonBodyNestedToHistory(thing));
        output.setItem(toItemJsonBodyNestedToHistory(item));
        output.setUser(toUserJsonBodyNestedToHistory(user));
        output.setApproveManager(toUserJsonBodyNestedToHistory(approveManager));
        output.setReturnManager(toUserJsonBodyNestedToHistory(returnManager));
        output.setLostManager(toUserJsonBodyNestedToHistory(lostManager));
        output.setReserveTimeStamp(historyDto.getReserveTimeStamp());
        output.setApproveTimeStamp(historyDto.getApproveTimeStamp());
        output.setCancelTimeStamp(historyDto.getCancelTimeStamp());
        output.setReturnTimeStamp(historyDto.getReturnTimeStamp());
        output.setLostTimeStamp(historyDto.getLostTimeStamp());
        
        return output;
    }
    
    private ThingDto getThingDto(HistoryDto historyDto) throws NotFoundException, InternalServerErrorException {
        return dataAdapter.findThingByUnivCodeAndDeptCodeAndThingCode(historyDto.getUnivCode(), historyDto.getDeptCode(), historyDto.getThingCode());
    }
    
    private ItemDto getItemDto(HistoryDto historyDto) throws NotFoundException, InternalServerErrorException {
        return dataAdapter.findItemByUnivCodeAndDeptCodeAndThingCodeAndItemNum(historyDto.getUnivCode(), historyDto.getDeptCode(), historyDto.getThingCode(), historyDto.getItemNum());
    }
    
    private ThingJsonBodyNestedToHistory toThingJsonBodyNestedToHistory(ThingDto thingDto) {
        if(thingDto == null) {
            return null;
        }
        
        ThingJsonBodyNestedToHistory output = new ThingJsonBodyNestedToHistory();
        
        output.setCode(thingDto.getCode());
        output.setName(thingDto.getName());
        output.setDescription(thingDto.getDescription());
        output.setEmoji(thingDto.getEmoji());
             
        return output;
    }
    
    private ItemJsonBodyNestedToHistory toItemJsonBodyNestedToHistory(ItemDto itemDto) throws NotFoundException, InternalServerErrorException {
        if(itemDto == null) {
            return null;
        }
        
        ItemJsonBodyNestedToHistory output = new ItemJsonBodyNestedToHistory();
        
        output.setNum(itemDto.getNum());
        output.setCurrentStatus(getStatus(itemDto).name());
        
        return output;
    }
}