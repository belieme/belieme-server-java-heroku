package com.belieme.server.domain.university;

import java.util.List;
import com.belieme.server.domain.exception.*;

public interface UniversityDao {
    public List<UniversityDto> findAllUnivs();
    public UniversityDto findByCode(String code) throws InternalDataBaseException, NotFoundOnServerException;
    public UniversityDto save(UniversityDto univ) throws CodeDuplicationException;
    public UniversityDto update(String code, UniversityDto univ) throws  InternalDataBaseException, CodeDuplicationException, NotFoundOnServerException;
}