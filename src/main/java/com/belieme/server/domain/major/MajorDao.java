package com.belieme.server.domain.major;

import java.util.List;
import com.belieme.server.domain.exception.*;

public interface MajorDao {
    public List<MajorDto> findAllByUnivCode(String univCode) throws ServerDomainException;
    public List<MajorDto> findAllByUnivCodeAndDeptCode(String univCode, String deptCode) throws ServerDomainException;
    public MajorDto findByUnivCodeAndMajorCode(String univCode, String majorCode) throws ServerDomainException;
    public MajorDto save(MajorDto major) throws ServerDomainException;
    public MajorDto update(String univCode, String majorCode, MajorDto major) throws ServerDomainException;
}