package com.hanyang.belieme.demoserver.thing;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

public interface ThingRepository extends CrudRepository<ThingDB, Integer> {
    public List<ThingDB> findByDepartmentId(int departmentId);
}
