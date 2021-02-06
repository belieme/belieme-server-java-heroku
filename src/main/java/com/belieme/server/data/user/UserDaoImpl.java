package com.belieme.server.data.user;

import java.util.*;

import com.belieme.server.domain.exception.*;
import com.belieme.server.domain.user.*;
import com.belieme.server.domain.permission.*;

import com.belieme.server.data.RepositoryManager;
import com.belieme.server.data.university.*;
import com.belieme.server.data.department.*;
import com.belieme.server.data.permission.*;

public class UserDaoImpl implements UserDao {
    RepositoryManager repositoryManager;

    public UserDaoImpl(RepositoryManager repositoryManager) {
        this.repositoryManager = repositoryManager;
    }
    
    
    public List<UserDto> findAllByUnivCodeAndDeptCode(String univCode, String deptCode) throws NotFoundOnDataBaseException, InternalDataBaseException {
        List<PermissionEntity> permissionListByDeptId = repositoryManager.getAllPermissionEntitiesByUnivCodeAndDeptCode(univCode, deptCode);
        List<UserDto> output = new ArrayList<>();
        
        for(int i = 0; i < permissionListByDeptId.size(); i++) {
            int userId = permissionListByDeptId.get(i).getId();
            UserDto user = toUserDto(repositoryManager.getUserEntityById(userId));
            output.add(user);
        }

         // TODO 같은 dept code를 갖는 permissions가 있을 시 예외처리는 안함. db에 저장하는 것을 제대로 만들면 딱히 필요없을 듯
        return output;
    }
    
    public UserDto findByUnivCodeAndStudentId(String univCode, String studentId) throws NotFoundOnDataBaseException, InternalDataBaseException {
        UserEntity user = repositoryManager.getUserEntityByUnivCodeAndStudentId(univCode, studentId);
        return toUserDto(user);
    }
    
    public UserDto findByToken(String token) throws NotFoundOnDataBaseException, InternalDataBaseException {
        return toUserDto(repositoryManager.getUserEntityByToken(token));
    }
    
    public UserDto save(UserDto userDto) throws NotFoundOnDataBaseException, InternalDataBaseException, CodeDuplicationException { 
        String univCode = userDto.getUnivCode();
        
        repositoryManager.checkUserDuplicationByUnivCodeAndStudentId(userDto.getUnivCode(), userDto.getStudentId());
        repositoryManager.checkUserDuplicationByToken(userDto.getToken());
        
        UserEntity newUser = new UserEntity();
        
        int univId = repositoryManager.getUnivEntityByUnivCode(univCode).getId();
        newUser.setUnivId(univId);
        newUser.setStudentId(userDto.getStudentId());
        newUser.setName(userDto.getName());
        newUser.setEntranceYear(userDto.getEntranceYear());
        newUser.setCreateTimeStamp(userDto.getCreateTimeStamp());
        newUser.setApprovalTimeStamp(userDto.getApprovalTimeStamp());
        newUser.setToken(userDto.getToken());
        
        UserEntity savedUser = repositoryManager.saveUser(newUser);
        
        Map<String, Permissions> permissions = userDto.getPermissions();
        for (Map.Entry<String, Permissions> entry : permissions.entrySet()) {
            PermissionEntity tmp = findOrMakeNewPermissionEntity(univCode, entry.getKey(), userDto.getStudentId());
            tmp.setPermission(entry.getValue().name());
            repositoryManager.savePermission(tmp);
        }
        
        return toUserDto(savedUser);
    }
    
    private PermissionEntity findOrMakeNewPermissionEntity(String univCode, String deptCode, String studentId) throws InternalDataBaseException {            
        PermissionEntity output = new PermissionEntity();
        try {
            output = repositoryManager.getPermissionEntityByUnivCodeAndStudentIdAndDeptCode(univCode, studentId, deptCode);
        } catch(NotFoundOnDataBaseException e1) {
            try {
                int userId = repositoryManager.getUserEntityByUnivCodeAndStudentId(univCode, studentId).getId();
                int deptId = repositoryManager.getDeptEntityByUnivCodeAndDeptCode(univCode, deptCode).getId();    
                output.setUserId(userId);
                output.setDeptId(deptId);
            } catch(NotFoundOnDataBaseException e2) {
                throw new InternalDataBaseException();
            }
        }
        return output;
    }
    
    private UserDto toUserDto(UserEntity userEntity) throws InternalDataBaseException {
        UserDto output = new UserDto();
        
        UniversityEntity univ;
        try{
            univ = repositoryManager.getUnivEntityById(userEntity.getUnivId());
            output.setUnivCode(univ.getCode());
        } catch(NotFoundOnDataBaseException e) {
            throw new InternalDataBaseException();
        }
        
        output.setStudentId(userEntity.getStudentId());
        output.setName(userEntity.getName());
        output.setEntranceYear(userEntity.getEntranceYear());
        output.setCreateTimeStamp(userEntity.getCreateTimeStamp());
        output.setApprovalTimeStamp(userEntity.getApprovalTimeStamp());
        output.setToken(userEntity.getToken());
        
        List<PermissionEntity> permissionList = repositoryManager.getAllPermissionEntitiesByUnivCodeAndStudentId(univ.getCode(), userEntity.getStudentId());
        for(int i = 0; i < permissionList.size(); i++) {
            PermissionEntity permission = permissionList.get(i);
            
            try {
                DepartmentEntity dept = repositoryManager.getDeptEntityById(permission.getDeptId());
                output.addPermission(dept.getCode(), Permissions.valueOf(permission.getPermission()));
                // TODO 같은 dept code를 갖는 permissions가 있을 시 예외처리는 안함. db에 저장하는 것을 제대로 만들면 딱히 필요없을 듯
            } catch(NotFoundOnDataBaseException e) {
                throw new InternalDataBaseException();
            }
        }
        
        return output;
    }
    
    public UserDto update(String univCode, String studentId, UserDto user) throws NotFoundOnDataBaseException, InternalDataBaseException, CodeDuplicationException {
        UserEntity target = repositoryManager.getUserEntityByUnivCodeAndStudentId(univCode, studentId);
        
        if(univCode != user.getUnivCode() || studentId != user.getStudentId()) {
            repositoryManager.checkUserDuplicationByUnivCodeAndStudentId(user.getUnivCode(), user.getStudentId());
            int newUnivId = repositoryManager.getUnivEntityByUnivCode(user.getUnivCode()).getId();
            target.setUnivId(newUnivId);
            target.setStudentId(user.getStudentId());
        }
        
        target.setName(user.getName());
        target.setEntranceYear(user.getEntranceYear());
        target.setCreateTimeStamp(user.getCreateTimeStamp());
        target.setApprovalTimeStamp(user.getApprovalTimeStamp());
        target.setToken(user.getToken());
        
        Map<String, Permissions> permissions = user.getPermissions();
        for (Map.Entry<String, Permissions> entry : permissions.entrySet()) {
            PermissionEntity tmp = findOrMakeNewPermissionEntity(univCode, entry.getKey(), user.getStudentId());
            tmp.setPermission(entry.getValue().name());
            repositoryManager.savePermission(tmp);
        }
        
        UserEntity newUser = repositoryManager.saveUser(target);
        return toUserDto(newUser);
    }
}