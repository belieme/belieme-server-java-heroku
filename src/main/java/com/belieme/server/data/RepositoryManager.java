package com.belieme.server.data;

import java.util.*;

import com.belieme.server.data.university.*;
import com.belieme.server.data.department.*;
import com.belieme.server.data.major.*;
import com.belieme.server.data.user.*;
import com.belieme.server.data.permission.*;
import com.belieme.server.data.thing.*;
import com.belieme.server.data.item.*;
import com.belieme.server.data.event.*;

import com.belieme.server.domain.exception.*;

public class RepositoryManager { //TODO 함수 이름 일관화 하고 daoImpl에 호환 맞추기
    private UniversityRepository univRepo;
    private DepartmentRepository deptRepo;
    private MajorRepository majorRepo;
    private UserRepository userRepo;
    private PermissionRepository permissionRepo;
    private ThingRepository thingRepo;
    private ItemRepository itemRepo;
    private EventRepository eventRepo;
    
    public RepositoryManager(UniversityRepository univRepo, DepartmentRepository deptRepo, MajorRepository majorRepo, UserRepository userRepo, PermissionRepository permissionRepo, ThingRepository thingRepo, ItemRepository itemRepo, EventRepository eventRepo) {
        this.univRepo = univRepo;
        this.deptRepo = deptRepo;
        this.majorRepo = majorRepo;
        this.userRepo = userRepo;
        this.permissionRepo = permissionRepo;
        this.thingRepo = thingRepo;
        this.itemRepo = itemRepo;
        this.eventRepo = eventRepo;
    }
    
    public List<UniversityEntity> getAllUnivEntities() {
        List<UniversityEntity> output = new ArrayList<>();
        Iterator<UniversityEntity> iter = univRepo.findAll().iterator();
        while(iter.hasNext()) {
            UniversityEntity tmpVarForLoop = iter.next();
            output.add(tmpVarForLoop);
        }
        return output;
    }
    
    public UniversityEntity getUnivEntityByUnivCode(String univCode) throws NotFoundOnDataBaseException, InternalDataBaseException { // done
        List<UniversityEntity> univListFromDb = univRepo.findByCode(univCode);
        if(univListFromDb.size() == 0) {
            throw new NotFoundOnDataBaseException("RepositoryManager.getUnivEntityByUnivCode()");
        } else if(univListFromDb.size() == 1) {
            return univListFromDb.get(0);
        } else {
            throw new InternalDataBaseException("RepositoryManager.getUnivEntityByUnivCode()");
        }
    }
    
    public UniversityEntity getUnivEntityById(int id) throws NotFoundOnDataBaseException {  // done
        UniversityEntity output = univRepo.findById(id).get();
        if(output == null) {
            throw new NotFoundOnDataBaseException("RepositoryManager.getUnivEntityById()");
        } else {
            return output;
        }
    }
    
    public void checkUnivDuplicate(String univCode) throws CodeDuplicationException { // done
        List<UniversityEntity> univListFromDb = univRepo.findByCode(univCode);
        if(univListFromDb.size() != 0) {
            throw new CodeDuplicationException("RepositoryManager.checkUnivDuplicate()");
        }
    }
    
    public UniversityEntity saveUniv(UniversityEntity univ) {
        return univRepo.save(univ);
    }
    
    public List<DepartmentEntity> getAllDeptEntitiesByUnivCode(String univCode) throws InternalDataBaseException { // 일단 이걸로
        int univId;
        try {
            univId = getUnivEntityByUnivCode(univCode).getId(); 
        } catch(NotFoundOnDataBaseException e) {
            return new ArrayList<>();
        }
        return deptRepo.findByUnivId(univId);
    }
    
    public DepartmentEntity getDeptEntityById(int id) throws NotFoundOnDataBaseException {
        DepartmentEntity output = deptRepo.findById(id).get();
        if(output == null) {
            throw new NotFoundOnDataBaseException("RepositoryManager.getDeptEntityById()");
        } else {
            return output;
        }
    }
    
    public DepartmentEntity getDeptEntityByUnivCodeAndDeptCode(String univCode, String deptCode) throws InternalDataBaseException, NotFoundOnDataBaseException { // done
        List<DepartmentEntity> deptListFromDb = deptRepo.findByUnivIdAndCode(getUnivEntityByUnivCode(univCode).getId(), deptCode);
        if(deptListFromDb.size() == 0) {
            throw new NotFoundOnDataBaseException("RepositoryManager.getDeptEntityByUnivCodeAndDeptCode()");
        } else if(deptListFromDb.size() == 1) {
            return deptListFromDb.get(0);
        } else {
            throw new InternalDataBaseException("RepositoryManager.getDeptEntityByUnivCodeAndDeptCode()");
        }
    }
    
    public void checkDeptDuplication(String univCode, String deptCode) throws CodeDuplicationException, InternalDataBaseException { // done
        int univId;
        try {
            univId = getUnivEntityByUnivCode(univCode).getId();    
        } catch(NotFoundOnDataBaseException e) {
            return;
        }
        
        List<DepartmentEntity> deptListFromDb = deptRepo.findByUnivIdAndCode(univId, deptCode);
        if(deptListFromDb.size() != 0) {
            throw new CodeDuplicationException("RepositoryManager.checkDeptDuplication()");
        }
    }
    
    public DepartmentEntity saveDept(DepartmentEntity dept) {
        return deptRepo.save(dept);
    }
    
    public List<MajorEntity> getAllMajorEntitiesByUnivCode(String univCode) throws InternalDataBaseException { // 일단 이걸로
        List<Integer> deptIdListByUnivCode = new ArrayList<>();
        List<DepartmentEntity> deptEntityListByUnivCode = getAllDeptEntitiesByUnivCode(univCode);
        
        for(int i = 0; i < deptEntityListByUnivCode.size(); i++) {
            deptIdListByUnivCode.add(deptEntityListByUnivCode.get(i).getId());
        }
        
        return majorRepo.findAllByDeptId(deptIdListByUnivCode);
    }
    
    public List<MajorEntity> getAllMajorEntitiesByUnivCodeAndDeptCode(String univCode, String deptCode) throws InternalDataBaseException { // 일단 이걸로
        List<MajorEntity> majorEntitiesByUnivCodeAndDeptCode = new ArrayList<>();
        try {
            DepartmentEntity dept = getDeptEntityByUnivCodeAndDeptCode(univCode, deptCode);
            majorEntitiesByUnivCodeAndDeptCode = majorRepo.findByDeptId(dept.getId());    
        } catch(NotFoundOnDataBaseException e) {   
        }
        return majorEntitiesByUnivCodeAndDeptCode;
    }
    
    public MajorEntity getMajorEntityByUnivCodeAndMajorCode(String univCode, String majorCode) throws NotFoundOnDataBaseException, InternalDataBaseException { // done
        List<MajorEntity> majorEntitiesByUnivCode = getAllMajorEntitiesByUnivCode(univCode);
        MajorEntity output = null;
        for(int i = 0; i < majorEntitiesByUnivCode.size(); i++) {
            if(majorCode.equals(majorEntitiesByUnivCode.get(i).getCode())) {
                if(output == null) {
                    output = majorEntitiesByUnivCode.get(i);    
                } else {
                    throw new InternalDataBaseException("RepositoryManager.getMajorEntityByUnivCodeAndMajorCode()");
                }
            }
        }
        if(output == null) {
            throw new NotFoundOnDataBaseException("RepositoryManager.getMajorEntityByUnivCodeAndMajorCode()");
        }
        return output;
    }
    
    public void checkMajorDuplication(String univCode, String majorCode) throws CodeDuplicationException, InternalDataBaseException { // done
        List<MajorEntity> majorEntitiesByUnivCode = getAllMajorEntitiesByUnivCode(univCode);
        for(int i = 0; i < majorEntitiesByUnivCode.size(); i++) {
            if(majorCode.equals(majorEntitiesByUnivCode.get(i).getCode())) {
                throw new CodeDuplicationException("RepositoryManager.checkMajorDuplication()");
            }
        }
    }
    
    public MajorEntity saveMajor(MajorEntity major) {
        return majorRepo.save(major);
    }
    
    public UserEntity getUserEntityById(int id) throws NotFoundOnDataBaseException {
        UserEntity output = userRepo.findById(id).get();
        if(output == null) {
            throw new NotFoundOnDataBaseException("RepositoryManager.getUserEntityById()");
        } else {
            return output;
        }
    }
    
    public UserEntity getUserEntityByUnivCodeAndStudentId(String univCode, String studentId) throws NotFoundOnDataBaseException, InternalDataBaseException { // done
        int univId = getUnivEntityByUnivCode(univCode).getId();
        List<UserEntity> userList = userRepo.findByUnivIdAndStudentId(univId, studentId);
        
        if(userList.size() == 1) {
            return userList.get(0);
        } else if(userList.size() == 0) {
            throw new NotFoundOnDataBaseException("RepositoryManager.getUserEntityByUnivCodeAndStudentId()");
        } else {
            throw new InternalDataBaseException("RepositoryManager.getUserEntityByUnivCodeAndStudentId()");
        }
    }
    
    public UserEntity getUserEntityByToken(String token) throws NotFoundOnDataBaseException, InternalDataBaseException { // done
        List<UserEntity> userList = userRepo.findByToken(token);
        
        if(userList.size() == 1) {
            return userList.get(0);
        } else if(userList.size() == 0) {
            throw new NotFoundOnDataBaseException("RepositoryManager.getUserEntityByToken()");
        } else {
            throw new InternalDataBaseException("RepositoryManager.getUserEntityByToken()");
        }
    }
    
    public UserEntity saveUser(UserEntity user) {
        return userRepo.save(user);
    }
    
    public void checkUserDuplicationByUnivCodeAndStudentId(String univCode, String studentId) throws CodeDuplicationException, InternalDataBaseException { // 일단 이걸로
        int univId;
        try { 
            univId = getUnivEntityByUnivCode(univCode).getId();
        } catch(NotFoundOnDataBaseException e) {
            return;
        }
        
        List<UserEntity> userListFromDb = userRepo.findByUnivIdAndStudentId(univId, studentId);
        if(userListFromDb.size() != 0) {
            throw new CodeDuplicationException("RepositoryManager.checkUserDuplicationByUnivCodeAndStudentId()");
        }
    }
    
    public void checkUserDuplicationByToken(String token) throws CodeDuplicationException { // done
        List<UserEntity> userListFromDb = userRepo.findByToken(token);
        if(userListFromDb.size() != 0) {
            throw new CodeDuplicationException("RepositoryManager.checkUserDuplicationByToken()");
        }
    }
    
    public List<PermissionEntity> getAllPermissionEntitiesByUnivCodeAndDeptCode(String univCode, String deptCode) throws InternalDataBaseException { // 일단 이걸로
        try {
            int deptId = getDeptEntityByUnivCodeAndDeptCode(univCode, deptCode).getId();
            return permissionRepo.findByDeptId(deptId);
        } catch(NotFoundOnDataBaseException e) {
            return new ArrayList<>();
        }
    }
    
    public List<PermissionEntity> getAllPermissionEntitiesByUnivCodeAndStudentId(String univCode, String studentId) throws InternalDataBaseException { // 일단 이걸로
        try {
            int userId = getUserEntityByUnivCodeAndStudentId(univCode, studentId).getId();
            return permissionRepo.findByUserId(userId);
        } catch(NotFoundOnDataBaseException e) {
            return new ArrayList<>();
        }
    }
    
    public PermissionEntity getPermissionEntityByUnivCodeAndStudentIdAndDeptCode(String univCode, String studentId, String deptCode) throws NotFoundOnDataBaseException, InternalDataBaseException { // done
        int deptId;
        int userId;
        
        deptId = getDeptEntityByUnivCodeAndDeptCode(univCode, deptCode).getId();
        userId = getUserEntityByUnivCodeAndStudentId(univCode, studentId).getId();
        
        List<PermissionEntity> permissionListFromDb = permissionRepo.findByUserIdAndDeptId(userId, deptId);
        if(permissionListFromDb.size() == 0) {
            throw new NotFoundOnDataBaseException("RepositoryManager.getPermissionEntityByUnivCodeAndStudentIdAndDeptCode()");
        } else if (permissionListFromDb.size() == 1) {
            return permissionListFromDb.get(0);
        } else {
            throw new InternalDataBaseException("RepositoryManager,getPermissionEntityByUnivCodeAndStudentIdAndDeptCode()");
        }
    }
    
    public void checkPermissionDuplication(String univCode, String studentId, String deptCode) throws CodeDuplicationException, InternalDataBaseException { // 일단 이걸로
        int userId;
        int deptId;
        try {
            userId = getUserEntityByUnivCodeAndStudentId(univCode, studentId).getId();
            deptId = getDeptEntityByUnivCodeAndDeptCode(univCode, deptCode).getId();
        } catch(NotFoundOnDataBaseException e) {
            return;
        }
        
        List<PermissionEntity> permissionList = permissionRepo.findByUserIdAndDeptId(userId, deptId);
        if(permissionList.size() != 0) {
            throw new CodeDuplicationException("RepositoryManager.checkPermissionDuplication()");
        }
    }
    
    public PermissionEntity savePermission(PermissionEntity permission) {
        return permissionRepo.save(permission);
    }
    
    public ThingEntity getThingEntity(int id) throws NotFoundOnDataBaseException {
        ThingEntity output = thingRepo.findById(id).get();
        if(output == null) {
            throw new NotFoundOnDataBaseException("RepositoryManager.getThingEntity()");
        } else {
            return output;
        }
    }
    
    public List<ThingEntity> getAllThingEntitiesByUnivCodeAndDeptCode(String univCode, String deptCode) throws InternalDataBaseException { // 일단 이걸로
        try { 
            int deptId = getDeptEntityByUnivCodeAndDeptCode(univCode, deptCode).getId();
            return thingRepo.findByDeptId(deptId);
        } catch(NotFoundOnDataBaseException e) {
            return new ArrayList<>();
        }
    }
    
    public ThingEntity getThingEntityByUnivCodeAndDeptCodeAndThingCode(String univCode, String deptCode, String thingCode) throws NotFoundOnDataBaseException, InternalDataBaseException { // done
        int deptId = getDeptEntityByUnivCodeAndDeptCode(univCode, deptCode).getId();
        List<ThingEntity> thingList = thingRepo.findByDeptIdAndCode(deptId, thingCode);
        if(thingList.size() == 0) {
            throw new NotFoundOnDataBaseException("RepositoryManager.getThingEntityByUnivCodeAndDeptCodeAndThingCode()");
        } else if(thingList.size() == 1) {
            return thingList.get(0);
        } else {
            throw new InternalDataBaseException("RepositoryManager.getThingEntityByUnivCodeAndDeptCodeAndThingCode()");
        }
    }
    
    public ThingEntity saveThing(ThingEntity thing) {
        return thingRepo.save(thing);
    }
    
    public void checkThingDuplication(String univCode, String deptCode, String thingCode) throws CodeDuplicationException, InternalDataBaseException { // 일단 이걸로
        int deptId;
        try {
            deptId = getDeptEntityByUnivCodeAndDeptCode(univCode, deptCode).getId();
        } catch(NotFoundOnDataBaseException e) {
            return ;
        }
        
        List<ThingEntity> thingList = thingRepo.findByDeptIdAndCode(deptId, thingCode);
        if(thingList.size() != 0) {
            throw new CodeDuplicationException("RepositoryManager.checkThingDuplication()");
        }
    }
    
    public ItemEntity getItemEntityById(int id) throws NotFoundOnDataBaseException { 
        ItemEntity output = itemRepo.findById(id).get();
        if(output == null) {
            throw new NotFoundOnDataBaseException("RepositoryManager.getItemEntityById()");
        } else {
            return output;
        }
    }
    
    public List<ItemEntity> getAllItemEntitiesByUnivCodeAndDeptCodeAndThingCode(String univCode, String deptCode, String thingCode) throws InternalDataBaseException { // 일단 이걸로
        try {
            List<ItemEntity> output = new ArrayList<>();
            ThingEntity thing = getThingEntityByUnivCodeAndDeptCodeAndThingCode(univCode, deptCode, thingCode);
            output.addAll(itemRepo.findByThingId(thing.getId()));
            return output;
        } catch(NotFoundOnDataBaseException e) {
            return new ArrayList<>();
        }
    }
    
    public ItemEntity getItemEntityByUnivCodeAndDeptCodeAndThingCodeAndItemNum(String univCode, String deptCode, String thingCode, int itemNum) throws NotFoundOnDataBaseException, InternalDataBaseException { // done
        int thingId = getThingEntityByUnivCodeAndDeptCodeAndThingCode(univCode, deptCode, thingCode).getId();
        List<ItemEntity> itemList = itemRepo.findByThingIdAndNum(thingId, itemNum);
        if(itemList.size() == 0) {
            throw new NotFoundOnDataBaseException("RepositoryManager.getItemEntityByUnivCodeAndDeptCodeAndThingCodeAndItemNum()");
        } else if(itemList.size() == 1) {
            return itemList.get(0);
        } else {
            throw new InternalDataBaseException("RepositoryManager.getItemEntityByUnivCodeAndDeptCodeAndThingCodeAndItemNum()");
        }
    }
    
    public void checkItemDuplication(String univCode, String deptCode, String thingCode, int itemNum) throws CodeDuplicationException, InternalDataBaseException { // 일단 이걸로
        int thingId;
        try {
            thingId= getThingEntityByUnivCodeAndDeptCodeAndThingCode(univCode, deptCode, thingCode).getId();
        } catch(NotFoundOnDataBaseException e) {
            return;
        }
        
        List<ItemEntity> itemList = itemRepo.findByThingIdAndNum(thingId, itemNum);
        if(itemList.size() != 0) {
            throw new CodeDuplicationException("RepositoryManager.checkItemDuplication()");
        }
    }
    
    public ItemEntity saveItem(ItemEntity item) {
        return itemRepo.save(item);
    }
    
    public EventEntity getEventEntityById(int id) throws NotFoundOnDataBaseException {
        EventEntity output = eventRepo.findById(id).get();
        if(output == null) {
            throw new NotFoundOnDataBaseException("RepositoryManager.getEventEntityById()");
        } else {
            return output;
        }
    }
    
    public List<EventEntity> getAllEventEntitiesByUnivCodeAndDeptCode(String univCode, String deptCode) throws InternalDataBaseException { // 일단 이걸로
        List<ThingEntity> thingList = getAllThingEntitiesByUnivCodeAndDeptCode(univCode, deptCode);
        List<ItemEntity> itemList = new ArrayList<>();
        for(int i = 0; i < thingList.size(); i++) {
            int thingId = thingList.get(i).getId();
            itemList.addAll(itemRepo.findByThingId(thingId));
        }
        
        List<EventEntity> output = new ArrayList<>();
        for(int i = 0; i < itemList.size(); i++) {
            int itemId = itemList.get(i).getId();
            output.addAll(eventRepo.findByItemId(itemId));
        }
        return output;
    }
    
    public List<EventEntity> getAllEventEntitiesByUnivCodeAndDeptCodeAndUserStudnetId(String univCode, String deptCode, String userStudentId) throws InternalDataBaseException { // 일단 이걸로
        List<ThingEntity> thingList = getAllThingEntitiesByUnivCodeAndDeptCode(univCode, deptCode);
        List<ItemEntity> itemList = new ArrayList<>();
        for(int i = 0; i < thingList.size(); i++) {
            int thingId = thingList.get(i).getId();
            itemList.addAll(itemRepo.findByThingId(thingId));
        }
        
        int userId;
        try {
            userId = getUserEntityByUnivCodeAndStudentId(univCode, userStudentId).getId();
        } catch(NotFoundOnDataBaseException e) {
            return new ArrayList<>();
        }

        List<EventEntity> output = new ArrayList<>();
        for(int i = 0; i < itemList.size(); i++) {
            int itemId = itemList.get(i).getId();
            List<EventEntity> eventList = eventRepo.findByItemId(itemId);
            for(int j = 0; j < eventList.size(); j++) {
                if(eventList.get(i).getUserId() == userId) {
                    output.add(eventList.get(j));        
                }
            }
        }
        return output;
    }
    
    public List<EventEntity> getAllEventEntitiesByUnivCodeAndDeptCodeAndThingCodeAndItemNum(String univCode, String deptCode, String thingCode, int itemNum) throws InternalDataBaseException { // 일단 이걸로
        int itemId;
        try { 
            itemId = getItemEntityByUnivCodeAndDeptCodeAndThingCodeAndItemNum(univCode, deptCode, thingCode, itemNum).getId();
        } catch(NotFoundOnDataBaseException e) {
            return new ArrayList<>();
        }
        
        return eventRepo.findByItemId(itemId);
    }
    
    public EventEntity getEventEntityByUnivCodeAndDeptCodeAndThingCodeAndItemNumAndEventNum(String univCode, String deptCode, String thingCode, int itemNum, int eventNum) throws NotFoundOnDataBaseException, InternalDataBaseException { // done
        int itemId = getItemEntityByUnivCodeAndDeptCodeAndThingCodeAndItemNum(univCode, deptCode, thingCode, itemNum).getId();
        List<EventEntity> eventList = eventRepo.findByItemIdAndNum(itemId, eventNum);
        if(eventList.size() == 0) {
            throw new NotFoundOnDataBaseException("RepositoryManager.getEventEntityByUnivCodeAndDeptCodeAndThingCodeAndItemNumAndEventNum()");
        } else if(eventList.size() == 1) {
            return eventList.get(0);
        } else {
            throw new InternalDataBaseException("RepositoryManager.getEventEntityByUnivCodeAndDeptCodeAndThingCodeAndItemNumAndEventNum()");
        }
    }
    
    public void checkEventDuplication(String univCode, String deptCode, String thingCode, int itemNum, int eventNum) throws CodeDuplicationException, InternalDataBaseException { // 일단 이걸로
        int itemId;
        try { 
            itemId = getItemEntityByUnivCodeAndDeptCodeAndThingCodeAndItemNum(univCode, deptCode, thingCode, itemNum).getId();
        } catch(NotFoundOnDataBaseException e) {
            return ;
        }
        
        List<EventEntity> eventList = eventRepo.findByItemIdAndNum(itemId, eventNum);
        if(eventList.size() != 0) {
            throw new CodeDuplicationException("RepositoryManager.checkEventDuplication()");
        }
    }
    
    public EventEntity saveEvent(EventEntity event) {
        return eventRepo.save(event);
    }
}