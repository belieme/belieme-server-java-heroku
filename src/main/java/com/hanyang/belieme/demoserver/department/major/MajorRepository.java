package com.hanyang.belieme.demoserver.department.major;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

public interface MajorRepository extends CrudRepository <Major, Integer> {
    List<Major> findByDepartmentId(int departmentId);
}