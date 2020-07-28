package com.hanyang.belieme.demoserver;

import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.UUID;

public interface HistoryRepository extends CrudRepository <History, UUID> {
    List<History> findByRequesterId(int requesterId);
}
