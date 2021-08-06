package com.belieme.server.domain.thing;

import java.util.List;
import com.belieme.server.domain.exception.*;

public interface ThingDao {
    public List<ThingDto> findByUnivCodeAndDeptCode(String univCode, String deptCode) throws InternalDataBaseException;
    public ThingDto findByUnivCodeAndDeptCodeAndThingCode(String univCode, String deptCode, String thingCode) throws NotFoundOnServerException, InternalDataBaseException;
    public ThingDto save(ThingDto thing) throws InternalDataBaseException, CodeDuplicationException, BreakDataBaseRulesException;
    public ThingDto update(String univCode, String deptCode, String code, ThingDto user) throws NotFoundOnServerException, InternalDataBaseException, CodeDuplicationException, BreakDataBaseRulesException;
}