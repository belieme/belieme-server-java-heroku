package com.belieme.server.data.item;

import java.util.List;

import com.belieme.server.data.common.*;

import com.belieme.server.domain.exception.*;
import com.belieme.server.domain.item.*;

public class ItemDaoImpl implements ItemDao {
    private DomainAdapter domainAdapter;
    
    public ItemDaoImpl(RepositoryManager repositoryManager) {
        this.domainAdapter = new DomainAdapter(repositoryManager);
    }
    
    public List<ItemDto> findByUnivCodeAndDeptCodeAndThingCode(String univCode, String deptCode, String thingCode) throws InternalDataBaseException {
        return domainAdapter.getItemDtoListByUnivCodeAndDeptCodeAndThingCode(univCode, deptCode, thingCode);
    }
    
    public ItemDto findByUnivCodeAndDeptCodeAndThingCodeAndItemNum(String univCode, String deptCode, String thingCode, int itemNum) throws NotFoundOnServerException, InternalDataBaseException {
        return domainAdapter.getItemDtoByUnivCodeAndDeptCodeAndThingCodeAndItemNum(univCode, deptCode, thingCode, itemNum);
    }
    
    public ItemDto save(ItemDto item) throws InternalDataBaseException, CodeDuplicationException, BreakDataBaseRulesException {
        return domainAdapter.saveItemDto(item);
    }
    
    public ItemDto update(String univCode, String deptCode, String thingCode, int num, ItemDto item) throws NotFoundOnServerException, InternalDataBaseException, CodeDuplicationException, BreakDataBaseRulesException { // TODO 사용을 하는가?
        return domainAdapter.updateItemDto(univCode, deptCode, thingCode, num, item);
    }
}