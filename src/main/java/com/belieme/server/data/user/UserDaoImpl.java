package com.belieme.server.data.user;

import java.util.*;

import com.belieme.server.domain.exception.*;
import com.belieme.server.domain.user.*;

import com.belieme.server.data.common.*;

public class UserDaoImpl implements UserDao {
    private DomainAdapter domainAdapter;

    public UserDaoImpl(RepositoryManager repositoryManager) {
        this.domainAdapter = new DomainAdapter(repositoryManager);
    } 
    
    public List<UserDto> findAllByUnivCodeAndDeptCode(String univCode, String deptCode) throws InternalDataBaseException {
        return domainAdapter.getUserDtoListByUnivCodeAndDeptCode(univCode, deptCode);
    }
    
    public UserDto findByUnivCodeAndStudentId(String univCode, String studentId) throws NotFoundOnServerException, InternalDataBaseException {
        return domainAdapter.getUserDtoByUnivCodeAndStudentId(univCode, studentId);
    }
    
    public UserDto findByToken(String token) throws NotFoundOnServerException, InternalDataBaseException { // TODO 토큰 만료되는거 구현하기
        return domainAdapter.getUserDtoByToken(token);
    }
    
    public UserDto save(UserDto userDto) throws InternalDataBaseException, CodeDuplicationException, BreakDataBaseRulesException { 
        return domainAdapter.saveUserDto(userDto);
    }
     
    public UserDto update(String univCode, String studentId, UserDto user) throws NotFoundOnServerException, InternalDataBaseException, CodeDuplicationException, BreakDataBaseRulesException {
        return domainAdapter.updateUserDto(univCode, studentId, user);
    }
}