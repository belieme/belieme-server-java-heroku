package com.belieme.server.data.history;

import java.util.List;

import com.belieme.server.data.common.*;

import com.belieme.server.domain.history.*;
import com.belieme.server.domain.exception.*;

public class HistoryDaoImpl implements HistoryDao {
    private DomainAdapter domainAdapter;
    
    public HistoryDaoImpl(RepositoryManager repositoryManager) {
        this.domainAdapter = new DomainAdapter(repositoryManager);
    }
    
    public List<HistoryDto> findAllByUnivCodeAndDeptCode(String univCode, String deptCode) throws InternalDataBaseException {
        return domainAdapter.getHistoryDtoListByUnivCodeAndDeptCode(univCode, deptCode);
    }
    
    public List<HistoryDto> findAllByUnivCodeAndDeptCodeAndUserId(String univCode, String deptCode, String userStudentId) throws InternalDataBaseException {
        return domainAdapter.getHistoryDtoListByUnivCodeAndDeptCodeAndStudentId(univCode, deptCode, userStudentId);
    }
    
    public List<HistoryDto> findAllByUnivCodeAndDeptCodeAndThingCodeAndItemNum(String univCode, String deptCode, String thingCode, int itemNum) throws InternalDataBaseException {
        return domainAdapter.getHistoryDtoListByUnivCodeAndDeptCodeAndThingCodeAndItemNum(univCode, deptCode, thingCode, itemNum);
    }
    
    public HistoryDto findByUnivCodeAndDeptCodeAndThingCodeAndItemNumAndHistoryNum(String univCode, String deptCode, String thingCode, int itemNum, int historyNum) throws NotFoundOnServerException, InternalDataBaseException {
        return domainAdapter.getHistoryDtoByUnivCodeAndDeptCodeAndThingCodeAndItemNumAndHistoryNum(univCode, deptCode, thingCode, itemNum, historyNum);
    }
    
    public HistoryDto save(HistoryDto history) throws InternalDataBaseException, CodeDuplicationException, BreakDataBaseRulesException { 
        return domainAdapter.saveHistoryDto(history);        
    }
    
    public HistoryDto update(String univCode, String deptCode, String thingCode, int itemNum, int historyNum, HistoryDto history) throws NotFoundOnServerException, InternalDataBaseException, CodeDuplicationException, BreakDataBaseRulesException {
        return domainAdapter.updateHistoryDto(univCode, deptCode, thingCode, itemNum, historyNum, history);
    }
}