package com.hanyang.belieme.demoserver.university;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

public interface UniversityRepository extends CrudRepository <University, Integer> {
    public List<University> findByUniversityCode(String universityCode);
}
