package com.belieme.server.domain.permission;

import java.util.List;

import com.belieme.server.domain.exception.*;

public interface PermissionDao {
    public List<PermissionDto> findAllByUnivCodeAndStudentId(String univCode, String studentId) throws InternalDataBaseException;
    public PermissionDto findByUnivCodeAndStudentIdAndDeptCode(String univCode, String studentId, String deptCode) throws NotFoundOnDataBaseException, InternalDataBaseException;
    public PermissionDto save(PermissionDto permission) throws NotFoundOnDataBaseException, InternalDataBaseException, CodeDuplicationException;
    public PermissionDto update(String univCode, String studentId, String deptCode, PermissionDto permission) throws NotFoundOnDataBaseException, InternalDataBaseException, CodeDuplicationException;
}