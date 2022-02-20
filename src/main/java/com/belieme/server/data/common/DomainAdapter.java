package com.belieme.server.data.common;

import java.util.*;

import org.apache.commons.logging.Log;

import com.belieme.server.data.university.*;
import com.belieme.server.data.department.*;
import com.belieme.server.data.major.*;
import com.belieme.server.data.user.*;
import com.belieme.server.data.permission.*;
import com.belieme.server.data.thing.*;
import com.belieme.server.data.item.*;
import com.belieme.server.data.history.*;

import com.belieme.server.domain.university.*;
import com.belieme.server.domain.department.*;
import com.belieme.server.domain.major.*;
import com.belieme.server.domain.user.*;
import com.belieme.server.domain.permission.*;
import com.belieme.server.domain.thing.*;
import com.belieme.server.domain.item.*;
import com.belieme.server.domain.history.*;

import com.belieme.server.domain.exception.*;
import com.belieme.server.data.exception.*;

public class DomainAdapter {
    private RepositoryManager repositoryManager;
    
    public DomainAdapter(RepositoryManager repositoryManager) {
        this.repositoryManager = repositoryManager;
    }
        
    public List<UniversityDto> getUnivDtoList() {
        return toUnivDtoList(repositoryManager.getAllUnivEntities());
    }
        
    public UniversityDto getUnivDtoByUnivCode(String univCode) throws NotFoundOnServerException, InternalDataBaseException {
        UniversityEntity univEntity = getUnivEntityByUnivCode(univCode);
        return toUnivDto(univEntity);
    }
    
    public UniversityDto saveUnivDto(UniversityDto univDto) throws CodeDuplicationException {
        checkUnivDuplication(univDto);
        
        UniversityEntity target = new UniversityEntity();
        target.setCode(univDto.getCode());
        target.setName(univDto.getName());
        target.setApiUrl(univDto.getApiUrl());
        
        UniversityEntity savedEntity = repositoryManager.saveUniv(target);
        return toUnivDto(savedEntity);
    }
    
    public UniversityDto updateUnivDto(String univCode, UniversityDto univDto) throws NotFoundOnServerException, InternalDataBaseException, CodeDuplicationException {
        UniversityEntity univEntity = getUnivEntityByUnivCode(univCode);
        
        if(!univCode.equalsIgnoreCase(univDto.getCode())) {
            checkUnivDuplication(univDto);
        }
        
        univEntity.setCode(univDto.getCode());
        univEntity.setName(univDto.getName());
        univEntity.setApiUrl(univDto.getApiUrl());
        
        UniversityEntity savedEntity = repositoryManager.saveUniv(univEntity);
        return toUnivDto(savedEntity);
    }
    
    public List<DepartmentDto> getDeptDtoListByUnivCode(String univCode) throws InternalDataBaseException {
        List<DepartmentEntity> deptEntityList = getDeptEntityListByUnivCode(univCode);
        
        return toDeptDtoList(deptEntityList);
    }
    
    public DepartmentDto getDeptDtoByUnivCodeAndDeptCode(String univCode, String deptCode) throws InternalDataBaseException, NotFoundOnServerException {
        DepartmentEntity deptEntity = getDeptEntityByUnivCodeAndDeptCode(univCode, deptCode);
        return toDeptDto(deptEntity);
    }
    
    public DepartmentDto saveDeptDto(DepartmentDto dept) throws CodeDuplicationException, InternalDataBaseException, BreakDataBaseRulesException {
        checkDeptDuplication(dept);
        
        DepartmentEntity target = new DepartmentEntity();
        target.setCode(dept.getCode());
        target.setName(dept.getName());
        target.setAvailable(dept.isAvailable());

        UniversityEntity univEntity = getUnivEntityByUnivCodeForSave(dept.getUnivCode());
        target.setUnivId(univEntity.getId());
        
        DepartmentEntity savedEntity = repositoryManager.saveDept(target);
        return toDeptDto(savedEntity);
    }
    
    public DepartmentDto updateDeptDto(String univCode, String deptCode, DepartmentDto dept) throws CodeDuplicationException, InternalDataBaseException, BreakDataBaseRulesException, NotFoundOnServerException {
        DepartmentEntity deptEntity = getDeptEntityByUnivCodeAndDeptCode(univCode, deptCode);
        
        if(!univCode.equalsIgnoreCase(dept.getUnivCode()) || !deptCode.equalsIgnoreCase(dept.getCode())) {
            checkDeptDuplication(dept);
            UniversityEntity univEntity = getUnivEntityByUnivCodeForSave(dept.getUnivCode());
            deptEntity.setUnivId(univEntity.getId());
        }
        
        deptEntity.setCode(dept.getCode());
        deptEntity.setName(dept.getName());
        deptEntity.setAvailable(dept.isAvailable());
        
        DepartmentEntity savedEntity = repositoryManager.saveDept(deptEntity);
        return toDeptDto(savedEntity);
    }
    
    public List<MajorDto> getMajorDtoListByUnivCode(String univCode) throws InternalDataBaseException {
        List<MajorEntity> majorEntities = getMajorEntityListByUnivCode(univCode);
        
        return toMajorDtoList(majorEntities);
    }
    
    public List<MajorDto> getMajorDtoListByUnivCodeAndDeptCode(String univCode, String deptCode) throws InternalDataBaseException {
        List<MajorEntity> majorEntityListByUnivCodeAndDeptCode = getMajorEntityListByUnivCodeAndDeptCode(univCode, deptCode);
        return toMajorDtoList(majorEntityListByUnivCodeAndDeptCode);
    }
    
    public MajorDto getMajorDtoByUnivCodeAndMajorCode(String univCode, String majorCode) throws InternalDataBaseException, NotFoundOnServerException {
        MajorEntity majorEntityByUnivCodeAndMajorCode = getMajorEntityByUnivCodeAndMajorCode(univCode, majorCode);
        return toMajorDto(majorEntityByUnivCodeAndMajorCode);
    }
    
    public MajorDto saveMajorDto(MajorDto major) throws InternalDataBaseException, BreakDataBaseRulesException, CodeDuplicationException {
        checkMajorDuplication(major);
        
        MajorEntity newMajor = new MajorEntity();
        DepartmentEntity deptEntity = getDeptEntityByUnivCodeAndDeptCodeForSave(major.getUnivCode(), major.getDeptCode());
        newMajor.setDeptId(deptEntity.getId());
        newMajor.setCode(major.getCode());
        
        MajorDto tmp = toMajorDto(repositoryManager.saveMajor(newMajor));
        
        return tmp;
    }
    
    public MajorDto updateMajorDto(String univCode, String majorCode, MajorDto major) throws NotFoundOnServerException, InternalDataBaseException, CodeDuplicationException, BreakDataBaseRulesException {
        MajorEntity target = getMajorEntityByUnivCodeAndMajorCode(univCode, majorCode);
        if(!univCode.equalsIgnoreCase(major.getUnivCode()) || !majorCode.equalsIgnoreCase(major.getCode())) {
            checkMajorDuplication(major);  
            
            DepartmentEntity deptEntity = getDeptEntityByUnivCodeAndDeptCodeForSave(major.getUnivCode(), major.getDeptCode());
            target.setDeptId(deptEntity.getId());
            target.setCode(major.getCode());
        }
        
        return toMajorDto(repositoryManager.saveMajor(target));
    }

    public List<PermissionDto> getPermissionDtoListByUnivCodeAndStudentId(String univCode, String studentId) throws InternalDataBaseException {
        List<PermissionEntity> permissionEntityList = getPermissionEntityListByUnivCodeAndStudentId(univCode, studentId);
        return toPermissionDtoList(permissionEntityList);
    }
    
    public PermissionDto getPermissionDtoByUnivCodeAndStudentIdAndDeptCode(String univCode, String studentId, String deptCode) throws NotFoundOnServerException, InternalDataBaseException {
        PermissionEntity permissionEntity = getPermissionEntityByUnivCodeAndStudentIdAndDeptCode(univCode, studentId, deptCode);
        return toPermissionDto(permissionEntity);
    }
    
    public PermissionDto savePermissionDto(PermissionDto permission) throws BreakDataBaseRulesException, InternalDataBaseException, CodeDuplicationException {
        checkPermissionDuplication(permission);
        
        PermissionEntity target = new PermissionEntity();
        DepartmentEntity deptEntity = getDeptEntityByUnivCodeAndDeptCodeForSave(permission.getUnivCode(), permission.getDeptCode());
        target.setDeptId(deptEntity.getId());
        
        UserEntity userEntity = getUserEntityByUnivCodeAndStudentIdForSave(permission.getUnivCode(), permission.getStudentId());
        target.setUserId(userEntity.getId());
        target.setPermission(permission.getPermission().name());
        
        PermissionDto output = toPermissionDto(repositoryManager.savePermission(target));
        return output;
    }
    
    
    // TODO permission update 코드 분석 후 생각 좀 더 해보기 univCode, studentId, deptCode를 바꿀 수 있게 해야하는지,, 이런거
    public PermissionDto updatePermissionDto(String univCode, String studentId, String deptCode, PermissionDto permission) throws NotFoundOnServerException, BreakDataBaseRulesException, InternalDataBaseException, CodeDuplicationException {
        PermissionEntity target = getPermissionEntityByUnivCodeAndStudentIdAndDeptCode(univCode, studentId, deptCode);
        
        if(!univCode.equalsIgnoreCase(permission.getUnivCode()) || !studentId.equals(permission.getStudentId()) || !deptCode.equalsIgnoreCase(permission.getDeptCode())) {
            checkPermissionDuplication(permission);  
            DepartmentEntity deptEntity = getDeptEntityByUnivCodeAndDeptCodeForSave(permission.getUnivCode(), permission.getDeptCode());
            target.setDeptId(deptEntity.getId());
        
            UserEntity userEntity = getUserEntityByUnivCodeAndStudentIdForSave(permission.getUnivCode(), permission.getStudentId());
            target.setUserId(userEntity.getId());
        }
        target.setPermission(permission.getPermission().name());
        
        PermissionDto output = toPermissionDto(repositoryManager.savePermission(target));
        return output;
    }
    
    public List<UserDto> getUserDtoListByUnivCodeAndDeptCode(String univCode, String deptCode) throws InternalDataBaseException {
        List<UserEntity> userEntityList = getUserEntityListByUnivCodeAndDeptCode(univCode, deptCode);
        return toUserDtoList(userEntityList);
    }
    
    public UserDto getUserDtoByUnivCodeAndStudentId(String univCode, String studentId) throws NotFoundOnServerException, InternalDataBaseException {
        UserEntity userEntity = getUserEntityByUnivCodeAndStudentId(univCode, studentId);
        return toUserDto(userEntity);
    }
    
    public UserDto getUserDtoByToken(String token) throws NotFoundOnServerException, InternalDataBaseException {
        UserEntity userEntity = getUserEntityByToken(token);
        return toUserDto(userEntity);
    }
    
    public UserDto saveUserDto(UserDto userDto) throws InternalDataBaseException, CodeDuplicationException, BreakDataBaseRulesException {
        checkUserDuplicationOnUnivCodeAndStudentId(userDto);
        checkUserDuplicationOnToken(userDto);
        
        UserEntity target = new UserEntity();
        
        UniversityEntity univEntity = getUnivEntityByUnivCodeForSave(userDto.getUnivCode());
        target.setUnivId(univEntity.getId());
    
        target.setStudentId(userDto.getStudentId());
        target.setName(userDto.getName());
        target.setEntranceYear(userDto.getEntranceYear());
        target.setCreateTimeStamp(userDto.getCreateTimeStamp());
        target.setApprovalTimeStamp(userDto.getApprovalTimeStamp());
        target.setToken(userDto.getToken());
        
        UserEntity savedUser = repositoryManager.saveUser(target);
        
        Map<String, Permissions> permissions = userDto.getPermissions();
        for (Map.Entry<String, Permissions> entry : permissions.entrySet()) {
            PermissionEntity tmp = findOrMakeNewPermissionEntity(userDto.getUnivCode(), entry.getKey(), userDto.getStudentId());
            tmp.setPermission(entry.getValue().name());
            repositoryManager.savePermission(tmp);
        }
        
        return toUserDto(savedUser);
    }
    
    public UserDto updateUserDto(String univCode, String studentId, UserDto user) throws NotFoundOnServerException, InternalDataBaseException, CodeDuplicationException, BreakDataBaseRulesException {
        UserEntity target = getUserEntityByUnivCodeAndStudentId(univCode, studentId);
        
        if(!univCode.equalsIgnoreCase(user.getUnivCode()) || !studentId.equals(user.getStudentId())) {
            checkUserDuplicationOnUnivCodeAndStudentId(user);
            
            UniversityEntity univEntity = getUnivEntityByUnivCodeForSave(user.getUnivCode());
            target.setUnivId(univEntity.getId());
            
            target.setStudentId(user.getStudentId());
        }
        
        if(!target.getToken().equals(user.getToken())) {
            checkUserDuplicationOnToken(user);
            target.setToken(user.getToken());
        }
        
        target.setName(user.getName());
        target.setEntranceYear(user.getEntranceYear());
        target.setCreateTimeStamp(user.getCreateTimeStamp());
        target.setApprovalTimeStamp(user.getApprovalTimeStamp());
        
        Map<String, Permissions> permissions = user.getPermissions();
        for (Map.Entry<String, Permissions> entry : permissions.entrySet()) {
            PermissionEntity tmp = findOrMakeNewPermissionEntity(univCode, entry.getKey(), user.getStudentId());
            tmp.setPermission(entry.getValue().name());
            repositoryManager.savePermission(tmp);
        }
        
        UserEntity newUser = repositoryManager.saveUser(target);
        return toUserDto(newUser);
    }
    
    public List<ThingDto> getThingDtoListByUnivCodeAndDeptCode(String univCode, String deptCode) throws InternalDataBaseException {
        List<ThingEntity> thingEntityList = getThingEntityListByUnivCodeAndDeptCode(univCode, deptCode);
        return toThingDtoList(thingEntityList);
    }
    
    public ThingDto getThingDtoByUnivCodeAndDeptCodeAndThingCode(String univCode, String deptCode, String thingCode) throws NotFoundOnServerException, InternalDataBaseException {
        ThingEntity thingEntity = getThingEntityByUnivCodeAndDeptCodeAndThingCode(univCode, deptCode, thingCode);
        return toThingDto(thingEntity);
    }
    
    public ThingDto saveThingDto(ThingDto thingDto) throws InternalDataBaseException, CodeDuplicationException, BreakDataBaseRulesException {
        checkThingDuplication(thingDto);
        ThingEntity target = new ThingEntity();
        
        DepartmentEntity deptEntity = getDeptEntityByUnivCodeAndDeptCodeForSave(thingDto.getUnivCode(), thingDto.getDeptCode());
        target.setDeptId(deptEntity.getId());
        
        target.setCode(thingDto.getCode());
        target.setDescription(thingDto.getDescription());
        target.setEmoji(thingDto.getEmoji());
        target.setName(thingDto.getName());
        
        return toThingDto(repositoryManager.saveThing(target));
    }
    
    public ThingDto updateThingDto(String univCode, String deptCode, String code, ThingDto thingDto) throws NotFoundOnServerException, InternalDataBaseException, CodeDuplicationException, BreakDataBaseRulesException {
        ThingEntity target = getThingEntityByUnivCodeAndDeptCodeAndThingCode(univCode, deptCode, code);
        
        if(!univCode.equalsIgnoreCase(thingDto.getUnivCode()) || !deptCode.equalsIgnoreCase(thingDto.getDeptCode()) || !code.equalsIgnoreCase(thingDto.getCode())) {
            checkThingDuplication(thingDto);
            DepartmentEntity deptEntity = getDeptEntityByUnivCodeAndDeptCodeForSave(thingDto.getUnivCode(), thingDto.getDeptCode());
            target.setDeptId(deptEntity.getId());
        }
        
        target.setCode(thingDto.getCode());
        target.setDescription(thingDto.getDescription());
        target.setEmoji(thingDto.getEmoji());
        target.setName(thingDto.getName());
        
        return toThingDto(repositoryManager.saveThing(target));
    }
    
    public List<ItemDto> getItemDtoListByUnivCodeAndDeptCodeAndThingCode(String univCode, String deptCode, String thingCode) throws InternalDataBaseException {
        List<ItemEntity> itemEntityList = getItemEntityListByUnivCodeAndDeptCodeAndThingCode(univCode, deptCode, thingCode);
        return toItemDtoList(itemEntityList);
    }
    
    public ItemDto getItemDtoByUnivCodeAndDeptCodeAndThingCodeAndItemNum(String univCode, String deptCode, String thingCode, int itemNum) throws NotFoundOnServerException, InternalDataBaseException {
        ItemEntity itemEntity = getItemEntityByUnivCodeAndDeptCodeAndThingCodeAndItemNum(univCode, deptCode, thingCode, itemNum);
        return toItemDto(itemEntity);
    }
    
    public ItemDto saveItemDto(ItemDto itemDto) throws InternalDataBaseException, CodeDuplicationException, BreakDataBaseRulesException {
        checkItemDuplication(itemDto);
        
        ItemEntity target = new ItemEntity();
        
        ThingEntity thingEntity = getThingEntityByUnivCodeAndDeptCodeAndThingCodeForSave(itemDto.getUnivCode(), itemDto.getDeptCode(), itemDto.getThingCode());
        target.setThingId(thingEntity.getId());
        
        target.setNum(itemDto.getNum());
        
        if(itemDto.getLastHistoryNum() == 0) {
            target.setLastHistoryId(0);    
        } else {
            HistoryEntity historyEntity = getHistoryEntityByUnivCodeAndDeptCodeAndThingCodeAndItemNumAndHistoryNumForSave(itemDto.getUnivCode(), itemDto.getDeptCode(), itemDto.getThingCode(), itemDto.getNum(), itemDto.getLastHistoryNum());
            target.setLastHistoryId(historyEntity.getId());    
        }
        
        return toItemDto(repositoryManager.saveItem(target));
    }
    
    public ItemDto updateItemDto(String univCode, String deptCode, String thingCode, int itemNum, ItemDto itemDto) throws NotFoundOnServerException, InternalDataBaseException, CodeDuplicationException, BreakDataBaseRulesException {
        ItemEntity target = getItemEntityByUnivCodeAndDeptCodeAndThingCodeAndItemNum(univCode, deptCode, thingCode, itemNum);
        
        if(!univCode.equalsIgnoreCase(itemDto.getUnivCode()) || !deptCode.equalsIgnoreCase(itemDto.getDeptCode()) || !thingCode.equalsIgnoreCase(itemDto.getThingCode()) || itemNum != itemDto.getNum()) {
            checkItemDuplication(itemDto);
            ThingEntity thingEntity = getThingEntityByUnivCodeAndDeptCodeAndThingCodeForSave(itemDto.getUnivCode(), itemDto.getDeptCode(), itemDto.getThingCode());
            target.setThingId(thingEntity.getId());
        }
        target.setNum(itemDto.getNum());
        
        if(itemDto.getLastHistoryNum() == 0) {
            target.setLastHistoryId(0);    
        } else {
            HistoryEntity historyEntity = getHistoryEntityByUnivCodeAndDeptCodeAndThingCodeAndItemNumAndHistoryNumForSave(itemDto.getUnivCode(), itemDto.getDeptCode(), itemDto.getThingCode(), itemDto.getNum(), itemDto.getLastHistoryNum());
            target.setLastHistoryId(historyEntity.getId());    
        }
        
        return toItemDto(repositoryManager.saveItem(target));
    }
    
    public List<HistoryDto> getHistoryDtoListByUnivCodeAndDeptCode(String univCode, String deptCode) throws InternalDataBaseException {
        List<HistoryEntity> historyEntityList = getHistoryEntityListByUnivCodeAndDeptCode(univCode, deptCode);
        return toHistoryDtoList(historyEntityList);
    }
    
    public List<HistoryDto> getHistoryDtoListByUnivCodeAndDeptCodeAndStudentId(String univCode, String deptCode, String studentId) throws InternalDataBaseException {
        List<HistoryEntity> historyEntityList = getHistoryEntityListByUnivCodeAndDeptCodeAndStudentId(univCode, deptCode, studentId);
        return toHistoryDtoList(historyEntityList);
    }
    
    public List<HistoryDto> getHistoryDtoListByUnivCodeAndDeptCodeAndThingCodeAndItemNum(String univCode, String deptCode, String thingCode, int itemNum) throws InternalDataBaseException {
        List<HistoryEntity> historyEntityList = getHistoryEntityListByUnivCodeAndDeptCodeAndThingCodeAndItemNum(univCode, deptCode, thingCode, itemNum);
        return toHistoryDtoList(historyEntityList);
    }
    
    public HistoryDto getHistoryDtoByUnivCodeAndDeptCodeAndThingCodeAndItemNumAndHistoryNum(String univCode, String deptCode, String thingCode, int itemNum, int historyNum) throws InternalDataBaseException, NotFoundOnServerException {
        HistoryEntity historyEntity = getHistoryEntityByUnivCodeAndDeptCodeAndThingCodeAndItemNumAndHistoryNum(univCode, deptCode, thingCode, itemNum, historyNum);
        return toHistoryDto(historyEntity);
    }
    
    public HistoryDto saveHistoryDto(HistoryDto historyDto) throws InternalDataBaseException, CodeDuplicationException, BreakDataBaseRulesException {
        checkHistoryDuplication(historyDto);
        
        HistoryEntity target = new HistoryEntity();
        
        ItemEntity itemEntity = getItemEntityByUnivCodeAndDeptCodeAndThingCodeAndItemNumForSave(historyDto.getUnivCode(), historyDto.getDeptCode(), historyDto.getThingCode(), historyDto.getItemNum());
        target.setItemId(itemEntity.getId());
        
        target.setNum(historyDto.getNum());
        
        if(historyDto.getUserStudentId() == null) {
            target.setUserId(0);
        } else {
            UserEntity userEntity = getUserEntityByUnivCodeAndStudentIdForSave(historyDto.getUnivCode(), historyDto.getUserStudentId());    
            target.setUserId(userEntity.getId());
        }
        
        if(historyDto.getApproveManagerStudentId() == null) {
            target.setApproveManagerId(0);
        } else {
            UserEntity approveManagerEntity = getUserEntityByUnivCodeAndStudentIdForSave(historyDto.getUnivCode(), historyDto.getApproveManagerStudentId());
            target.setApproveManagerId(approveManagerEntity.getId());
        }
        
        if(historyDto.getReturnManagerStudentId() == null) {
            target.setReturnManagerId(0);
        } else {
            UserEntity returnManagerEntity = getUserEntityByUnivCodeAndStudentIdForSave(historyDto.getUnivCode(), historyDto.getReturnManagerStudentId());    
            target.setReturnManagerId(returnManagerEntity.getId());
        }
        
        if(historyDto.getLostManagerStudentId() == null) {
            target.setLostManagerId(0);
        } else {
            UserEntity lostManagerEntity = getUserEntityByUnivCodeAndStudentIdForSave(historyDto.getUnivCode(), historyDto.getLostManagerStudentId());
            target.setLostManagerId(lostManagerEntity.getId());
        }
        
        target.setReserveTimeStamp(historyDto.getReserveTimeStamp());
        target.setApproveTimeStamp(historyDto.getApproveTimeStamp());
        target.setReturnTimeStamp(historyDto.getReturnTimeStamp());
        target.setCancelTimeStamp(historyDto.getCancelTimeStamp());
        target.setLostTimeStamp(historyDto.getLostTimeStamp());
        
        HistoryEntity savedHistoryEntity = repositoryManager.saveHistory(target);
        
        itemEntity.setLastHistoryId(savedHistoryEntity.getId());
        repositoryManager.saveItem(itemEntity);
        
        return toHistoryDto(savedHistoryEntity);
    }
    
    public HistoryDto updateHistoryDto(String univCode, String deptCode, String thingCode, int itemNum, int historyNum, HistoryDto historyDto) throws NotFoundOnServerException, InternalDataBaseException, CodeDuplicationException, BreakDataBaseRulesException {
        HistoryEntity target = getHistoryEntityByUnivCodeAndDeptCodeAndThingCodeAndItemNumAndHistoryNum(univCode, deptCode, thingCode, itemNum, historyNum);
        
        if(!univCode.equalsIgnoreCase(historyDto.getUnivCode()) || !deptCode.equalsIgnoreCase(historyDto.getDeptCode()) || !thingCode.equalsIgnoreCase(historyDto.getThingCode()) || itemNum != historyDto.getItemNum() || historyNum != historyDto.getNum()) {
            checkHistoryDuplication(historyDto);
            ItemEntity itemEntity = getItemEntityByUnivCodeAndDeptCodeAndThingCodeAndItemNumForSave(historyDto.getUnivCode(), historyDto.getDeptCode(), historyDto.getThingCode(), historyDto.getItemNum());
            target.setItemId(itemEntity.getId());
            target.setNum(historyDto.getNum());
        }
        
        if(historyDto.getUserStudentId() == null) {
            target.setUserId(0);
        } else {
            UserEntity userEntity = getUserEntityByUnivCodeAndStudentIdForSave(historyDto.getUnivCode(), historyDto.getUserStudentId());    
            target.setUserId(userEntity.getId());
        }
        
        if(historyDto.getApproveManagerStudentId() == null) {
            target.setApproveManagerId(0);
        } else {
            UserEntity approveManagerEntity = getUserEntityByUnivCodeAndStudentIdForSave(historyDto.getUnivCode(), historyDto.getApproveManagerStudentId());
            target.setApproveManagerId(approveManagerEntity.getId());
        }
        
        if(historyDto.getReturnManagerStudentId() == null) {
            target.setReturnManagerId(0);
        } else {
            UserEntity returnManagerEntity = getUserEntityByUnivCodeAndStudentIdForSave(historyDto.getUnivCode(), historyDto.getReturnManagerStudentId());    
            target.setReturnManagerId(returnManagerEntity.getId());
        }
        
        if(historyDto.getLostManagerStudentId() == null) {
            target.setLostManagerId(0);
        } else {
            UserEntity lostManagerEntity = getUserEntityByUnivCodeAndStudentIdForSave(historyDto.getUnivCode(), historyDto.getLostManagerStudentId());
            target.setLostManagerId(lostManagerEntity.getId());
        }
        
        target.setReserveTimeStamp(historyDto.getReserveTimeStamp());
        target.setApproveTimeStamp(historyDto.getApproveTimeStamp());
        target.setReturnTimeStamp(historyDto.getReturnTimeStamp());
        target.setCancelTimeStamp(historyDto.getCancelTimeStamp());
        target.setLostTimeStamp(historyDto.getLostTimeStamp());
        
        HistoryEntity savedHistoryEntity = repositoryManager.saveHistory(target);
        
        return toHistoryDto(savedHistoryEntity);
    }
    
    private UniversityDto toUnivDto(UniversityEntity univEntity) {
        UniversityDto output = new UniversityDto();
        output.setCode(univEntity.getCode());
        output.setName(univEntity.getName());
        output.setApiUrl(univEntity.getApiUrl());
        
        return output;
    }
    
    private List<UniversityDto> toUnivDtoList(List<UniversityEntity> univEntityList) {
        List<UniversityDto> output = new ArrayList<UniversityDto>();
        for(int i = 0; i < univEntityList.size(); i++) {
            output.add(toUnivDto(univEntityList.get(i)));
        }
        return output;
    }
    
    private UniversityEntity getUnivEntityByUnivCode(String univCode) throws NotFoundOnServerException, InternalDataBaseException {
        try {
           return repositoryManager.getUnivEntityByUnivCode(univCode);
        } catch(NotFoundOnDataBaseException e) {
            throw new NotFoundOnServerException("Database안에 고유 키에 해당하는 record가 없습니다.");
        } catch(UniqueKeyViolationException e) {
            throw new InternalDataBaseException("Database안에 고유 키를 공유하는 두 개 이상의 record가 있습니다.");
        }
    }
    
    private UniversityEntity getUnivEntityByUnivCodeForSave(String univCode) throws BreakDataBaseRulesException, InternalDataBaseException {
        try {
           return getUnivEntityByUnivCode(univCode);
        } catch(NotFoundOnServerException e) {
            throw new BreakDataBaseRulesException("저장하려는 record가 Database의 규칙을 무시합니다.");
        }
    }
        
    private void checkUnivDuplication(UniversityDto univDto) throws CodeDuplicationException {
        boolean doesUnivDuplicate;
        doesUnivDuplicate = repositoryManager.doesUnivDuplicate(univDto.getCode());
        if(doesUnivDuplicate) {
            throw new CodeDuplicationException("Database안에 이미 같은 키를 고유키로 가지는 record가 있습니다.");
        }
    }
        
    private DepartmentDto toDeptDto(DepartmentEntity deptEntity) throws InternalDataBaseException {
        DepartmentDto output = new DepartmentDto();
        
        output.setCode(deptEntity.getCode());
        output.setName(deptEntity.getName());
        output.setAvailable(deptEntity.isAvailble());
        
        try {
            UniversityEntity univ = repositoryManager.getUnivEntityById(deptEntity.getUnivId());    
            output.setUnivCode(univ.getCode());
        } catch(NotFoundOnDataBaseException e) {
            throw new InternalDataBaseException("해당 Department는 대응되는 University를 찾을 수 없는 잘못된 Department입니다.");
        }
        return output;
    }
    
    private List<DepartmentDto> toDeptDtoList(List<DepartmentEntity> deptEntityList) throws InternalDataBaseException {
        ArrayList<DepartmentDto> output = new ArrayList<>();
        for(int i = 0; i < deptEntityList.size(); i++) {
            output.add(toDeptDto(deptEntityList.get(i)));
        }
        return output;
    }
    
    private List<DepartmentEntity> getDeptEntityListByUnivCode(String univCode) throws InternalDataBaseException {
        try {
            return repositoryManager.getAllDeptEntitiesByUnivCode(univCode);
        } catch(UniqueKeyViolationException e) {
            throw new InternalDataBaseException("Database안에 고유 키를 공유하는 두 개 이상의 record가 있습니다.");
        }        
    }
    
    private DepartmentEntity getDeptEntityByUnivCodeAndDeptCode(String univCode, String deptCode) throws NotFoundOnServerException, InternalDataBaseException {
        try {
           return repositoryManager.getDeptEntityByUnivCodeAndDeptCode(univCode, deptCode);
        } catch(NotFoundOnDataBaseException e) {
            throw new NotFoundOnServerException("Database안에 고유 키에 해당하는 record가 없습니다.");
        } catch(UniqueKeyViolationException e) {
            throw new InternalDataBaseException("Database안에 고유 키를 공유하는 두 개 이상의 record가 있습니다.");
        }
    }
    
    private DepartmentEntity getDeptEntityByUnivCodeAndDeptCodeForSave(String univCode, String deptCode) throws BreakDataBaseRulesException, InternalDataBaseException {
        try {
           return getDeptEntityByUnivCodeAndDeptCode(univCode, deptCode);
        } catch(NotFoundOnServerException e) {
            throw new BreakDataBaseRulesException("저장하려는 record가 Database의 규칙을 무시합니다.");
        }
    }

    
    private void checkDeptDuplication(DepartmentDto deptDto) throws CodeDuplicationException, InternalDataBaseException {
        boolean doesDeptDuplicate;

        try {
            doesDeptDuplicate = repositoryManager.doesDeptDuplicate(deptDto.getUnivCode(), deptDto.getCode());
        } catch(UniqueKeyViolationException e) {
            throw new InternalDataBaseException("Database안에 고유 키를 공유하는 두 개 이상의 record가 있습니다.");
        }
        
        if(doesDeptDuplicate) {
            throw new CodeDuplicationException("Database안에 이미 같은 키를 고유키로 가지는 record가 있습니다.");
        }
    }
    
    private MajorDto toMajorDto(MajorEntity majorEntity) throws InternalDataBaseException {
        MajorDto output = new MajorDto();
        
        UniversityEntity univEntity;
        DepartmentEntity deptEntity;
        
        try {
            deptEntity = repositoryManager.getDeptEntityById(majorEntity.getDeptId());
            output.setDeptCode(deptEntity.getCode());
        } catch(NotFoundOnDataBaseException e) {
            throw new InternalDataBaseException("해당 Major는 대응되는 Department를 찾을 수 없는 잘못된 Major입니다.");
        }
        
        try {
            univEntity = repositoryManager.getUnivEntityById(deptEntity.getUnivId());    
            output.setUnivCode(univEntity.getCode());
        } catch(NotFoundOnDataBaseException e) {
            throw new InternalDataBaseException("해당 Major는 대응되는 University를 찾을 수 없는 잘못된 Major입니다.");
        }
    
        output.setCode(majorEntity.getCode());
        return output;
    }
    
    private List<MajorDto> toMajorDtoList(List<MajorEntity> majorEntityList) throws InternalDataBaseException {
        List<MajorDto> output = new ArrayList<>();
        for(int i = 0; i < majorEntityList.size(); i++) {
            output.add(toMajorDto(majorEntityList.get(i)));
        }
        return output;
    }
    
    private List<MajorEntity> getMajorEntityListByUnivCode(String univCode) throws InternalDataBaseException {
        try {
            return repositoryManager.getAllMajorEntitiesByUnivCode(univCode);
        } catch(UniqueKeyViolationException e) {
            throw new InternalDataBaseException("Database안에 고유 키를 공유하는 두 개 이상의 record가 있습니다."); //TODO 어떤 record의 어떤키인지 알려주는 것 까지는 안 되는 것인가... (1)
        }
    }

    private List<MajorEntity> getMajorEntityListByUnivCodeAndDeptCode(String univCode, String deptCode) throws InternalDataBaseException {
        try {
            return repositoryManager.getAllMajorEntitiesByUnivCodeAndDeptCode(univCode, deptCode);
        } catch(UniqueKeyViolationException e) {
            throw new InternalDataBaseException("Database안에 고유 키를 공유하는 두 개 이상의 record가 있습니다.");
        }
    }
    
    private MajorEntity getMajorEntityByUnivCodeAndMajorCode(String univCode, String majorCode) throws NotFoundOnServerException, InternalDataBaseException {
        try {
           return repositoryManager.getMajorEntityByUnivCodeAndMajorCode(univCode, majorCode);
        } catch(NotFoundOnDataBaseException e) {
            throw new NotFoundOnServerException("Database안에 고유 키에 해당하는 record가 없습니다.");
        } catch(UniqueKeyViolationException e) {
            throw new InternalDataBaseException("Database안에 고유 키를 공유하는 두 개 이상의 record가 있습니다.");
        }
    }
    
    private void checkMajorDuplication(MajorDto majorDto) throws CodeDuplicationException, InternalDataBaseException {
        boolean doesMajorDuplicate;
        try {
            doesMajorDuplicate = repositoryManager.doesMajorDuplicate(majorDto.getUnivCode(), majorDto.getCode());
        } catch(UniqueKeyViolationException e) {
            throw new InternalDataBaseException("Database안에 고유 키를 공유하는 두 개 이상의 record가 있습니다.");
        }
        
        if(doesMajorDuplicate) {
            throw new CodeDuplicationException("Database안에 이미 같은 키를 고유키로 가지는 record가 있습니다.");
        }
    }
    
    private PermissionDto toPermissionDto(PermissionEntity permissionEntity) throws InternalDataBaseException {
        PermissionDto output = new PermissionDto();
        
        UniversityEntity univEntity;
        DepartmentEntity deptEntity;
        UserEntity userEntity;
        
        try {
            userEntity = repositoryManager.getUserEntityById(permissionEntity.getUserId());
            output.setStudentId(userEntity.getStudentId());
        } catch(NotFoundOnDataBaseException e) {
            throw new InternalDataBaseException("해당 Permission는 대응되는 User를 찾을 수 없는 잘못된 Permission입니다.");
        }
        
        try {
            deptEntity = repositoryManager.getDeptEntityById(permissionEntity.getDeptId());
            output.setDeptCode(deptEntity.getCode());
        } catch(NotFoundOnDataBaseException e) {
            throw new InternalDataBaseException("해당 Permission는 대응되는 Department를 찾을 수 없는 잘못된 Permission입니다.");
        }
        
        try {
            univEntity = repositoryManager.getUnivEntityById(deptEntity.getUnivId());    
            output.setUnivCode(univEntity.getCode());
        } catch(NotFoundOnDataBaseException e) {
            throw new InternalDataBaseException("해당 Permission는 대응되는 University를 찾을 수 없는 잘못된 Permission입니다.");
        }
        output.setPermission(Permissions.valueOf(permissionEntity.getPermission()));
    
        return output;
    }
    
    private List<PermissionDto> toPermissionDtoList(List<PermissionEntity> permissionEntityList) throws InternalDataBaseException {
        ArrayList<PermissionDto> output = new ArrayList<>();
        for(int i = 0; i < permissionEntityList.size(); i++) {
            output.add(toPermissionDto(permissionEntityList.get(i)));
        }
        return output;
    }
    
    private List<PermissionEntity> getPermissionEntityListByUnivCodeAndStudentId(String univCode, String studentId) throws InternalDataBaseException {
        try {
            return repositoryManager.getAllPermissionEntitiesByUnivCodeAndStudentId(univCode, studentId);
        } catch(UniqueKeyViolationException e) {
            throw new InternalDataBaseException("Database안에 고유 키를 공유하는 두 개 이상의 record가 있습니다.");
        }        
    }
    
    private PermissionEntity getPermissionEntityByUnivCodeAndStudentIdAndDeptCode(String univCode, String studentId, String deptCode) throws NotFoundOnServerException, InternalDataBaseException {
        try {
           return repositoryManager.getPermissionEntityByUnivCodeAndStudentIdAndDeptCode(univCode, studentId, deptCode);
        } catch(NotFoundOnDataBaseException e) {
            throw new NotFoundOnServerException("Database안에 고유 키에 해당하는 record가 없습니다.");
        } catch(UniqueKeyViolationException e) {
            throw new InternalDataBaseException("Database안에 고유 키를 공유하는 두 개 이상의 record가 있습니다.");
        }
    }
    
    private PermissionEntity findOrMakeNewPermissionEntity(String univCode, String deptCode, String studentId) throws InternalDataBaseException, BreakDataBaseRulesException {            
        PermissionEntity output = new PermissionEntity();
        try {
            output = getPermissionEntityByUnivCodeAndStudentIdAndDeptCode(univCode, studentId, deptCode);
        } catch(NotFoundOnServerException e) {
            UserEntity userEntity = getUserEntityByUnivCodeAndStudentIdForSave(univCode, studentId);
            output.setUserId(userEntity.getId());
            
            DepartmentEntity deptEntity = getDeptEntityByUnivCodeAndDeptCodeForSave(univCode, deptCode);
            output.setDeptId(deptEntity.getId());
        }
        
        return output;
    }

    
    private void checkPermissionDuplication(PermissionDto permissionDto) throws CodeDuplicationException, InternalDataBaseException {
        boolean doesDeptDuplicate;

        try {
            doesDeptDuplicate = repositoryManager.doesPermissionDuplicate(permissionDto.getUnivCode(), permissionDto.getStudentId(), permissionDto.getDeptCode());
        } catch(UniqueKeyViolationException e) {
            throw new InternalDataBaseException("Database안에 고유 키를 공유하는 두 개 이상의 record가 있습니다.");
        }
        
        if(doesDeptDuplicate) {
            throw new CodeDuplicationException("Database안에 이미 같은 키를 고유키로 가지는 record가 있습니다.");
        }
    }
    
    private UserDto toUserDto(UserEntity userEntity) throws InternalDataBaseException {
        UserDto output = new UserDto();
        
        UniversityEntity univEntity;
        
        try {
            univEntity = repositoryManager.getUnivEntityById(userEntity.getUnivId());
            output.setUnivCode(univEntity.getCode());
        } catch(NotFoundOnDataBaseException e) {
            throw new InternalDataBaseException("해당 User는 대응되는 University를 찾을 수 없는 잘못된 User입니다.");
        }
        
        output.setStudentId(userEntity.getStudentId());
        output.setName(userEntity.getName());
        output.setEntranceYear(userEntity.getEntranceYear());
        output.setCreateTimeStamp(userEntity.getCreateTimeStamp());
        output.setApprovalTimeStamp(userEntity.getApprovalTimeStamp());
        output.setToken(userEntity.getToken());
        
        Map<String, Permissions> permissions = new HashMap<>();
        
        List<PermissionEntity> permissionEntityList;
        try {
             permissionEntityList = repositoryManager.getAllPermissionEntitiesByUnivCodeAndStudentId(univEntity.getCode(), userEntity.getStudentId());
        } catch(UniqueKeyViolationException e) {
             throw new InternalDataBaseException("Database안에 고유 키를 공유하는 두 개 이상의 record가 있습니다.");
        }
        
        for(int i = 0; i < permissionEntityList.size(); i++) {
            DepartmentEntity deptEntity;
            try {
                deptEntity = repositoryManager.getDeptEntityById(permissionEntityList.get(i).getDeptId());
            } catch(NotFoundOnDataBaseException e) {
                throw new InternalDataBaseException("해당 User의 Permission 중 하나가 대응되는 Department를 찾을 수 없는 잘못된 Permission입니다.");
            }
            
            permissions.put(deptEntity.getCode(), Permissions.valueOf(permissionEntityList.get(i).getPermission())); 
            // TODO 같은 dept code를 갖는 permissions가 있을 시 예외처리는 안함. db에 저장하는 것을 제대로 만들면 딱히 필요없을 듯
        }        
        output.setPermissions(permissions);
        
        return output;
    }
    
    private List<UserDto> toUserDtoList(List<UserEntity> userEntityList) throws InternalDataBaseException {
        ArrayList<UserDto> output = new ArrayList<>();
        for(int i = 0; i < userEntityList.size(); i++) {
            output.add(toUserDto(userEntityList.get(i)));
        }
        return output;
    }
    
    private List<UserEntity> getUserEntityListByUnivCodeAndDeptCode(String univCode, String deptCode) throws InternalDataBaseException {
        List<PermissionEntity> permissionEntityList;
        try {
            permissionEntityList = repositoryManager.getAllPermissionEntitiesByUnivCodeAndDeptCode(univCode, deptCode);
        } catch(UniqueKeyViolationException e) {
            throw new InternalDataBaseException("Database안에 고유 키를 공유하는 두 개 이상의 record가 있습니다.");
        }
        
        List<UserEntity> output = new ArrayList<>();
        for(int i = 0; i < permissionEntityList.size(); i++) {
            UserEntity userEntity;
            try {
                userEntity = repositoryManager.getUserEntityById(permissionEntityList.get(i).getUserId());
            } catch(NotFoundOnDataBaseException e) {
                throw new InternalDataBaseException("해당 Permission는 대응되는 User를 찾을 수 없는 잘못된 Permission입니다.");
            }
            output.add(userEntity);
        }

        return output;
         // TODO 같은 dept code를 갖는 permissions가 있을 시 예외처리는 안함. db에 저장하는 것을 제대로 만들면 딱히 필요없을 듯
    }

    private UserEntity getUserEntityByToken(String token) throws NotFoundOnServerException, InternalDataBaseException {
        try {
           return repositoryManager.getUserEntityByToken(token);
        } catch(NotFoundOnDataBaseException e) {
            throw new NotFoundOnServerException("Database안에 고유 키에 해당하는 record가 없습니다.");
        } catch(UniqueKeyViolationException e) {
            throw new InternalDataBaseException("Database안에 고유 키를 공유하는 두 개 이상의 record가 있습니다.");
        }
    }
    
    private UserEntity getUserEntityByUnivCodeAndStudentId(String univCode, String studentId) throws NotFoundOnServerException, InternalDataBaseException {
        try {
           return repositoryManager.getUserEntityByUnivCodeAndStudentId(univCode, studentId);
        } catch(NotFoundOnDataBaseException e) {
            throw new NotFoundOnServerException("Database안에 고유 키에 해당하는 record가 없습니다.");
        } catch(UniqueKeyViolationException e) {
            throw new InternalDataBaseException("Database안에 고유 키를 공유하는 두 개 이상의 record가 있습니다.");
        }
    }
    
    private UserEntity getUserEntityByUnivCodeAndStudentIdForSave(String univCode, String studentId) throws BreakDataBaseRulesException, InternalDataBaseException {
        try {
           return getUserEntityByUnivCodeAndStudentId(univCode, studentId);
        } catch(NotFoundOnServerException e) {
            throw new BreakDataBaseRulesException("저장하려는 record가 Database의 규칙을 무시합니다.");
        }
    }
    
    private void checkUserDuplicationOnToken(UserDto userDto) throws CodeDuplicationException, InternalDataBaseException {
        boolean doesUserDuplicate = repositoryManager.doesUserDuplicateOnToken(userDto.getToken());
        
        if(doesUserDuplicate) {
            throw new CodeDuplicationException("Database안에 이미 같은 키를 고유키로 가지는 record가 있습니다.");
        }
    }
    
    private void checkUserDuplicationOnUnivCodeAndStudentId(UserDto userDto) throws CodeDuplicationException, InternalDataBaseException {
        boolean doesUserDuplicate;        
        try {
            doesUserDuplicate = repositoryManager.doesUserDuplicateOnUnivCodeAndStudentId(userDto.getUnivCode(), userDto.getStudentId());    
        } catch(UniqueKeyViolationException e) {
            throw new InternalDataBaseException("Database안에 고유 키를 공유하는 두 개 이상의 record가 있습니다.");
        }

        if(doesUserDuplicate) {
            throw new CodeDuplicationException("Database안에 이미 같은 키를 고유키로 가지는 record가 있습니다.");
        }
    }
    
    private ThingDto toThingDto(ThingEntity thingEntity) throws InternalDataBaseException {
        ThingDto output = new ThingDto();
        
        UniversityEntity univEntity;
        DepartmentEntity deptEntity;
        
        try {
            deptEntity = repositoryManager.getDeptEntityById(thingEntity.getDeptId());
            output.setDeptCode(deptEntity.getCode());
        } catch(NotFoundOnDataBaseException e) {
            throw new InternalDataBaseException("해당 Thing은 대응되는 Department를 찾을 수 없는 잘못된 Thing입니다.");
        }
        
        try {
            univEntity = repositoryManager.getUnivEntityById(deptEntity.getUnivId());    
            output.setUnivCode(univEntity.getCode());
        } catch(NotFoundOnDataBaseException e) {
            throw new InternalDataBaseException("해당 Thing은 대응되는 University를 찾을 수 없는 잘못된 Thing입니다.");
        }
        
        output.setCode(thingEntity.getCode());
        output.setName(thingEntity.getName());
        output.setDescription(thingEntity.getDescription());
        output.setEmoji(thingEntity.getEmoji());
        
        return output;
    }
    
    private List<ThingDto> toThingDtoList(List<ThingEntity> thingEntityList) throws InternalDataBaseException {
        ArrayList<ThingDto> output = new ArrayList<>();
        for(int i = 0; i < thingEntityList.size(); i++) {
            output.add(toThingDto(thingEntityList.get(i)));
        }
        return output;
    }
    
    private List<ThingEntity> getThingEntityListByUnivCodeAndDeptCode(String univCode, String deptCode) throws InternalDataBaseException {
        try {
            return repositoryManager.getAllThingEntitiesByUnivCodeAndDeptCode(univCode, deptCode);    
        } catch(UniqueKeyViolationException e) {
            throw new InternalDataBaseException("Database안에 고유 키를 공유하는 두 개 이상의 record가 있습니다.");
        }   
    }
    
    private ThingEntity getThingEntityByUnivCodeAndDeptCodeAndThingCode(String univCode, String deptCode, String thingCode) throws NotFoundOnServerException, InternalDataBaseException {
        try {
           return repositoryManager.getThingEntityByUnivCodeAndDeptCodeAndThingCode(univCode, deptCode, thingCode);
        } catch(NotFoundOnDataBaseException e) {
            throw new NotFoundOnServerException("Database안에 고유 키에 해당하는 record가 없습니다.");
        } catch(UniqueKeyViolationException e) {
            throw new InternalDataBaseException("Database안에 고유 키를 공유하는 두 개 이상의 record가 있습니다.");
        }
    }
    
    private ThingEntity getThingEntityByUnivCodeAndDeptCodeAndThingCodeForSave(String univCode, String deptCode, String thingCode) throws BreakDataBaseRulesException, InternalDataBaseException {
        try {
           return getThingEntityByUnivCodeAndDeptCodeAndThingCode(univCode, deptCode, thingCode);
        } catch(NotFoundOnServerException e) {
            throw new BreakDataBaseRulesException("저장하려는 record가 Database의 규칙을 무시합니다.");
        }
    }
    
    private void checkThingDuplication(ThingDto thingDto) throws CodeDuplicationException, InternalDataBaseException {
        boolean doesThingDuplicate;        
        try {
            doesThingDuplicate = repositoryManager.doesThingDuplicate(thingDto.getUnivCode(), thingDto.getDeptCode(), thingDto.getCode());    
        } catch(UniqueKeyViolationException e) {
            throw new InternalDataBaseException("Database안에 고유 키를 공유하는 두 개 이상의 record가 있습니다.");
        }

        if(doesThingDuplicate) {
            throw new CodeDuplicationException("Database안에 이미 같은 키를 고유키로 가지는 record가 있습니다.");
        }
    }
    
    public ItemDto toItemDto(ItemEntity itemEntity) throws InternalDataBaseException {
        ItemDto output = new ItemDto();
        
        UniversityEntity univEntity;
        DepartmentEntity deptEntity;
        ThingEntity thingEntity;
        HistoryEntity historyEntity;
        
        try {
            thingEntity = repositoryManager.getThingEntityById(itemEntity.getThingId());
            output.setThingCode(thingEntity.getCode());
        } catch(NotFoundOnDataBaseException e) {
            throw new InternalDataBaseException("해당 Item은 대응되는 Thing을 찾을 수 없는 잘못된 Item입니다.");
        }
        
        try {
            deptEntity = repositoryManager.getDeptEntityById(thingEntity.getDeptId());
            output.setDeptCode(deptEntity.getCode());
        } catch(NotFoundOnDataBaseException e) {
            throw new InternalDataBaseException("해당 Item은 대응되는 Department를 찾을 수 없는 잘못된 Item입니다.");
        }
        
        try {
            univEntity = repositoryManager.getUnivEntityById(deptEntity.getUnivId());    
            output.setUnivCode(univEntity.getCode());
        } catch(NotFoundOnDataBaseException e) {
            throw new InternalDataBaseException("해당 Item은 대응되는 University를 찾을 수 없는 잘못된 Item입니다.");
        }
        
        if(itemEntity.getLastHistoryId() == 0) {
            output.setLastHistoryNum(0);
        } else {
            try {
                historyEntity = repositoryManager.getHistoryEntityById(itemEntity.getLastHistoryId());
                output.setLastHistoryNum(historyEntity.getNum());
            } catch(NotFoundOnDataBaseException e) {
                throw new InternalDataBaseException("해당 Item은 대응되는 LastHistory를 찾을 수 없는 잘못된 Item입니다.");
            }    
        }
           
        output.setNum(itemEntity.getNum());
        
        return output;
    }
    
    private List<ItemDto> toItemDtoList(List<ItemEntity> itemEntityList) throws InternalDataBaseException {
        ArrayList<ItemDto> output = new ArrayList<>();
        for(int i = 0; i < itemEntityList.size(); i++) {
            output.add(toItemDto(itemEntityList.get(i)));
        }
        return output;
    }
    
    private List<ItemEntity> getItemEntityListByUnivCodeAndDeptCodeAndThingCode(String univCode, String deptCode, String thingCode) throws InternalDataBaseException {
        try {
            return repositoryManager.getAllItemEntitiesByUnivCodeAndDeptCodeAndThingCode(univCode, deptCode, thingCode);
        } catch(UniqueKeyViolationException e) {
            throw new InternalDataBaseException("Database안에 고유 키를 공유하는 두 개 이상의 record가 있습니다.");
        }   
    }
    
    private ItemEntity getItemEntityByUnivCodeAndDeptCodeAndThingCodeAndItemNum(String univCode, String deptCode, String thingCode, int itemNum) throws NotFoundOnServerException, InternalDataBaseException {
        try {
           return repositoryManager.getItemEntityByUnivCodeAndDeptCodeAndThingCodeAndItemNum(univCode, deptCode, thingCode, itemNum);
        } catch(NotFoundOnDataBaseException e) {
            throw new NotFoundOnServerException("Database안에 고유 키에 해당하는 record가 없습니다.");
        } catch(UniqueKeyViolationException e) {
            throw new InternalDataBaseException("Database안에 고유 키를 공유하는 두 개 이상의 record가 있습니다.");
        }
    }
    
    private ItemEntity getItemEntityByUnivCodeAndDeptCodeAndThingCodeAndItemNumForSave(String univCode, String deptCode, String thingCode, int itemNum) throws BreakDataBaseRulesException, InternalDataBaseException {
        try {
           return getItemEntityByUnivCodeAndDeptCodeAndThingCodeAndItemNum(univCode, deptCode, thingCode, itemNum);
        } catch(NotFoundOnServerException e) {
            throw new BreakDataBaseRulesException("저장하려는 record가 Database의 규칙을 무시합니다.");
        }
    }
    
    private void checkItemDuplication(ItemDto itemDto) throws CodeDuplicationException, InternalDataBaseException {
        boolean doesItemDuplicate;        
        try {
            doesItemDuplicate = repositoryManager.doesItemDuplicate(itemDto.getUnivCode(), itemDto.getDeptCode(), itemDto.getThingCode(), itemDto.getNum());    
        } catch(UniqueKeyViolationException e) {
            throw new InternalDataBaseException("Database안에 고유 키를 공유하는 두 개 이상의 record가 있습니다.");
        }

        if(doesItemDuplicate) {
            throw new CodeDuplicationException("Database안에 이미 같은 키를 고유키로 가지는 record가 있습니다.");
        }
    }
    
    private HistoryDto toHistoryDto(HistoryEntity historyEntity) throws InternalDataBaseException {
        HistoryDto output = new HistoryDto();
        
        ItemEntity itemEntity;
        ThingEntity thingEntity;
        DepartmentEntity deptEntity;
        UniversityEntity univEntity;
        
        try {
            itemEntity = repositoryManager.getItemEntityById(historyEntity.getItemId());
            output.setItemNum(itemEntity.getNum());
        } catch(NotFoundOnDataBaseException e) {
            throw new InternalDataBaseException("해당 History는 대응되는 Item을 찾을 수 없는 잘못된 History입니다.");
        }
        
        try {
            thingEntity = repositoryManager.getThingEntityById(itemEntity.getThingId());
            output.setThingCode(thingEntity.getCode());
        } catch(NotFoundOnDataBaseException e) {
            throw new InternalDataBaseException("해당 History는 대응되는 Thing을 찾을 수 없는 잘못된 History입니다.");
        }
        
        try {
            deptEntity = repositoryManager.getDeptEntityById(thingEntity.getDeptId());
            output.setDeptCode(deptEntity.getCode());
        } catch(NotFoundOnDataBaseException e) {
            throw new InternalDataBaseException("해당 History는 대응되는 Department를 찾을 수 없는 잘못된 History입니다.");
        }
        
        try {
            univEntity = repositoryManager.getUnivEntityById(deptEntity.getUnivId());    
            output.setUnivCode(univEntity.getCode());
        } catch(NotFoundOnDataBaseException e) {
            throw new InternalDataBaseException("해당 History는 대응되는 University를 찾을 수 없는 잘못된 History입니다.");
        }
      
        output.setNum(historyEntity.getNum());
        
        output.setUserStudentId(getUserStudentId(historyEntity));
        output.setApproveManagerStudentId(getApproveManagerStudentId(historyEntity));
        output.setReturnManagerStudentId(getReturnManagerStudentId(historyEntity));
        output.setLostManagerStudentId(getLostManagerStudentId(historyEntity));
        
        output.setReserveTimeStamp(historyEntity.getReserveTimeStamp());
        output.setApproveTimeStamp(historyEntity.getApproveTimeStamp());
        output.setReturnTimeStamp(historyEntity.getReturnTimeStamp());
        output.setCancelTimeStamp(historyEntity.getCancelTimeStamp());
        output.setLostTimeStamp(historyEntity.getLostTimeStamp());
        
        return output;
    }
    
    private String getUserStudentId(HistoryEntity historyEntity) throws InternalDataBaseException {
        if(historyEntity.getUserId() == 0) {
            return null;
        }
        try {
            UserEntity userEntity = repositoryManager.getUserEntityById(historyEntity.getUserId());
            return userEntity.getStudentId();
        } catch(NotFoundOnDataBaseException e) {
            throw new InternalDataBaseException("해당 History는 대응되는 User를 찾을 수 없는 잘못된 History입니다.");
        }
    }
    
    private String getApproveManagerStudentId(HistoryEntity historyEntity) throws InternalDataBaseException {
        if(historyEntity.getApproveManagerId() == 0) {
            return null;
        }
        try {
            UserEntity approveManagerEntity = repositoryManager.getUserEntityById(historyEntity.getApproveManagerId());
            return approveManagerEntity.getStudentId();
        } catch(NotFoundOnDataBaseException e) {
            throw new InternalDataBaseException("해당 History는 대응되는 ApproveManager를 찾을 수 없는 잘못된 History입니다.");
        }
    }
    
    private String getReturnManagerStudentId(HistoryEntity historyEntity) throws InternalDataBaseException {
        if(historyEntity.getReturnManagerId() == 0) {
            return null;
        }
        try {
            UserEntity returnManagerEntity = repositoryManager.getUserEntityById(historyEntity.getReturnManagerId());
            return returnManagerEntity.getStudentId();
        } catch(NotFoundOnDataBaseException e) {
            throw new InternalDataBaseException("해당 History는 대응되는 ReturnManager를 찾을 수 없는 잘못된 History입니다.");
        }
    }
    
    private String getLostManagerStudentId(HistoryEntity historyEntity) throws InternalDataBaseException {
        if(historyEntity.getLostManagerId() == 0) {
            return null;
        }
        try {
            UserEntity lostManagerEntity = repositoryManager.getUserEntityById(historyEntity.getLostManagerId());
            return lostManagerEntity.getStudentId();
        } catch(NotFoundOnDataBaseException e) {
            throw new InternalDataBaseException("해당 History는 대응되는 LostManager를 찾을 수 없는 잘못된 History입니다.");
        }
    }
    
    private List<HistoryDto> toHistoryDtoList(List<HistoryEntity> historyEntityList) throws InternalDataBaseException {
        ArrayList<HistoryDto> output = new ArrayList<>();
        for(int i = 0; i < historyEntityList.size(); i++) {
            output.add(toHistoryDto(historyEntityList.get(i)));
        }
        return output;
    }
    
    private List<HistoryEntity> getHistoryEntityListByUnivCodeAndDeptCode(String univCode, String deptCode) throws InternalDataBaseException {
        try {
            return repositoryManager.getAllHistoryEntitiesByUnivCodeAndDeptCode(univCode, deptCode);
        } catch(UniqueKeyViolationException e) {
            throw new InternalDataBaseException("Database안에 고유 키를 공유하는 두 개 이상의 record가 있습니다.");
        }   
    }
    
    private List<HistoryEntity> getHistoryEntityListByUnivCodeAndDeptCodeAndStudentId(String univCode, String deptCode, String studentId) throws InternalDataBaseException {
        try {
            return repositoryManager.getAllHistoryEntitiesByUnivCodeAndDeptCodeAndUserStudentId(univCode, deptCode, studentId);
        } catch(UniqueKeyViolationException e) {
            throw new InternalDataBaseException("Database안에 고유 키를 공유하는 두 개 이상의 record가 있습니다.");
        }   
    }
    
    private List<HistoryEntity> getHistoryEntityListByUnivCodeAndDeptCodeAndThingCodeAndItemNum(String univCode, String deptCode, String thingCode, int itemNum) throws InternalDataBaseException {
        try {
            return repositoryManager.getAllHistoryEntitiesByUnivCodeAndDeptCodeAndThingCodeAndItemNum(univCode, deptCode, thingCode, itemNum);
        } catch(UniqueKeyViolationException e) {
            throw new InternalDataBaseException("Database안에 고유 키를 공유하는 두 개 이상의 record가 있습니다.");
        }   
    }
    
    private HistoryEntity getHistoryEntityByUnivCodeAndDeptCodeAndThingCodeAndItemNumAndHistoryNum(String univCode, String deptCode, String thingCode, int itemNum, int historyNum) throws NotFoundOnServerException, InternalDataBaseException {
        try {
           return repositoryManager.getHistoryEntityByUnivCodeAndDeptCodeAndThingCodeAndItemNumAndHistoryNum(univCode, deptCode, thingCode, itemNum, historyNum);
        } catch(NotFoundOnDataBaseException e) {
            throw new NotFoundOnServerException("Database안에 고유 키에 해당하는 record가 없습니다.");
        } catch(UniqueKeyViolationException e) {
            throw new InternalDataBaseException("Database안에 고유 키를 공유하는 두 개 이상의 record가 있습니다.");
        }
    }
    
    private HistoryEntity getHistoryEntityByUnivCodeAndDeptCodeAndThingCodeAndItemNumAndHistoryNumForSave(String univCode, String deptCode, String thingCode, int itemNum, int historyNum) throws BreakDataBaseRulesException, InternalDataBaseException {
        try {
           return getHistoryEntityByUnivCodeAndDeptCodeAndThingCodeAndItemNumAndHistoryNum(univCode, deptCode, thingCode, itemNum, historyNum);
        } catch(NotFoundOnServerException e) {
            throw new BreakDataBaseRulesException("저장하려는 record가 Database의 규칙을 무시합니다.");
        }
    }
    
    private void checkHistoryDuplication(HistoryDto historyDto) throws CodeDuplicationException, InternalDataBaseException {
        boolean doesHistoryDuplicate;        
        try {
            doesHistoryDuplicate = repositoryManager.doesHistoryDuplicate(historyDto.getUnivCode(), historyDto.getDeptCode(), historyDto.getThingCode(), historyDto.getItemNum(), historyDto .getNum());    
        } catch(UniqueKeyViolationException e) {
            throw new InternalDataBaseException("Database안에 고유 키를 공유하는 두 개 이상의 record가 있습니다.");
        }

        if(doesHistoryDuplicate) {
            throw new CodeDuplicationException("Database안에 이미 같은 키를 고유키로 가지는 record가 있습니다.");
        }
    }
    
}