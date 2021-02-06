package com.belieme.server.domain.department;

import java.util.List;
import com.belieme.server.domain.exception.*;

public interface DepartmentDao {
    public List<DepartmentDto> findAllByUnivCode(String univCode) throws ServerDomainException;
    public DepartmentDto findByUnivCodeAndDeptCode(String univCode, String deptCode) throws ServerDomainException;
    public DepartmentDto save(DepartmentDto dept) throws ServerDomainException;
    public DepartmentDto update(String univCode, String deptCode, DepartmentDto dept) throws ServerDomainException;
}