package com.belieme.server.data.user;

import java.util.*;

import com.belieme.server.domain.exception.*;
import com.belieme.server.domain.user.*;

import com.belieme.server.data.common.*;
import com.belieme.server.web.controller.GeneralApiController;
import com.belieme.server.web.exception.UnauthorizedException;
import org.springframework.http.ResponseEntity;

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
    
    public UserDto findByToken(String token) throws NotFoundOnServerException, InternalDataBaseException, TokenExpiredException { // TODO 토큰 만료되는거 구현하기
        UserDto userByToken = domainAdapter.getUserDtoByToken(token);
        if(System.currentTimeMillis()/1000 < userByToken.tokenExpiredTime() || userByToken.getCreateTimeStamp() == 0) {
            return userByToken;
        } else {
            throw new TokenExpiredException("토큰이 만료되었습니다.");
        }
    }
    
    public UserDto save(UserDto userDto) throws InternalDataBaseException, CodeDuplicationException, BreakDataBaseRulesException { 
        return domainAdapter.saveUserDto(userDto);
    }
     
    public UserDto update(String univCode, String studentId, UserDto user) throws NotFoundOnServerException, InternalDataBaseException, CodeDuplicationException, BreakDataBaseRulesException {
        return domainAdapter.updateUserDto(univCode, studentId, user);
    }
}