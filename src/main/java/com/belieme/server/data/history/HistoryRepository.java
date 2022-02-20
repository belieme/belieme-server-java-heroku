package com.belieme.server.data.history;

import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface HistoryRepository extends CrudRepository <HistoryEntity, Integer> {
    List<HistoryEntity> findByUserId(int userId);
    List<HistoryEntity> findByItemId(int itemId);
    List<HistoryEntity> findByItemIdAndNum(int itemId, int num);
}
