package com.belieme.server.data.permission;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

public interface PermissionRepository extends CrudRepository <PermissionEntity, Integer> {
    List<PermissionEntity> findByUserId(int userId);
    List<PermissionEntity> findByDeptId(int deptId);
    List<PermissionEntity> findByUserIdAndDeptId(int userId, int deptId);
}