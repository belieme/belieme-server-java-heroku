package com.belieme.server.data.department;

import java.util.List;
import org.springframework.data.repository.CrudRepository;

public interface DepartmentRepository extends CrudRepository <DepartmentEntity, Integer> {
    public List<DepartmentEntity> findByUnivId(int univId);
    public List<DepartmentEntity> findByUnivIdAndCode(int univId, String code);
}
