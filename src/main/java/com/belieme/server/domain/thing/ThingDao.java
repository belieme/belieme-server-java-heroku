package com.belieme.server.domain.thing;

import java.util.List;
import com.belieme.server.domain.exception.*;

public interface ThingDao {
    public List<ThingDto> findByUnivCodeAndDeptCode(String univCode, String deptCode) throws InternalDataBaseException;
    public ThingDto findByUnivCodeAndDeptCodeAndCode(String univCode, String deptCode, String code) throws NotFoundOnDataBaseException, InternalDataBaseException;
    public ThingDto save(ThingDto thing) throws NotFoundOnDataBaseException, InternalDataBaseException, CodeDuplicationException;
    public ThingDto update(String univCode, String deptCode, String code, ThingDto user) throws NotFoundOnDataBaseException, InternalDataBaseException, CodeDuplicationException;
}