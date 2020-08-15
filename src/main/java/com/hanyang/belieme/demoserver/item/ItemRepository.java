package com.hanyang.belieme.demoserver.item;

import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface ItemRepository extends CrudRepository<Item, Integer> {
    List<Item> findByThingId(int thingId); //가능할 것인가? 불가능띠
    List<Item> findByThingIdAndNum(int thingId, int num);
}
