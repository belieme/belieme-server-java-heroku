package com.belieme.server.domain.item;

import java.util.List;
import com.belieme.server.domain.exception.*;

public interface ItemDao {
    public List<ItemDto> findAllByUnivCodeAndDeptCodeAndThingCode(String univCode, String deptCode, String thingCode) throws InternalDataBaseException;
    public ItemDto findByUnivCodeAndDeptCodeAndThingCodeAndItemNum(String univCode, String deptCode, String thingCode, int itemNum) throws NotFoundOnServerException, InternalDataBaseException;
    public ItemDto save(ItemDto item) throws InternalDataBaseException, CodeDuplicationException, BreakDataBaseRulesException;
    public ItemDto update(String univCode, String deptCode, String code, int num, ItemDto item) throws NotFoundOnServerException, InternalDataBaseException, CodeDuplicationException, BreakDataBaseRulesException;
}