package com.belieme.server.data.thing;

import java.util.List;

import com.belieme.server.data.common.*;

import com.belieme.server.domain.exception.*;
import com.belieme.server.domain.thing.*;

public class ThingDaoImpl implements ThingDao {
    private DomainAdapter domainAdapter;

    public ThingDaoImpl(RepositoryManager repositoryManager) {
        this.domainAdapter = new DomainAdapter(repositoryManager);
    } 
    
    public List<ThingDto> findAllByUnivCodeAndDeptCode(String univCode, String deptCode) throws InternalDataBaseException {
        return domainAdapter.getThingDtoListByUnivCodeAndDeptCode(univCode, deptCode);
    }
    
    public ThingDto findByUnivCodeAndDeptCodeAndThingCode(String univCode, String deptCode, String thingCode) throws NotFoundOnServerException, InternalDataBaseException {
        return domainAdapter.getThingDtoByUnivCodeAndDeptCodeAndThingCode(univCode, deptCode, thingCode);
    }
    
    public ThingDto save(ThingDto thing) throws InternalDataBaseException, CodeDuplicationException, BreakDataBaseRulesException {
        return domainAdapter.saveThingDto(thing);
    }
    
    public ThingDto update(String univCode, String deptCode, String code, ThingDto thing) throws NotFoundOnServerException, InternalDataBaseException, CodeDuplicationException, BreakDataBaseRulesException {
        return domainAdapter.updateThingDto(univCode, deptCode, code, thing);
    }
}