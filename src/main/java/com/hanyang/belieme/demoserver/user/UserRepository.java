package com.hanyang.belieme.demoserver.user;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

public interface UserRepository extends CrudRepository<UserDB, Integer> {
    List<UserDB> findByUniversityIdAndStudentId(int universityId, String studentId);
    List<UserDB> findByUniversityId(int universityId);
    List<UserDB> findByToken(String token);
}
