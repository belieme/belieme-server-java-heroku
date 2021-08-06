package com.belieme.server.data.university;

import java.util.*;
import com.belieme.server.domain.university.*;
import com.belieme.server.data.common.*;
import com.belieme.server.domain.exception.*;

public class UniversityDaoImpl implements UniversityDao {
    private DomainAdapter domainAdapter;
    
    public UniversityDaoImpl(RepositoryManager repositoryManager) {
        this.domainAdapter = new DomainAdapter(repositoryManager);
    }
    
    public List<UniversityDto> findAllUnivs() {
        return domainAdapter.getUnivDtoList();
    }
    
    public UniversityDto findByCode(String univCode) throws InternalDataBaseException, NotFoundOnServerException {
        return domainAdapter.getUnivDtoByUnivCode(univCode);
    }
    
    public UniversityDto save(UniversityDto univ) throws CodeDuplicationException {
        return domainAdapter.saveUnivDto(univ);
    }
    
    public UniversityDto update(String univCode, UniversityDto univ) throws InternalDataBaseException, CodeDuplicationException, NotFoundOnServerException {
        return domainAdapter.updateUnivDto(univCode, univ);
    }
}