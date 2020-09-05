package com.hanyang.belieme.demoserver.user;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

public interface UserRepository extends CrudRepository<UserDB, Integer> {
    List<UserDB> findByStudentId(String studentId);
    List<UserDB> findByToken(String token);
}
