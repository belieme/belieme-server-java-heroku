package com.hanyang.belieme.demoserver.event;

import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface EventRepository extends CrudRepository <EventDB, Integer> {
    List<EventDB> findByUserId(int userId);
    List<EventDB> findByItemId(int itemId);
}
