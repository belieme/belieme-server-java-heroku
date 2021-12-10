package com.belieme.server.data.common;

import java.util.*;

import com.belieme.server.data.university.*;
import com.belieme.server.data.department.*;
import com.belieme.server.data.major.*;
import com.belieme.server.data.user.*;
import com.belieme.server.data.permission.*;
import com.belieme.server.data.thing.*;
import com.belieme.server.data.item.*;
import com.belieme.server.data.event.*;

import com.belieme.server.data.exception.*;

public class RepositoryManager { //TODO exception data.exception으로 바꾸기
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
    
    public UniversityEntity getUnivEntityByUnivCode(String univCode) throws NotFoundOnDataBaseException, UniqueKeyViolationException { // done
        List<UniversityEntity> univListFromDb = univRepo.findByCode(univCode);
        if(univListFromDb.size() == 0) {
            throw new NotFoundOnDataBaseException("RepositoryManager.getUnivEntityByUnivCode()");
        } else if(univListFromDb.size() == 1) {
            return univListFromDb.get(0);
        } else {
            throw new UniqueKeyViolationException("RepositoryManager.getUnivEntityByUnivCode()");
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
    
    public boolean doesUnivDuplicate(String univCode) { // done
        List<UniversityEntity> univListFromDb = univRepo.findByCode(univCode);
        if(univListFromDb.size() != 0) {
            return true;
        }
        return false;
    }
    
    public UniversityEntity saveUniv(UniversityEntity univ) {
        return univRepo.save(univ);
    }
    
    public List<DepartmentEntity> getAllDeptEntitiesByUnivCode(String univCode) throws UniqueKeyViolationException { // 일단 이걸로
        int univId;
        try {
        	System.out.println("AAAAAAAAAAAAAAAAAAAA");
            univId = getUnivEntityByUnivCode(univCode).getId();
            System.out.println("BBBBBBBBBBBBBBBBBBBB");
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
    
    public DepartmentEntity getDeptEntityByUnivCodeAndDeptCode(String univCode, String deptCode) throws NotFoundOnDataBaseException, UniqueKeyViolationException { // done
        List<DepartmentEntity> deptListFromDb = deptRepo.findByUnivIdAndCode(getUnivEntityByUnivCode(univCode).getId(), deptCode);
        if(deptListFromDb.size() == 0) {
            throw new NotFoundOnDataBaseException("RepositoryManager.getDeptEntityByUnivCodeAndDeptCode()");
        } else if(deptListFromDb.size() == 1) {
            return deptListFromDb.get(0);
        } else {
            throw new UniqueKeyViolationException("RepositoryManager.getDeptEntityByUnivCodeAndDeptCode()");
        }
    }
    
    public boolean doesDeptDuplicate(String univCode, String deptCode) throws UniqueKeyViolationException { // done
        int univId;
        try {
            univId = getUnivEntityByUnivCode(univCode).getId();    
        } catch(NotFoundOnDataBaseException e) {
            return false;
        }
        
        List<DepartmentEntity> deptListFromDb = deptRepo.findByUnivIdAndCode(univId, deptCode);
        if(deptListFromDb.size() != 0) {
            return true;
        }
        return false;
    }
    
    public DepartmentEntity saveDept(DepartmentEntity dept) {
        return deptRepo.save(dept);
    }
    
    public List<MajorEntity> getAllMajorEntitiesByUnivCode(String univCode) throws UniqueKeyViolationException { // 일단 이걸로
    	System.out.println("AAAAAAAAAAAAAAAA");
        List<Integer> deptIdListByUnivCode = new ArrayList<>();
        List<DepartmentEntity> deptEntityListByUnivCode = getAllDeptEntitiesByUnivCode(univCode);
        System.out.println("BBBBBBBBBBBBBBBB");
        
        for(int i = 0; i < deptEntityListByUnivCode.size(); i++) {
            deptIdListByUnivCode.add(deptEntityListByUnivCode.get(i).getId());
        }
        System.out.println("CCCCCCCCCCCCCCCC");
        return majorRepo.findAllByDeptId(deptIdListByUnivCode);
    }
    
    public List<MajorEntity> getAllMajorEntitiesByUnivCodeAndDeptCode(String univCode, String deptCode) throws UniqueKeyViolationException { // 일단 이걸로
        List<MajorEntity> majorEntitiesByUnivCodeAndDeptCode = new ArrayList<>();
        try {
            DepartmentEntity dept = getDeptEntityByUnivCodeAndDeptCode(univCode, deptCode);
            majorEntitiesByUnivCodeAndDeptCode = majorRepo.findByDeptId(dept.getId());    
        } catch(NotFoundOnDataBaseException e) {   
        }
        return majorEntitiesByUnivCodeAndDeptCode;
    }
    
    public MajorEntity getMajorEntityByUnivCodeAndMajorCode(String univCode, String majorCode) throws NotFoundOnDataBaseException, UniqueKeyViolationException { // done
        List<MajorEntity> majorEntitiesByUnivCode = getAllMajorEntitiesByUnivCode(univCode);
        MajorEntity output = null;
        for(int i = 0; i < majorEntitiesByUnivCode.size(); i++) {
            if(majorCode.equalsIgnoreCase(majorEntitiesByUnivCode.get(i).getCode())) {
                if(output == null) {
                    output = majorEntitiesByUnivCode.get(i);    
                } else {
                    throw new UniqueKeyViolationException("RepositoryManager.getMajorEntityByUnivCodeAndMajorCode()");
                }
            }
        }
        if(output == null) {
            throw new NotFoundOnDataBaseException("RepositoryManager.getMajorEntityByUnivCodeAndMajorCode()");
        }
        return output;
    }
    
    public boolean doesMajorDuplicate(String univCode, String majorCode) throws UniqueKeyViolationException { // done
    	System.out.println("AAAAAAAAAAAA");
        List<MajorEntity> majorEntitiesByUnivCode = getAllMajorEntitiesByUnivCode(univCode);
        System.out.println("BBBBBBBBBBBB");
        for(int i = 0; i < majorEntitiesByUnivCode.size(); i++) {
            if(majorCode.equalsIgnoreCase(majorEntitiesByUnivCode.get(i).getCode())) {
                return true;
            }
        }
        return false;
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
    
    public UserEntity getUserEntityByUnivCodeAndStudentId(String univCode, String studentId) throws NotFoundOnDataBaseException, UniqueKeyViolationException { // done
        int univId = getUnivEntityByUnivCode(univCode).getId();
        List<UserEntity> userList = userRepo.findByUnivIdAndStudentId(univId, studentId);
        
        if(userList.size() == 1) {
            return userList.get(0);
        } else if(userList.size() == 0) {
            throw new NotFoundOnDataBaseException("RepositoryManager.getUserEntityByUnivCodeAndStudentId()");
        } else {
            throw new UniqueKeyViolationException("RepositoryManager.getUserEntityByUnivCodeAndStudentId()");
        }
    }
    
    public UserEntity getUserEntityByToken(String token) throws NotFoundOnDataBaseException, UniqueKeyViolationException { // done
        List<UserEntity> userList = userRepo.findByToken(token);
        
        if(userList.size() == 1) {
            return userList.get(0);
        } else if(userList.size() == 0) {
            throw new NotFoundOnDataBaseException("RepositoryManager.getUserEntityByToken()");
        } else {
            throw new UniqueKeyViolationException("RepositoryManager.getUserEntityByToken()");
        }
    }
    
    public UserEntity saveUser(UserEntity user) {
        return userRepo.save(user);
    }
    
    public boolean doesUserDuplicateOnUnivCodeAndStudentId(String univCode, String studentId) throws UniqueKeyViolationException { // 일단 이걸로
        int univId;
        try { 
            univId = getUnivEntityByUnivCode(univCode).getId();
        } catch(NotFoundOnDataBaseException e) {
            return false;
        }
        
        List<UserEntity> userListFromDb = userRepo.findByUnivIdAndStudentId(univId, studentId);
        if(userListFromDb.size() != 0) {
            return true;
        }
        return false;
    }
    
    public boolean doesUserDuplicateOnToken(String token) { // done
        List<UserEntity> userListFromDb = userRepo.findByToken(token);
        if(userListFromDb.size() != 0) {
            return true;
        }
        return false;
    }
    
    public List<PermissionEntity> getAllPermissionEntitiesByUnivCodeAndDeptCode(String univCode, String deptCode) throws UniqueKeyViolationException { // 일단 이걸로
        try {
            int deptId = getDeptEntityByUnivCodeAndDeptCode(univCode, deptCode).getId();
            return permissionRepo.findByDeptId(deptId);
        } catch(NotFoundOnDataBaseException e) {
            return new ArrayList<>();
        }
    }
    
    public List<PermissionEntity> getAllPermissionEntitiesByUnivCodeAndStudentId(String univCode, String studentId) throws UniqueKeyViolationException { // 일단 이걸로
        try {
            int userId = getUserEntityByUnivCodeAndStudentId(univCode, studentId).getId();
            return permissionRepo.findByUserId(userId);
        } catch(NotFoundOnDataBaseException e) {
            return new ArrayList<>();
        }
    }
    
    public PermissionEntity getPermissionEntityByUnivCodeAndStudentIdAndDeptCode(String univCode, String studentId, String deptCode) throws NotFoundOnDataBaseException, UniqueKeyViolationException { // done
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
            throw new UniqueKeyViolationException("RepositoryManager,getPermissionEntityByUnivCodeAndStudentIdAndDeptCode()");
        }
    }
    
    public boolean doesPermissionDuplicate(String univCode, String studentId, String deptCode) throws UniqueKeyViolationException { // 일단 이걸로
        int userId;
        int deptId;
        try {
            userId = getUserEntityByUnivCodeAndStudentId(univCode, studentId).getId();
            deptId = getDeptEntityByUnivCodeAndDeptCode(univCode, deptCode).getId();
        } catch(NotFoundOnDataBaseException e) {
            return false;
        }
        
        List<PermissionEntity> permissionList = permissionRepo.findByUserIdAndDeptId(userId, deptId);
        if(permissionList.size() != 0) {
            return true;
        }
        return false;
    }
    
    public PermissionEntity savePermission(PermissionEntity permission) {
        return permissionRepo.save(permission);
    }
    
    public ThingEntity getThingEntityById(int id) throws NotFoundOnDataBaseException {
        ThingEntity output = thingRepo.findById(id).get();
        if(output == null) {
            throw new NotFoundOnDataBaseException("RepositoryManager.getThingEntityById()");
        } else {
            return output;
        }
    }
    
    public List<ThingEntity> getAllThingEntitiesByUnivCodeAndDeptCode(String univCode, String deptCode) throws UniqueKeyViolationException { // 일단 이걸로
        try { 
            int deptId = getDeptEntityByUnivCodeAndDeptCode(univCode, deptCode).getId();
            return thingRepo.findByDeptId(deptId);
        } catch(NotFoundOnDataBaseException e) {
            return new ArrayList<>();
        }
    }
    
    public ThingEntity getThingEntityByUnivCodeAndDeptCodeAndThingCode(String univCode, String deptCode, String thingCode) throws NotFoundOnDataBaseException, UniqueKeyViolationException { // done
        int deptId = getDeptEntityByUnivCodeAndDeptCode(univCode, deptCode).getId();
        List<ThingEntity> thingList = thingRepo.findByDeptIdAndCode(deptId, thingCode);
        if(thingList.size() == 0) {
            throw new NotFoundOnDataBaseException("RepositoryManager.getThingEntityByUnivCodeAndDeptCodeAndThingCode()");
        } else if(thingList.size() == 1) {
            return thingList.get(0);
        } else {
            throw new UniqueKeyViolationException("RepositoryManager.getThingEntityByUnivCodeAndDeptCodeAndThingCode()");
        }
    }
    
    public ThingEntity saveThing(ThingEntity thing) {
        return thingRepo.save(thing);
    }
    
    public boolean doesThingDuplicate(String univCode, String deptCode, String thingCode) throws UniqueKeyViolationException { // 일단 이걸로
        int deptId;
        try {
            deptId = getDeptEntityByUnivCodeAndDeptCode(univCode, deptCode).getId();
        } catch(NotFoundOnDataBaseException e) {
            return false;
        }
        
        List<ThingEntity> thingList = thingRepo.findByDeptIdAndCode(deptId, thingCode);
        if(thingList.size() != 0) {
            return true;
        }
        return false;
    }
    
    public ItemEntity getItemEntityById(int id) throws NotFoundOnDataBaseException { 
        ItemEntity output = itemRepo.findById(id).get();
        if(output == null) {
            throw new NotFoundOnDataBaseException("RepositoryManager.getItemEntityById()");
        } else {
            return output;
        }
    }
    
    public List<ItemEntity> getAllItemEntitiesByUnivCodeAndDeptCodeAndThingCode(String univCode, String deptCode, String thingCode) throws UniqueKeyViolationException { // 일단 이걸로
        try {
            List<ItemEntity> output = new ArrayList<>();
            ThingEntity thing = getThingEntityByUnivCodeAndDeptCodeAndThingCode(univCode, deptCode, thingCode);
            output.addAll(itemRepo.findByThingId(thing.getId()));
            return output;
        } catch(NotFoundOnDataBaseException e) {
            return new ArrayList<>();
        }
    }
    
    public ItemEntity getItemEntityByUnivCodeAndDeptCodeAndThingCodeAndItemNum(String univCode, String deptCode, String thingCode, int itemNum) throws NotFoundOnDataBaseException, UniqueKeyViolationException { // done
        int thingId = getThingEntityByUnivCodeAndDeptCodeAndThingCode(univCode, deptCode, thingCode).getId();
        List<ItemEntity> itemList = itemRepo.findByThingIdAndNum(thingId, itemNum);
        if(itemList.size() == 0) {
            throw new NotFoundOnDataBaseException("RepositoryManager.getItemEntityByUnivCodeAndDeptCodeAndThingCodeAndItemNum()");
        } else if(itemList.size() == 1) {
            return itemList.get(0);
        } else {
            throw new UniqueKeyViolationException("RepositoryManager.getItemEntityByUnivCodeAndDeptCodeAndThingCodeAndItemNum()");
        }
    }
    
    public boolean doesItemDuplicate(String univCode, String deptCode, String thingCode, int itemNum) throws UniqueKeyViolationException { // 일단 이걸로
        int thingId;
        try {
            thingId= getThingEntityByUnivCodeAndDeptCodeAndThingCode(univCode, deptCode, thingCode).getId();
        } catch(NotFoundOnDataBaseException e) {
            return false;
        }
        
        List<ItemEntity> itemList = itemRepo.findByThingIdAndNum(thingId, itemNum);
        if(itemList.size() != 0) {
            return true;
        }
        return false;
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
    
    public List<EventEntity> getAllEventEntitiesByUnivCodeAndDeptCode(String univCode, String deptCode) throws UniqueKeyViolationException { // 일단 이걸로
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
    
    public List<EventEntity> getAllEventEntitiesByUnivCodeAndDeptCodeAndUserStudentId(String univCode, String deptCode, String userStudentId) throws UniqueKeyViolationException { // 일단 이걸로
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
                if(eventList.get(j).getUserId() == userId) {
                    output.add(eventList.get(j));        
                }
            }
        }
        return output;
    }
    
    public List<EventEntity> getAllEventEntitiesByUnivCodeAndDeptCodeAndThingCodeAndItemNum(String univCode, String deptCode, String thingCode, int itemNum) throws UniqueKeyViolationException { // 일단 이걸로
        int itemId;
        try { 
            itemId = getItemEntityByUnivCodeAndDeptCodeAndThingCodeAndItemNum(univCode, deptCode, thingCode, itemNum).getId();
        } catch(NotFoundOnDataBaseException e) {
            return new ArrayList<>();
        }
        return eventRepo.findByItemId(itemId);
    }
    
    public EventEntity getEventEntityByUnivCodeAndDeptCodeAndThingCodeAndItemNumAndEventNum(String univCode, String deptCode, String thingCode, int itemNum, int eventNum) throws NotFoundOnDataBaseException, UniqueKeyViolationException { // done
        int itemId = getItemEntityByUnivCodeAndDeptCodeAndThingCodeAndItemNum(univCode, deptCode, thingCode, itemNum).getId();
        List<EventEntity> eventList = eventRepo.findByItemIdAndNum(itemId, eventNum);
        if(eventList.size() == 0) {
            throw new NotFoundOnDataBaseException("RepositoryManager.getEventEntityByUnivCodeAndDeptCodeAndThingCodeAndItemNumAndEventNum()");
        } else if(eventList.size() == 1) {
            return eventList.get(0);
        } else {
            throw new UniqueKeyViolationException("RepositoryManager.getEventEntityByUnivCodeAndDeptCodeAndThingCodeAndItemNumAndEventNum()");
        }
    }
    
    public boolean doesEventDuplicate(String univCode, String deptCode, String thingCode, int itemNum, int eventNum) throws UniqueKeyViolationException { // 일단 이걸로
        int itemId;
        try { 
            itemId = getItemEntityByUnivCodeAndDeptCodeAndThingCodeAndItemNum(univCode, deptCode, thingCode, itemNum).getId();
        } catch(NotFoundOnDataBaseException e) {
            return false;
        }
        
        List<EventEntity> eventList = eventRepo.findByItemIdAndNum(itemId, eventNum);
        if(eventList.size() != 0) {
            return true;
        }
        return false;
    }
    
    public EventEntity saveEvent(EventEntity event) {
        return eventRepo.save(event);
    }
}