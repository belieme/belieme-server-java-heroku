package com.belieme.server.data.department;

import java.util.*;

import com.belieme.server.domain.department.*;
import com.belieme.server.domain.exception.*;
import com.belieme.server.data.RepositoryManager;
import com.belieme.server.data.university.*; 

public class DepartmentDaoImpl implements DepartmentDao {
    private RepositoryManager repositoryManager;
    
    public DepartmentDaoImpl(RepositoryManager repositoryManager) {
        this.repositoryManager = repositoryManager;
    }
    
    public List<DepartmentDto> findAllByUnivCode(String univCode) throws InternalDataBaseException {
        List<DepartmentEntity> deptListFromDb = repositoryManager.getAllDeptEntitiesByUnivCode(univCode);
        
        ArrayList<DepartmentDto> output = new ArrayList<>();
        for(int i = 0; i < deptListFromDb.size(); i++) {
            output.add(toDepartmentDto(deptListFromDb.get(i)));
        }
        return output;
    }

    
    private DepartmentDto toDepartmentDto(DepartmentEntity deptEntity) throws InternalDataBaseException {
        DepartmentDto output = new DepartmentDto();
        
        
        output.setCode(deptEntity.getCode());
        output.setName(deptEntity.getName());
        output.setAvailable(deptEntity.isAvailble());
        
        try {
            UniversityEntity univ = repositoryManager.getUnivEntityById(deptEntity.getUnivId());    
            output.setUnivCode(univ.getCode());
        } catch(NotFoundOnDataBaseException e) {
            throw new InternalDataBaseException("DepartmentDaoImpl.toDepartmentDto()");
        }
        return output;
    }
    
    public DepartmentDto findByUnivCodeAndDeptCode(String univCode, String deptCode) throws InternalDataBaseException, NotFoundOnDataBaseException {
        return toDepartmentDto(getDeptEntity(univCode, deptCode));
    }
    
    private DepartmentEntity getDeptEntity(String univCode, String deptCode) throws InternalDataBaseException, NotFoundOnDataBaseException {
        return repositoryManager.getDeptEntityByUnivCodeAndDeptCode(univCode, deptCode);
    }
    
    public DepartmentDto save(DepartmentDto dept) throws CodeDuplicationException, NotFoundOnDataBaseException, InternalDataBaseException {
        String univCode = dept.getUnivCode();
        String deptCode = dept.getCode();
        repositoryManager.checkDeptDuplication(univCode, deptCode);
        
        DepartmentEntity target = new DepartmentEntity();
        target.setCode(dept.getCode());
        target.setName(dept.getName());
        target.setAvailable(dept.isAvailable());

        int univId = repositoryManager.getUnivEntityByUnivCode(dept.getUnivCode()).getId();
        target.setUnivId(univId);
        
        DepartmentEntity savedEntity = repositoryManager.saveDept(target);
        return toDepartmentDto(savedEntity);
    }
    
    public DepartmentDto update(String univCode, String deptCode, DepartmentDto dept) throws NotFoundOnDataBaseException, CodeDuplicationException, InternalDataBaseException {
        DepartmentEntity target = getDeptEntity(univCode, deptCode);
        
        if(!univCode.equals(dept.getUnivCode()) || !deptCode.equals(dept.getCode())) {
            repositoryManager.checkDeptDuplication(dept.getUnivCode(), dept.getCode());
            int newUnivId = repositoryManager.getUnivEntityByUnivCode(dept.getUnivCode()).getId();
            target.setUnivId(newUnivId);
        }
        
        target.setCode(dept.getCode());
        target.setName(dept.getName());
        target.setAvailable(dept.isAvailable());
        
        DepartmentEntity savedEntity = repositoryManager.saveDept(target);
        return toDepartmentDto(savedEntity);
    }
}