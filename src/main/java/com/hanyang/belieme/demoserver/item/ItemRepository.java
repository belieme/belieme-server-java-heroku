package com.hanyang.belieme.demoserver.item;

import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;

public interface ItemRepository extends CrudRepository<Item, Integer> {
    Optional<Item> findByItemId(int itemId);
    List<Item> findByThingId(int thingId);
    List<Item> findByThingIdAndNum(int thingId, int num);
}
