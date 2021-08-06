package com.belieme.server.domain.user;

import java.util.List;
import com.belieme.server.domain.exception.*;

public interface UserDao {
    public List<UserDto> findAllByUnivCodeAndDeptCode(String univCode, String deptCode) throws InternalDataBaseException;
    public UserDto findByUnivCodeAndStudentId(String univCode, String studentId) throws NotFoundOnServerException, InternalDataBaseException;
    public UserDto findByToken(String token) throws NotFoundOnServerException, InternalDataBaseException;
    public UserDto save(UserDto user) throws InternalDataBaseException, CodeDuplicationException, BreakDataBaseRulesException;
    public UserDto update(String univCode, String studentId, UserDto user) throws NotFoundOnServerException, InternalDataBaseException, CodeDuplicationException, BreakDataBaseRulesException;
}