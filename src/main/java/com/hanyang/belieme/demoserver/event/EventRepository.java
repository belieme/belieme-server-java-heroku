package com.hanyang.belieme.demoserver.event;

import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface EventRepository extends CrudRepository <Event, Integer> {
    List<Event> findByRequesterId(int requesterId);
    List<Event> findByItemId(int itemId);
}
