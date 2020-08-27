package com.hanyang.belieme.demoserver.item;

import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface ItemRepository extends CrudRepository<ItemDB, Integer> {
    List<ItemDB> findByThingId(int thingId);
    List<ItemDB> findByThingIdAndNum(int thingId, int num);
}
