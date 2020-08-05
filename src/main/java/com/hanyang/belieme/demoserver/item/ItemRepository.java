package com.hanyang.belieme.demoserver.item;

import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface ItemRepository extends CrudRepository<Item, ItemPK> {
    List<Item> findByTypeId(int typeId); //가능할 것인가?
}
