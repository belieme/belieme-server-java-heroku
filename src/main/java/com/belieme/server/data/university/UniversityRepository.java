package com.belieme.server.data.university;

import java.util.List;
import org.springframework.data.repository.CrudRepository;

public interface UniversityRepository extends CrudRepository <UniversityEntity, Integer> {
    public List<UniversityEntity> findByCode(String code);
}
