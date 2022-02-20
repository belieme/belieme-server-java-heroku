package com.belieme.server.domain.history;

import java.util.List;
import com.belieme.server.domain.exception.*;

public interface HistoryDao {
    public List<HistoryDto> findAllByUnivCodeAndDeptCode(String univCode, String deptCode) throws InternalDataBaseException;
    public List<HistoryDto> findAllByUnivCodeAndDeptCodeAndUserId(String univCode, String deptCode, String userId) throws InternalDataBaseException;
    public List<HistoryDto> findAllByUnivCodeAndDeptCodeAndThingCodeAndItemNum(String univCode, String deptCode, String thingCode, int itemNum) throws InternalDataBaseException;
    public HistoryDto findByUnivCodeAndDeptCodeAndThingCodeAndItemNumAndHistoryNum(String univCode, String deptCode, String thingCode, int itemNum, int historyNum) throws NotFoundOnServerException, InternalDataBaseException;
    public HistoryDto save(HistoryDto history) throws InternalDataBaseException, CodeDuplicationException, BreakDataBaseRulesException;
    public HistoryDto update(String univCode, String deptCode, String thingCode, int itemNum, int historyNum, HistoryDto history) throws NotFoundOnServerException, InternalDataBaseException, CodeDuplicationException, BreakDataBaseRulesException;
}