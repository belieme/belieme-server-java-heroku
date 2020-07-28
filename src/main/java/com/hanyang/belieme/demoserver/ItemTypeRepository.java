package com.hanyang.belieme.demoserver;

import java.util.UUID;

import org.springframework.data.repository.CrudRepository;

public interface ItemTypeRepository extends CrudRepository<ItemTypeDB, UUID> {
}
