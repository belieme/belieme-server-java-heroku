package com.belieme.server.domain.user;

import java.util.List;
import com.belieme.server.domain.exception.*;

public interface UserDao {
    public List<UserDto> findAllByUnivCodeAndDeptCode(String univCode, String deptCode) throws InternalDataBaseException;
    public UserDto findByUnivCodeAndStudentId(String univCode, String studentId) throws NotFoundOnDataBaseException, InternalDataBaseException;
    public UserDto findByToken(String token) throws NotFoundOnDataBaseException, InternalDataBaseException;
    public UserDto save(UserDto user) throws NotFoundOnDataBaseException, InternalDataBaseException, CodeDuplicationException;
    public UserDto update(String univCode, String studentId, UserDto user) throws NotFoundOnDataBaseException, InternalDataBaseException, CodeDuplicationException;
}