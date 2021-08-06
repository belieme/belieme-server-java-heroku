package com.belieme.server.data.major;

import java.util.*;

import com.belieme.server.domain.exception.*;
import com.belieme.server.domain.major.*;

import com.belieme.server.data.common.*;


public class MajorDaoImpl implements MajorDao {
    private DomainAdapter domainAdapter;
   
    public MajorDaoImpl(RepositoryManager repositoryManager) {
        this.domainAdapter = new DomainAdapter(repositoryManager);
    }
    
    public List<MajorDto> findAllByUnivCode(String univCode) throws InternalDataBaseException {
        return domainAdapter.getMajorDtoListByUnivCode(univCode);
    }
    
    public List<MajorDto> findAllByUnivCodeAndDeptCode(String univCode, String deptCode) throws InternalDataBaseException {
        return domainAdapter.getMajorDtoListByUnivCodeAndDeptCode(univCode, deptCode);
    }
    
    public MajorDto findByUnivCodeAndMajorCode(String univCode, String majorCode) throws InternalDataBaseException, NotFoundOnServerException {
        return domainAdapter.getMajorDtoByUnivCodeAndMajorCode(univCode, majorCode);
    }
    
    public MajorDto save(MajorDto major) throws BreakDataBaseRulesException, InternalDataBaseException, CodeDuplicationException {
        return domainAdapter.saveMajorDto(major);
    }
    
    public MajorDto update(String univCode, String majorCode, MajorDto major) throws NotFoundOnServerException, BreakDataBaseRulesException, InternalDataBaseException, CodeDuplicationException {
        return domainAdapter.updateMajorDto(univCode, majorCode, major);
    }
}