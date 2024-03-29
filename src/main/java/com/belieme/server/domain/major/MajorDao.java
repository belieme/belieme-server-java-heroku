package com.belieme.server.domain.major;

import java.util.List;
import com.belieme.server.domain.exception.*;

public interface MajorDao {
    public List<MajorDto> findAllByUnivCode(String univCode) throws InternalDataBaseException;
    public List<MajorDto> findAllByUnivCodeAndDeptCode(String univCode, String deptCode) throws InternalDataBaseException;
    public MajorDto findByUnivCodeAndMajorCode(String univCode, String majorCode) throws InternalDataBaseException, NotFoundOnServerException;
    public MajorDto save(MajorDto major) throws BreakDataBaseRulesException, InternalDataBaseException, CodeDuplicationException;
    public MajorDto update(String univCode, String majorCode, MajorDto major) throws NotFoundOnServerException, BreakDataBaseRulesException, InternalDataBaseException, CodeDuplicationException;
}