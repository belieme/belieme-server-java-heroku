package com.belieme.server.data.item;

import java.util.ArrayList;
import java.util.List;

import com.belieme.server.data.RepositoryManager;
import com.belieme.server.data.department.DepartmentEntity;
import com.belieme.server.data.event.EventEntity;
import com.belieme.server.data.thing.ThingEntity;
import com.belieme.server.data.university.UniversityEntity;
import com.belieme.server.domain.exception.*;
import com.belieme.server.domain.item.*;

public class ItemDaoImpl implements ItemDao {
    private RepositoryManager repositoryManager;
    
    public ItemDaoImpl(RepositoryManager repositoryManager) {
        this.repositoryManager = repositoryManager;
    }
    
    private ItemDto toItemDto(ItemEntity itemEntity) throws InternalDataBaseException {
        ItemDto output = new ItemDto();
        
        try {
            ThingEntity thing = repositoryManager.getThingEntity(itemEntity.getThingId());
            int deptId = thing.getDeptId();
            String thingCode = thing.getCode();
            
            DepartmentEntity dept = repositoryManager.getDeptEntityById(deptId);    
            int univId = dept.getUnivId();
            String deptCode = dept.getCode();
            
            UniversityEntity univ = repositoryManager.getUnivEntityById(univId);
            String univCode = univ.getCode();
            
            output.setUnivCode(univCode);
            output.setDeptCode(deptCode);
            output.setThingCode(thingCode);
            output.setNum(itemEntity.getNum());
            
            if(itemEntity.getLastEventId() == 0) {
                output.setLastEventNum(0);    
            } else {
                EventEntity lastEvent = repositoryManager.getEventEntityById(itemEntity.getLastEventId());
                output.setLastEventNum(lastEvent.getNum());    
            }
            return output;
        } catch(NotFoundOnDataBaseException e) {
            throw new InternalDataBaseException("ItemDaoImpl.toItemDto()");
        }    
    }
    
    public List<ItemDto> findByUnivCodeAndDeptCodeAndThingCode(String univCode, String deptCode, String thingCode) throws InternalDataBaseException {
        List<ItemEntity> itemListFromDb = repositoryManager.getAllItemEntitiesByUnivCodeAndDeptCodeAndThingCode(univCode, deptCode, thingCode);
        List<ItemDto> output = new ArrayList<>();
        for(int i = 0; i < itemListFromDb.size(); i++) {
            output.add(toItemDto(itemListFromDb.get(i)));
        }
        return output;
    }
    
    public ItemDto findByUnivCodeAndDeptCodeAndThingCodeAndNum(String univCode, String deptCode, String thingCode, int num) throws NotFoundOnDataBaseException, InternalDataBaseException {
        ItemEntity itemEntity = repositoryManager.getItemEntityByUnivCodeAndDeptCodeAndThingCodeAndItemNum(univCode, deptCode, thingCode, num);
        return toItemDto(itemEntity);
    }
    
    public ItemDto save(ItemDto item) throws NotFoundOnDataBaseException, InternalDataBaseException, CodeDuplicationException {
        repositoryManager.checkItemDuplication(item.getUnivCode(), item.getDeptCode(), item.getThingCode(), item.getNum());
        ItemEntity newItem = new ItemEntity();
        
        int thingId = repositoryManager.getThingEntityByUnivCodeAndDeptCodeAndThingCode(item.getUnivCode(), item.getDeptCode(), item.getThingCode()).getId();
        newItem.setThingId(thingId);
        newItem.setNum(item.getNum());
        if(item.getLastEventNum() == 0) {
            newItem.setLastEventId(0);
        } else {
            int lastEventId = repositoryManager.getEventEntityByUnivCodeAndDeptCodeAndThingCodeAndItemNumAndEventNum(item.getUnivCode(), item.getDeptCode(), item.getThingCode(), item.getNum(), item.getLastEventNum()).getId();
            newItem.setLastEventId(lastEventId);            
        }
        return toItemDto(repositoryManager.saveItem(newItem));
    }
    
    public ItemDto update(String univCode, String deptCode, String thingCode, int num, ItemDto item) throws NotFoundOnDataBaseException, InternalDataBaseException, CodeDuplicationException {
        ItemEntity target = repositoryManager.getItemEntityByUnivCodeAndDeptCodeAndThingCodeAndItemNum(univCode, deptCode, thingCode, num);
        
        if(!univCode.equals(item.getUnivCode()) || !deptCode.equals(item.getDeptCode()) || !thingCode.equals(item.getThingCode()) || num != item.getNum()) {
            repositoryManager.checkItemDuplication(item.getUnivCode(), item.getDeptCode(), item.getThingCode(), item.getNum());
            int newThingId = repositoryManager.getThingEntityByUnivCodeAndDeptCodeAndThingCode(item.getUnivCode(), item.getDeptCode(), item.getThingCode()).getId();
            target.setThingId(newThingId);
        }
        target.setNum(item.getNum());
        
        if(item.getLastEventNum() == 0) {
            target.setLastEventId(0);
        } else {
            int lastEventId = repositoryManager.getEventEntityByUnivCodeAndDeptCodeAndThingCodeAndItemNumAndEventNum(item.getUnivCode(), item.getDeptCode(), item.getThingCode(), item.getNum(), item.getLastEventNum()).getId();
            target.setLastEventId(lastEventId);            
        }
        return toItemDto(repositoryManager.saveItem(target));
    }
}