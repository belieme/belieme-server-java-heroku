package com.belieme.server.data.event;

import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface EventRepository extends CrudRepository <EventEntity, Integer> {
    List<EventEntity> findByUserId(int userId);
    List<EventEntity> findByItemId(int itemId);
    List<EventEntity> findByItemIdAndNum(int itemId, int num);
}
