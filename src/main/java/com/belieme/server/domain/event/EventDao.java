package com.belieme.server.domain.event;

import java.util.List;
import com.belieme.server.domain.exception.*;

public interface EventDao {
    public List<EventDto> findByUnivCodeAndDeptCode(String univCode, String deptCode) throws InternalDataBaseException;
    public List<EventDto> findByUnivCodeAndDeptCodeAndUserId(String univCode, String deptCode, String userId) throws InternalDataBaseException;
    public List<EventDto> findByUnivCodeAndDeptCodeAndThingCodeAndItemNum(String univCode, String deptCode, String thingCode, int itemNum) throws InternalDataBaseException;
    public EventDto findByUnivCodeAndDeptCodeAndThingCodeAndItemNumAndEventNum(String univCode, String deptCode, String thingCode, int itemNum, int eventNum) throws NotFoundOnServerException, InternalDataBaseException;
    public EventDto save(EventDto event) throws InternalDataBaseException, CodeDuplicationException, BreakDataBaseRulesException;
    public EventDto update(String univCode, String deptCode, String thingCode, int itemNum, int eventNum, EventDto event) throws NotFoundOnServerException, InternalDataBaseException, CodeDuplicationException, BreakDataBaseRulesException;
}