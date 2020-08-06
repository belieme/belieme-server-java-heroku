package com.hanyang.belieme.demoserver.item;

import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface ItemRepository extends CrudRepository<Item, Integer> {
    List<Item> findByTypeId(int typeId); //가능할 것인가? 불가능띠
    List<Item> findByTypeIdAndNum(int typeId, int num);
}
