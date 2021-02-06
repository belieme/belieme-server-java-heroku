package com.belieme.server.data.major;

import java.util.*;

import com.belieme.server.domain.exception.*;
import com.belieme.server.domain.major.*;
import com.belieme.server.data.RepositoryManager;
import com.belieme.server.data.department.*;
import com.belieme.server.data.university.*;

public class MajorDaoImpl implements MajorDao {
    private RepositoryManager repositoryManager;
    public MajorDaoImpl(RepositoryManager repositoryManager) {
        this.repositoryManager = repositoryManager;
    }
    
    public List<MajorDto> findAllByUnivCode(String univCode) throws InternalDataBaseException {
        List<MajorEntity> majorEntities = repositoryManager.getAllMajorEntitiesByUnivCode(univCode);
        
        return toMajorDtoList(majorEntities);
    }
    
    private List<MajorDto> toMajorDtoList(List<MajorEntity> majorEntityList) throws InternalDataBaseException {
        List<MajorDto> output = new ArrayList<>();
        for(int i = 0; i < majorEntityList.size(); i++) {
            output.add(toMajorDto(majorEntityList.get(i)));
        }
        return output;
    }
    
    private MajorDto toMajorDto(MajorEntity majorEntity) throws InternalDataBaseException {
        MajorDto output = new MajorDto();
        try {
            DepartmentEntity deptEntity = repositoryManager.getDeptEntityById(majorEntity.getDeptId());
            UniversityEntity univEntity = repositoryManager.getUnivEntityById(deptEntity.getUnivId());    
            output.setUnivCode(univEntity.getCode());
            output.setDeptCode(deptEntity.getCode());
        } catch(NotFoundOnDataBaseException e) {
            throw new InternalDataBaseException();
        }
        output.setCode(majorEntity.getCode());
        
        return output;
    }
    
    public List<MajorDto> findAllByUnivCodeAndDeptCode(String univCode, String deptCode) throws InternalDataBaseException {
        List<MajorEntity> majorEntityListByUnivCodeAndDeptCode = repositoryManager.getAllMajorEntitiesByUnivCodeAndDeptCode(univCode, deptCode);
        return toMajorDtoList(majorEntityListByUnivCodeAndDeptCode);
    }
    
    public MajorDto findByUnivCodeAndMajorCode(String univCode, String majorCode) throws InternalDataBaseException, NotFoundOnDataBaseException {
        MajorEntity majorEntityByUnivCodeAndMajorCode = repositoryManager.getMajorEntityByUnivCodeAndMajorCode(univCode, majorCode);
        return toMajorDto(majorEntityByUnivCodeAndMajorCode);
    }
    
    public MajorDto save(MajorDto major) throws NotFoundOnDataBaseException, InternalDataBaseException, CodeDuplicationException {
        repositoryManager.checkMajorDuplication(major.getUnivCode(), major.getCode());
        MajorEntity newMajor = new MajorEntity();
        
        int deptId = repositoryManager.getDeptEntityByUnivCodeAndDeptCode(major.getUnivCode(), major.getDeptCode()).getId();
        newMajor.setDeptId(deptId);
        newMajor.setCode(major.getCode());
        
        return toMajorDto(repositoryManager.saveMajor(newMajor));
    }
    
    public MajorDto update(String univCode, String majorCode, MajorDto major) throws NotFoundOnDataBaseException, InternalDataBaseException, CodeDuplicationException {
        MajorEntity target = repositoryManager.getMajorEntityByUnivCodeAndMajorCode(univCode, majorCode);
        if(univCode != major.getUnivCode() || majorCode != major.getCode()) {
            repositoryManager.checkMajorDuplication(major.getUnivCode(), major.getCode());   
            int deptId = repositoryManager.getDeptEntityByUnivCodeAndDeptCode(major.getUnivCode(), major.getDeptCode()).getId();
            target.setDeptId(deptId);
            target.setCode(major.getCode());
        }
        
        return toMajorDto(repositoryManager.saveMajor(target));
    }
}