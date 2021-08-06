package com.belieme.server.domain.department;

import java.util.List;
import com.belieme.server.domain.exception.*;

public interface DepartmentDao {
    public List<DepartmentDto> findAllByUnivCode(String univCode) throws InternalDataBaseException;
    public DepartmentDto findByUnivCodeAndDeptCode(String univCode, String deptCode) throws InternalDataBaseException, NotFoundOnServerException;
    public DepartmentDto save(DepartmentDto dept) throws InternalDataBaseException, BreakDataBaseRulesException, CodeDuplicationException;
    public DepartmentDto update(String univCode, String deptCode, DepartmentDto dept) throws InternalDataBaseException, BreakDataBaseRulesException, NotFoundOnServerException, CodeDuplicationException;
}