package com.hanyang.belieme.demoserver;

import org.springframework.data.repository.CrudRepository;

public interface ThingsRepository extends CrudRepository<ThingsDB, Integer> {
}
