package com.hanyang.belieme.demoserver.department;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

public interface DepartmentRepository extends CrudRepository <DepartmentDB, Integer> {
    public List<DepartmentDB> findByUniversityId(int universityId);
    public List<DepartmentDB> findByUniversityIdAndDepartmentCode(int universityId, String departmentCode);
}
