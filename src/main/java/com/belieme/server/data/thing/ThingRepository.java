package com.belieme.server.data.thing;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

public interface ThingRepository extends CrudRepository<ThingEntity, Integer> {
    public List<ThingEntity> findByDeptId(int deptId);
    public List<ThingEntity> findByDeptIdAndCode(int deptId, String code);
}
