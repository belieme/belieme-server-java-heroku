package com.belieme.server.data.department;

import java.util.*;

import com.belieme.server.data.common.*;

import com.belieme.server.domain.department.*;
import com.belieme.server.domain.exception.*;

public class DepartmentDaoImpl implements DepartmentDao {
    private DomainAdapter domainAdapter;
    
    public DepartmentDaoImpl(RepositoryManager repositoryManager) {
        this.domainAdapter = new DomainAdapter(repositoryManager);
    }
    
    public List<DepartmentDto> findAllByUnivCode(String univCode) throws InternalDataBaseException {
        return domainAdapter.getDeptDtoListByUnivCode(univCode);
    }
    
    public DepartmentDto findByUnivCodeAndDeptCode(String univCode, String deptCode) throws InternalDataBaseException, NotFoundOnServerException {
        return domainAdapter.getDeptDtoByUnivCodeAndDeptCode(univCode, deptCode);
    }
    
    public DepartmentDto save(DepartmentDto dept) throws CodeDuplicationException, BreakDataBaseRulesException, InternalDataBaseException {
        return domainAdapter.saveDeptDto(dept);
    }
    
    public DepartmentDto update(String univCode, String deptCode, DepartmentDto dept) throws CodeDuplicationException, BreakDataBaseRulesException, InternalDataBaseException, NotFoundOnServerException {
        return domainAdapter.updateDeptDto(univCode, deptCode, dept);
    }
}