package com.belieme.server.data.user;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

public interface UserRepository extends CrudRepository<UserEntity, Integer> {
    List<UserEntity> findByUnivIdAndStudentId(int univId, String studentId);
    List<UserEntity> findByUnivId(int univId);
    List<UserEntity> findByToken(String token);
}
