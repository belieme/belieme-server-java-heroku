package com.hanyang.belieme.demoserver.event;

import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface EventRepository extends CrudRepository <EventDB, Integer> {
    List<EventDB> findByRequesterId(int requesterId);
    List<EventDB> findByItemId(int itemId);
}
