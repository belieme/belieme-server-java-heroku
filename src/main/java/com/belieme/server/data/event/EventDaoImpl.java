package com.belieme.server.data.event;

import java.util.ArrayList;
import java.util.List;

import com.belieme.server.data.RepositoryManager;
import com.belieme.server.data.department.*;
import com.belieme.server.data.item.ItemEntity;
import com.belieme.server.data.thing.ThingEntity;
import com.belieme.server.data.university.*;

import com.belieme.server.domain.event.*;
import com.belieme.server.domain.exception.*;

public class EventDaoImpl implements EventDao {
    private RepositoryManager repositoryManager;
    
    public EventDaoImpl(RepositoryManager repositoryManager) {
        this.repositoryManager = repositoryManager;
    }
    
    private EventDto toEventDto(EventEntity eventEntity) throws NotFoundOnDataBaseException {
        EventDto output = new EventDto();
        
        ItemEntity itemEntity = repositoryManager.getItemEntityById(eventEntity.getItemId());
        ThingEntity thingEntity = repositoryManager.getThingEntity(itemEntity.getThingId());
        DepartmentEntity deptEntity = repositoryManager.getDeptEntityById(thingEntity.getDepartmentId());
        UniversityEntity univEntity = repositoryManager.getUnivEntityById(deptEntity.getUnivId());
        
        output.setUnivCode(univEntity.getCode());
        output.setDeptCode(deptEntity.getCode());
        output.setThingCode(thingEntity.getCode());
        output.setItemNum(itemEntity.getNum());
        output.setNum(eventEntity.getNum());
    
        String userStudentId = repositoryManager.getUserEntityById(eventEntity.getUserId()).getStudentId();
        String approveManagerStudentId = repositoryManager.getUserEntityById(eventEntity.getApproveManagerId()).getStudentId();
        String returnManagerStudentId = repositoryManager.getUserEntityById(eventEntity.getReturnManagerId()).getStudentId();
        String lostManagerStudentId = repositoryManager.getUserEntityById(eventEntity.getLostManagerId()).getStudentId();
        
        output.setUserStudentId(userStudentId);
        output.setApproveManagerStudentId(approveManagerStudentId);
        output.setReturnManagerStudentId(returnManagerStudentId);
        output.setLostManagerStudentId(lostManagerStudentId);
    
        output.setReserveTimeStamp(eventEntity.getReserveTimeStamp());
        output.setApproveTimeStamp(eventEntity.getApproveTimeStamp());
        output.setReturnTimeStamp(eventEntity.getReturnTimeStamp());
        output.setCancelTimeStamp(eventEntity.getCancelTimeStamp());
        output.setLostTimeStamp(eventEntity.getLostTimeStamp());
        
        return output;
    }
    
    public List<EventDto> findByUnivCodeAndDeptCode(String univCode, String deptCode) throws NotFoundOnDataBaseException, InternalDataBaseException {
        List<EventEntity> eventList = repositoryManager.getAllEventEntitiesByUnivCodeAndDeptCode(univCode, deptCode);
        List<EventDto> output = new ArrayList<>();
        
        for(int i = 0; i < eventList.size(); i++) {
            output.add(toEventDto(eventList.get(i)));
        }
        return output;
    }
    
    public List<EventDto> findByUnivCodeAndDeptCodeAndUserId(String univCode, String deptCode, String userStudentId) throws NotFoundOnDataBaseException, InternalDataBaseException {
        List<EventEntity> eventList = repositoryManager.getAllEventEntitiesByUnivCodeAndDeptCodeAndUserStudnetId(univCode, deptCode, userStudentId);
        List<EventDto> output = new ArrayList<>();
        
        for(int i = 0; i < eventList.size(); i++) {
            output.add(toEventDto(eventList.get(i)));
        }
        return output;
    }
    
    public List<EventDto> findByUnivCodeAndDeptCodeAndThingCodeAndItemNum(String univCode, String deptCode, String thingCode, int itemNum) throws NotFoundOnDataBaseException, InternalDataBaseException {
        List<EventEntity> eventList = repositoryManager.getAllEventEntitiesByUnivCodeAndDeptCodeAndThingCodeAndItemNum(univCode, deptCode, thingCode, itemNum);
        List<EventDto> output = new ArrayList<>();
        
        for(int i = 0; i < eventList.size(); i++) {
            output.add(toEventDto(eventList.get(i)));
        }
        return output;
    }
    
    public EventDto findByUnivCodeAndDeptCodeAndThingCodeAndItemNumAndEventNum(String univCode, String deptCode, String thingCode, int itemNum, int eventNum) throws NotFoundOnDataBaseException, InternalDataBaseException {
        return toEventDto(repositoryManager.getEventEntityByUnivCodeAndDeptCodeAndThingCodeAndItemNumAndEventNum(univCode, deptCode, thingCode, itemNum, eventNum));
    }
    
    public EventDto save(EventDto event) throws NotFoundOnDataBaseException, InternalDataBaseException, CodeDuplicationException {
        repositoryManager.checkEventDuplication(event.getUnivCode(), event.getDeptCode(), event.getThingCode(), event.getItemNum(), event.getNum());
        
        EventEntity newEvent = new EventEntity();
        
        ItemEntity item = repositoryManager.getItemEntityByUnivCodeAndDeptCodeAndThingCodeAndItemNum(event.getUnivCode(),event.getDeptCode(), event.getThingCode(), event.getItemNum());
        newEvent.setItemId(item.getId());
        newEvent.setNum(event.getNum());
        
        int userId, approveManagerId, returnManagerId, lostManagerId;
        if(event.getUserStudentId() != null) {
            userId = repositoryManager.getUserEntityByUnivCodeAndStudentId(event.getUnivCode(), event.getUserStudentId()).getId();    
        } else {
            userId = 0;
        }
        if(event.getApproveManagerStudentId() != null) {
            approveManagerId = repositoryManager.getUserEntityByUnivCodeAndStudentId(event.getUnivCode(), event.getApproveManagerStudentId()).getId();
        } else {
            approveManagerId = 0;
        }
        if(event.getReturnManagerStudentId() != null) {
            returnManagerId = repositoryManager.getUserEntityByUnivCodeAndStudentId(event.getUnivCode(), event.getReturnManagerStudentId()).getId();
        } else {
            returnManagerId = 0;
        }
        if(event.getLostManagerStudentId() != null) {
            lostManagerId = repositoryManager.getUserEntityByUnivCodeAndStudentId(event.getUnivCode(), event.getLostManagerStudentId()).getId();    
        } else {
            lostManagerId = 0;
        }
        
        
        newEvent.setUserId(userId);
        newEvent.setApproveManagerId(approveManagerId);
        newEvent.setReturnManagerId(returnManagerId);
        newEvent.setLostManagerId(lostManagerId);
        
        newEvent.setReserveTimeStamp(event.getReserveTimeStamp());
        newEvent.setApproveTimeStamp(event.getApproveTimeStamp());
        newEvent.setReturnTimeStamp(event.getReturnTimeStamp());
        newEvent.setCancelTimeStamp(event.getCancelTimeStamp());
        newEvent.setLostTimeStamp(event.getLostTimeStamp());
        
        EventEntity savedEventEntity = repositoryManager.saveEvent(newEvent);
        item.setLastEventId(savedEventEntity.getId());
        
        repositoryManager.saveItem(item);
        
        return toEventDto(savedEventEntity);
    }
    
    public EventDto update(String univCode, String deptCode, String thingCode, int itemNum, int eventNum, EventDto event) throws NotFoundOnDataBaseException, InternalDataBaseException, CodeDuplicationException {
        EventEntity target = repositoryManager.getEventEntityByUnivCodeAndDeptCodeAndThingCodeAndItemNumAndEventNum(univCode, deptCode, thingCode, itemNum, eventNum);
        
        if(univCode != event.getUnivCode() || deptCode != event.getDeptCode() || thingCode != event.getThingCode() || itemNum != event.getItemNum() || eventNum != event.getNum()) {
            repositoryManager.checkEventDuplication(event.getUnivCode(), event.getDeptCode(), event.getThingCode(), event.getItemNum(), event.getNum());
            ItemEntity item = repositoryManager.getItemEntityByUnivCodeAndDeptCodeAndThingCodeAndItemNum(event.getUnivCode(),event.getDeptCode(), event.getThingCode(), event.getItemNum());
            target.setItemId(item.getId());
            target.setNum(item.getNum());
        }
        
        int userId, approveManagerId, returnManagerId, lostManagerId;
        if(event.getUserStudentId() != null) {
            userId = repositoryManager.getUserEntityByUnivCodeAndStudentId(event.getUnivCode(), event.getUserStudentId()).getId();    
        } else {
            userId = 0;
        }
        if(event.getApproveManagerStudentId() != null) {
            approveManagerId = repositoryManager.getUserEntityByUnivCodeAndStudentId(event.getUnivCode(), event.getApproveManagerStudentId()).getId();
        } else {
            approveManagerId = 0;
        }
        if(event.getReturnManagerStudentId() != null) {
            returnManagerId = repositoryManager.getUserEntityByUnivCodeAndStudentId(event.getUnivCode(), event.getReturnManagerStudentId()).getId();
        } else {
            returnManagerId = 0;
        }
        if(event.getLostManagerStudentId() != null) {
            lostManagerId = repositoryManager.getUserEntityByUnivCodeAndStudentId(event.getUnivCode(), event.getLostManagerStudentId()).getId();    
        } else {
            lostManagerId = 0;
        }
        
        
        target.setUserId(userId);
        target.setApproveManagerId(approveManagerId);
        target.setReturnManagerId(returnManagerId);
        target.setLostManagerId(lostManagerId);
        
        target.setReserveTimeStamp(event.getReserveTimeStamp());
        target.setApproveTimeStamp(event.getApproveTimeStamp());
        target.setReturnTimeStamp(event.getReturnTimeStamp());
        target.setCancelTimeStamp(event.getCancelTimeStamp());
        target.setLostTimeStamp(event.getLostTimeStamp());
        
        EventEntity savedEventEntity = repositoryManager.saveEvent(target);
        
        return toEventDto(savedEventEntity);
    }
}