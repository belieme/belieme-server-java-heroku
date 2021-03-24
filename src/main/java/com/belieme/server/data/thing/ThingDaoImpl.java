package com.belieme.server.data.thing;

import java.util.ArrayList;
import java.util.List;

import com.belieme.server.data.RepositoryManager;
import com.belieme.server.data.department.DepartmentEntity;
import com.belieme.server.data.university.UniversityEntity;
import com.belieme.server.domain.exception.*;
import com.belieme.server.domain.thing.*;

public class ThingDaoImpl implements ThingDao {
    private RepositoryManager repositoryManager;
    
    public ThingDaoImpl(RepositoryManager repositoryManager) {
        this.repositoryManager = repositoryManager;
    }
    
    public List<ThingDto> findByUnivCodeAndDeptCode(String univCode, String deptCode) throws InternalDataBaseException {
        List<ThingEntity> thingListFromDb = repositoryManager.getAllThingEntitiesByUnivCodeAndDeptCode(univCode, deptCode);
        List<ThingDto> output = new ArrayList<>();
        
        for(int i = 0; i < thingListFromDb.size(); i++) {
            output.add(toThingDto(thingListFromDb.get(i)));    
        }
        return output;
    }
    
    private ThingDto toThingDto(ThingEntity thingEntity) throws InternalDataBaseException {
        ThingDto output = new ThingDto();
        
        try {
            DepartmentEntity dept = repositoryManager.getDeptEntityById(thingEntity.getDeptId());    
            int univId = dept.getUnivId();
            String deptCode = dept.getCode();
            
        
            UniversityEntity univ = repositoryManager.getUnivEntityById(univId);
            String univCode = univ.getCode();
            output.setUnivCode(univCode);
            output.setDeptCode(deptCode);
            output.setCode(thingEntity.getCode());
            output.setName(thingEntity.getName());
            output.setDescription(thingEntity.getDescription());
            output.setEmoji(thingEntity.getEmoji());
        
            return output;
        } catch(NotFoundOnDataBaseException e) {
            throw new InternalDataBaseException("ThingDaoImpl.toThingDto()");
        }        
    }
    
    public ThingDto findByUnivCodeAndDeptCodeAndCode(String univCode, String deptCode, String code) throws NotFoundOnDataBaseException, InternalDataBaseException {
        return toThingDto(repositoryManager.getThingEntityByUnivCodeAndDeptCodeAndThingCode(univCode, deptCode, code));
    }
    
    public ThingDto save(ThingDto thing) throws NotFoundOnDataBaseException, InternalDataBaseException, CodeDuplicationException {
        repositoryManager.checkThingDuplication(thing.getUnivCode(), thing.getDeptCode(), thing.getCode());
        ThingEntity newThing = new ThingEntity();
        
        int deptId = repositoryManager.getDeptEntityByUnivCodeAndDeptCode(thing.getUnivCode(), thing.getDeptCode()).getId();
        newThing.setDeptId(deptId);
        newThing.setCode(thing.getCode());
        newThing.setDescription(thing.getDescription());
        newThing.setEmoji(thing.getEmoji());
        newThing.setName(thing.getName());
        
        return toThingDto(repositoryManager.saveThing(newThing));
    }
    
    public ThingDto update(String univCode, String deptCode, String code, ThingDto thing) throws NotFoundOnDataBaseException, InternalDataBaseException, CodeDuplicationException { // TODO 학교 바꾸는 거 같은거 가능??
        ThingEntity target = repositoryManager.getThingEntityByUnivCodeAndDeptCodeAndThingCode(univCode, deptCode, code);
        
        if(!univCode.equalsIgnoreCase(thing.getUnivCode()) || !deptCode.equalsIgnoreCase(thing.getDeptCode()) || !code.equalsIgnoreCase(thing.getCode())) {
            repositoryManager.checkThingDuplication(thing.getUnivCode(), thing.getDeptCode(), thing.getCode());
            int newDeptId = repositoryManager.getDeptEntityByUnivCodeAndDeptCode(thing.getUnivCode(), thing.getDeptCode()).getId();
            target.setDeptId(newDeptId);
        }
        
        target.setCode(thing.getCode());
        target.setDescription(thing.getDescription());
        target.setEmoji(thing.getEmoji());
        target.setName(thing.getName());
        
        return toThingDto(repositoryManager.saveThing(target));
    }
}