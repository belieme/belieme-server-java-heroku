package com.belieme.server.data.item;

import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface ItemRepository extends CrudRepository<ItemEntity, Integer> {
    List<ItemEntity> findByThingId(int thingId);
    List<ItemEntity> findByThingIdAndNum(int thingId, int num);
}
