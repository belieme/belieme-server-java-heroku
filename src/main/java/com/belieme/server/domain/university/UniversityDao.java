package com.belieme.server.domain.university;

import java.util.List;
import com.belieme.server.domain.exception.*;

public interface UniversityDao {
    public List<UniversityDto> findAllUnivs() throws ServerDomainException;
    public UniversityDto findByCode(String code) throws ServerDomainException;
    public UniversityDto save(UniversityDto univ) throws ServerDomainException;
    public UniversityDto update(String code, UniversityDto univ) throws ServerDomainException;
}