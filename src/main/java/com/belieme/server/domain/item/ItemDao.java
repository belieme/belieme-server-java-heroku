package com.belieme.server.domain.item;

import java.util.List;
import com.belieme.server.domain.exception.*;

public interface ItemDao {
    public List<ItemDto> findByUnivCodeAndDeptCodeAndThingCode(String univCode, String deptCode, String thingCode) throws ServerDomainException;
    public ItemDto findByUnivCodeAndDeptCodeAndThingCodeAndNum(String univCode, String deptCode, String thingCode, int num) throws ServerDomainException;
    public ItemDto save(ItemDto item) throws ServerDomainException;
    public ItemDto update(String univCode, String deptCode, String code, int num, ItemDto item) throws ServerDomainException;
}