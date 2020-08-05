package com.hanyang.belieme.demoserver.event;

import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface HistoryRepository extends CrudRepository <History, Integer> {
    List<History> findByRequesterId(int requesterId);
    List<History> findByTypeIdAndItemNum(int typeId, int itemNum);
}
