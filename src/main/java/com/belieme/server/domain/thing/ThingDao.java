package com.belieme.server.domain.thing;

import java.util.List;
import com.belieme.server.domain.exception.*;

public interface ThingDao {
    public List<ThingDto> findByUnivCodeAndDeptCode(String univCode, String deptCode) throws ServerDomainException;
    public ThingDto findByUnivCodeAndDeptCodeAndCode(String univCode, String deptCode, String code) throws ServerDomainException;
    public ThingDto save(ThingDto thing) throws ServerDomainException;
    public ThingDto update(String univCode, String deptCode, String code, ThingDto user) throws ServerDomainException;
}