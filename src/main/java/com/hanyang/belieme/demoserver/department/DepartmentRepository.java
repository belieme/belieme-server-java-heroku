package com.hanyang.belieme.demoserver.department;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

public interface DepartmentRepository extends CrudRepository <Department, Integer> {
    public List<Department> findByUniversityId(int universityId);
    public List<Department> findByUniversityIdAndDepartmentCode(int universityId, String departmentCode);
}
