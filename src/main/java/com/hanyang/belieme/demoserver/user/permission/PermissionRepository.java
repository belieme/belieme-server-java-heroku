package com.hanyang.belieme.demoserver.user.permission;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

public interface PermissionRepository extends CrudRepository <PermissionDB, Integer> {
    List<PermissionDB> findByUserId(int userId);
    List<PermissionDB> findByUserIdAndDeptId(int userId, int deptId);
}