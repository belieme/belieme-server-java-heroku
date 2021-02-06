package com.belieme.server.domain.event;

import java.util.List;
import com.belieme.server.domain.exception.*;

public interface EventDao {
    public List<EventDto> findByUnivCodeAndDeptCode(String univCode, String deptCode) throws ServerDomainException;
    public List<EventDto> findByUnivCodeAndDeptCodeAndUserId(String univCode, String deptCode, String userId) throws ServerDomainException;
    public List<EventDto> findByUnivCodeAndDeptCodeAndThingCodeAndItemNum(String univCode, String deptCode, String thingCode, int itemNum) throws ServerDomainException;
    public EventDto findByUnivCodeAndDeptCodeAndThingCodeAndItemNumAndEventNum(String univCode, String deptCode, String thingCode, int itemNum, int eventNum) throws ServerDomainException;
    public EventDto save(EventDto event) throws ServerDomainException;
    public EventDto update(String univCode, String deptCode, String thingCode, int itemNum, int eventNum, EventDto event) throws ServerDomainException;
}