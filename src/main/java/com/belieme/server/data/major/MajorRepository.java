package com.belieme.server.data.major;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

public interface MajorRepository extends CrudRepository <MajorEntity, Integer> {
    List<MajorEntity> findByDeptId(int deptId);
}