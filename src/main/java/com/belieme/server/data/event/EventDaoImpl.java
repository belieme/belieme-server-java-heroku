package com.belieme.server.data.event;

import java.util.List;

import com.belieme.server.data.common.*;

import com.belieme.server.domain.event.*;
import com.belieme.server.domain.exception.*;

public class EventDaoImpl implements EventDao {
    private DomainAdapter domainAdapter;
    
    public EventDaoImpl(RepositoryManager repositoryManager) {
        this.domainAdapter = new DomainAdapter(repositoryManager);
    }
    
    public List<EventDto> findByUnivCodeAndDeptCode(String univCode, String deptCode) throws InternalDataBaseException {
        return domainAdapter.getEventDtoListByUnivCodeAndDeptCode(univCode, deptCode);
    }
    
    public List<EventDto> findByUnivCodeAndDeptCodeAndUserId(String univCode, String deptCode, String userStudentId) throws InternalDataBaseException {
        return domainAdapter.getEventDtoListByUnivCodeAndDeptCodeAndStudentId(univCode, deptCode, userStudentId);
    }
    
    public List<EventDto> findByUnivCodeAndDeptCodeAndThingCodeAndItemNum(String univCode, String deptCode, String thingCode, int itemNum) throws InternalDataBaseException {
        return domainAdapter.getEventDtoListByUnivCodeAndDeptCodeAndThingCodeAndItemNum(univCode, deptCode, thingCode, itemNum);
    }
    
    public EventDto findByUnivCodeAndDeptCodeAndThingCodeAndItemNumAndEventNum(String univCode, String deptCode, String thingCode, int itemNum, int eventNum) throws NotFoundOnServerException, InternalDataBaseException {
        return domainAdapter.getEventDtoByUnivCodeAndDeptCodeAndThingCodeAndItemNumAndEventNum(univCode, deptCode, thingCode, itemNum, eventNum);
    }
    
    public EventDto save(EventDto event) throws InternalDataBaseException, CodeDuplicationException, BreakDataBaseRulesException { 
        return domainAdapter.saveEventDto(event);        
    }
    
    public EventDto update(String univCode, String deptCode, String thingCode, int itemNum, int eventNum, EventDto event) throws NotFoundOnServerException, InternalDataBaseException, CodeDuplicationException, BreakDataBaseRulesException {
        return domainAdapter.updateEventDto(univCode, deptCode, thingCode, itemNum, eventNum, event);
    }
}