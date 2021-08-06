package com.belieme.server.domain.permission;

import java.util.List;

import com.belieme.server.domain.exception.*;

public interface PermissionDao {
    public List<PermissionDto> findAllByUnivCodeAndStudentId(String univCode, String studentId) throws InternalDataBaseException;
    public PermissionDto findByUnivCodeAndStudentIdAndDeptCode(String univCode, String studentId, String deptCode) throws NotFoundOnServerException, InternalDataBaseException;
    public PermissionDto save(PermissionDto permission) throws BreakDataBaseRulesException, InternalDataBaseException, CodeDuplicationException;
    public PermissionDto update(String univCode, String studentId, String deptCode, PermissionDto permission) throws NotFoundOnServerException, BreakDataBaseRulesException, InternalDataBaseException, CodeDuplicationException;
}