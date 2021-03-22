package com.belieme.server.data.university;

import java.util.*;
import com.belieme.server.domain.university.*;
import com.belieme.server.data.RepositoryManager;
import com.belieme.server.domain.exception.*;

public class UniversityDaoImpl implements UniversityDao {
    private RepositoryManager repositoryManager;
    
    public UniversityDaoImpl(RepositoryManager repositoryManager) {
        this.repositoryManager = repositoryManager;
    }
    
    public List<UniversityDto> findAllUnivs() {
        List<UniversityEntity> univEntityList = repositoryManager.getAllUnivEntities();
        List<UniversityDto> output = new ArrayList<UniversityDto>();
        for(int i = 0; i < univEntityList.size(); i++) {
            output.add(toUnivDto(univEntityList.get(i)));
        }
        return output;
    }
    
    private UniversityDto toUnivDto(UniversityEntity univEntity) {
        UniversityDto output = new UniversityDto();
        output.setCode(univEntity.getCode());
        output.setName(univEntity.getName());
        output.setApiUrl(univEntity.getApiUrl());
        
        return output;
    }
    
    public UniversityDto findByCode(String univCode) throws InternalDataBaseException, NotFoundOnDataBaseException {
        return toUnivDto(repositoryManager.getUnivEntityByUnivCode(univCode));
    }
    
    public UniversityDto save(UniversityDto univ) throws CodeDuplicationException {
        String univCode = univ.getCode();
        repositoryManager.checkUnivDuplicate(univCode);
        
        UniversityEntity target = new UniversityEntity();
        target.setCode(univCode);
        target.setName(univ.getName());
        target.setApiUrl(univ.getApiUrl());
        
        UniversityEntity savedEntity = repositoryManager.saveUniv(target);
        return toUnivDto(savedEntity);
    }
    

    
    public UniversityDto update(String univCode, UniversityDto univ) throws InternalDataBaseException, CodeDuplicationException, NotFoundOnDataBaseException {
        UniversityEntity target = repositoryManager.getUnivEntityByUnivCode(univCode);
        if(!univCode.equals(univ.getCode())) {
            repositoryManager.checkUnivDuplicate(univ.getCode());
        }
        
        target.setCode(univ.getCode());
        target.setName(univ.getName());
        target.setApiUrl(univ.getApiUrl());
        
        UniversityEntity savedEntity = repositoryManager.saveUniv(target);
        return toUnivDto(savedEntity);
    }
}