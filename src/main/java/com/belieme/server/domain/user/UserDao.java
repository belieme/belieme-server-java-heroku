package com.belieme.server.domain.user;

import java.util.List;
import com.belieme.server.domain.exception.*;

public interface UserDao {
    public List<UserDto> findAllByUnivCodeAndDeptCode(String univCode, String deptCode) throws ServerDomainException;
    public UserDto findByUnivCodeAndStudentId(String univCode, String studentId) throws ServerDomainException;
    public UserDto findByToken(String token) throws ServerDomainException;
    public UserDto save(UserDto user) throws ServerDomainException;
    public UserDto update(String univCode, String studentId, UserDto user) throws ServerDomainException;
}