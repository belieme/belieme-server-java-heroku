package com.belieme.server.domain.permission;

import java.util.List;

import com.belieme.server.domain.exception.ServerDomainException;

public interface PermissionDao {
    public List<PermissionDto> findAllByUnivCodeAndStudentId(String univCode, String studentId) throws ServerDomainException;
    public PermissionDto findByUnivCodeAndStudentIdAndDeptCode(String univCode, String studentId, String deptCode) throws ServerDomainException;
    public PermissionDto save(PermissionDto permission) throws ServerDomainException;
    public PermissionDto update(String univCode, String studentId, String deptCode, PermissionDto permission) throws ServerDomainException;
}