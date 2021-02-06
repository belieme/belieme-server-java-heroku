package com.belieme.server.data.permission;

import java.util.ArrayList;
import java.util.List;

import com.belieme.server.data.RepositoryManager;
import com.belieme.server.data.department.DepartmentEntity;
import com.belieme.server.data.university.UniversityEntity;
import com.belieme.server.data.user.UserEntity;
import com.belieme.server.domain.exception.InternalDataBaseException;
import com.belieme.server.domain.exception.NotFoundOnDataBaseException;
import com.belieme.server.domain.exception.ServerDomainException;
import com.belieme.server.domain.permission.*;

public class PermissionDaoImpl implements PermissionDao {
    RepositoryManager repositoryManager;

    public PermissionDaoImpl(RepositoryManager repositoryManager) {
        this.repositoryManager = repositoryManager;
    }
    
    private PermissionDto toPermissionDto(PermissionEntity permissionEntity) throws InternalDataBaseException {
        PermissionDto output = new PermissionDto();
        try {
            DepartmentEntity dept = repositoryManager.getDeptEntityById(permissionEntity.getDepartmentId());
            UniversityEntity univ = repositoryManager.getUnivEntityById(dept.getUnivId());
            UserEntity user = repositoryManager.getUserEntityById(permissionEntity.getUserId());
            output.setUnivCode(univ.getCode());
            output.setStudentId(user.getStudentId());
            output.setDeptCode(dept.getCode());    
        } catch(NotFoundOnDataBaseException e) {
            throw new InternalDataBaseException();
        }
        return output;
    }
    
    private List<PermissionDto> toPermissionDtoList(List<PermissionEntity> permissionEntities) throws InternalDataBaseException {
        List<PermissionDto> output = new ArrayList<>();
        for(int i = 0; i < permissionEntities.size(); i++) {
            output.add(toPermissionDto(permissionEntities.get(i)));
        }
        return output;
    }
    
    public List<PermissionDto> findAllByUnivCodeAndStudentId(String univCode, String studentId) throws ServerDomainException {
        return toPermissionDtoList(repositoryManager.getAllPermissionEntitiesByUnivCodeAndDeptCode(univCode, studentId));
    }
    
    public PermissionDto findByUnivCodeAndStudentIdAndDeptCode(String univCode, String studentId, String deptCode) throws ServerDomainException {
        return toPermissionDto(repositoryManager.getPermissionEntityByUnivCodeAndStudentIdAndDeptCode(univCode, studentId, deptCode));
    }
    
    public PermissionDto save(PermissionDto permission) throws ServerDomainException {
        repositoryManager.checkPermissionDuplication(permission.getUnivCode(), permission.getStudentId(), permission.getDeptCode());
        
        int userId = repositoryManager.getUserEntityByUnivCodeAndStudentId(permission.getUnivCode(), permission.getStudentId()).getId();
        int deptId = repositoryManager.getDeptEntityByUnivCodeAndDeptCode(permission.getUnivCode(), permission.getDeptCode()).getId();
        
        PermissionEntity newPermissionEntity = new PermissionEntity();
        newPermissionEntity.setUserId(userId);
        newPermissionEntity.setDepartmentId(deptId);
        newPermissionEntity.setPermission(permission.getPermission().name());
        
        PermissionDto output = toPermissionDto(repositoryManager.savePermission(newPermissionEntity));
        return output;
    }
    
    public PermissionDto update(String univCode, String studentId, String deptCode, PermissionDto permission) throws ServerDomainException {
        PermissionEntity target = repositoryManager.getPermissionEntityByUnivCodeAndStudentIdAndDeptCode(univCode, studentId, deptCode);
        
        if(univCode != permission.getUnivCode() || studentId != permission.getStudentId() || deptCode != permission.getDeptCode()) {
            repositoryManager.checkPermissionDuplication(permission.getUnivCode(), permission.getStudentId(), permission.getDeptCode());    
            int userId = repositoryManager.getUserEntityByUnivCodeAndStudentId(permission.getUnivCode(), permission.getStudentId()).getId();
            int deptId = repositoryManager.getDeptEntityByUnivCodeAndDeptCode(permission.getUnivCode(), permission.getDeptCode()).getId();
            target.setUserId(userId);
            target.setDepartmentId(deptId);
        }
        target.setPermission(permission.getPermission().name());
        
        PermissionDto output = toPermissionDto(repositoryManager.savePermission(target));
        return output;
    }
}