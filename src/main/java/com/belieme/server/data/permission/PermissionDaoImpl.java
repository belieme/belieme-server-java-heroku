package com.belieme.server.data.permission;

import java.util.List;

import com.belieme.server.data.common.*;
import com.belieme.server.domain.exception.*;
import com.belieme.server.domain.permission.*;

public class PermissionDaoImpl implements PermissionDao {
    private DomainAdapter domainAdapter;

    public PermissionDaoImpl(RepositoryManager repositoryManager) {
        this.domainAdapter = new DomainAdapter(repositoryManager);
    }
    
    public List<PermissionDto> findAllByUnivCodeAndStudentId(String univCode, String studentId) throws InternalDataBaseException {
        return domainAdapter.getPermissionDtoListByUnivCodeAndStudentId(univCode, studentId);
    }
    
    public PermissionDto findByUnivCodeAndStudentIdAndDeptCode(String univCode, String studentId, String deptCode) throws NotFoundOnServerException, InternalDataBaseException {
        return domainAdapter.getPermissionDtoByUnivCodeAndStudentIdAndDeptCode(univCode, studentId, deptCode);
    }
    
    public PermissionDto save(PermissionDto permission) throws BreakDataBaseRulesException, InternalDataBaseException, CodeDuplicationException {
        return domainAdapter.savePermissionDto(permission);
    }
    
    public PermissionDto update(String univCode, String studentId, String deptCode, PermissionDto permission) throws BreakDataBaseRulesException, NotFoundOnServerException, InternalDataBaseException, CodeDuplicationException {
        return domainAdapter.updatePermissionDto(univCode, studentId, deptCode, permission);
    }
}