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
    
    public UniversityEntity getUnivEntityByUnivCode(String univCode) throws NotFoundOnDataBaseException, InternalDataBaseException {
        List<UniversityEntity> univListFromDb = univRepo.findByCode(univCode);
        if(univListFromDb.size() == 0) {
            throw new NotFoundOnDataBaseException();
        } else if(univListFromDb.size() == 1) {
            return univListFromDb.get(0);
        } else {
            throw new InternalDataBaseException();
        }
    }
    
    public UniversityEntity getUnivEntityById(int id) throws NotFoundOnDataBaseException {
        UniversityEntity output = univRepo.findById(id).get();
        if(output == null) {
            throw new NotFoundOnDataBaseException();
        } else {
            return output;
        }
    }
    
    public void checkUnivDuplicate(String univCode) throws CodeDuplicationException {
        List<UniversityEntity> univListFromDb = univRepo.findByCode(univCode);
        if(univListFromDb.size() != 0) {
            throw new CodeDuplicationException();
        }
    }
    
    public UniversityEntity saveUniv(UniversityEntity univ) {
        return univRepo.save(univ);
    }
    
    public List<DepartmentEntity> getAllDeptEntitiesByUnivCode(String univCode) throws InternalDataBaseException {
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
            throw new NotFoundOnDataBaseException();
        } else {
            return output;
        }
    }
    
    public DepartmentEntity getDeptEntityByUnivCodeAndDeptCode(String univCode, String deptCode) throws InternalDataBaseException, NotFoundOnDataBaseException {
        List<DepartmentEntity> deptListFromDb = deptRepo.findByUnivIdAndCode(getUnivEntityByUnivCode(univCode).getId(), deptCode);
        if(deptListFromDb.size() == 0) {
            throw new NotFoundOnDataBaseException();
        } else if(deptListFromDb.size() == 1) {
            return deptListFromDb.get(0);
        } else {
            throw new InternalDataBaseException();
        }
    }
    
    public void checkDeptDuplication(String univCode, String deptCode) throws CodeDuplicationException, NotFoundOnDataBaseException, InternalDataBaseException {
        int univId = getUnivEntityByUnivCode(univCode).getId();
        List<DepartmentEntity> deptListFromDb = deptRepo.findByUnivIdAndCode(univId, deptCode);
        if(deptListFromDb.size() != 0) {
            throw new CodeDuplicationException();
        }
    }
    
    public DepartmentEntity saveDept(DepartmentEntity dept) {
        return deptRepo.save(dept);
    }
    
    public List<MajorEntity> getAllMajorEntitiesByUnivCode(String univCode) throws InternalDataBaseException {
        List<Integer> deptIdListByUnivCode = new ArrayList<>();
        List<DepartmentEntity> deptEntityListByUnivCode = getAllDeptEntitiesByUnivCode(univCode);
        
        for(int i = 0; i < deptEntityListByUnivCode.size(); i++) {
            deptIdListByUnivCode.add(deptEntityListByUnivCode.get(i).getId());
        }
        
        return majorRepo.findAllByDeptId(deptIdListByUnivCode);
    }
    
    public List<MajorEntity> getAllMajorEntitiesByUnivCodeAndDeptCode(String univCode, String deptCode) throws InternalDataBaseException {
        List<MajorEntity> majorEntitiesByUnivCodeAndDeptCode = new ArrayList<>();
        try {
            DepartmentEntity dept = getDeptEntityByUnivCodeAndDeptCode(univCode, deptCode);
            majorEntitiesByUnivCodeAndDeptCode = majorRepo.findByDeptId(dept.getId());    
        } catch(NotFoundOnDataBaseException e) {   
        }
        return majorEntitiesByUnivCodeAndDeptCode;
    }
    
    public MajorEntity getMajorEntityByUnivCodeAndMajorCode(String univCode, String majorCode) throws NotFoundOnDataBaseException, InternalDataBaseException {
        List<MajorEntity> majorEntitiesByUnivCode = getAllMajorEntitiesByUnivCode(univCode);
        MajorEntity output = null;
        for(int i = 0; i < majorEntitiesByUnivCode.size(); i++) {
            if(majorCode.equals(majorEntitiesByUnivCode.get(i).getCode())) {
                if(output == null) {
                    output = majorEntitiesByUnivCode.get(i);    
                } else {
                    throw new InternalDataBaseException();
                }
            }
        }
        if(output == null) {
            throw new NotFoundOnDataBaseException();
        }
        return output;
    }
    
    public void checkMajorDuplication(String univCode, String majorCode) throws CodeDuplicationException, InternalDataBaseException {
        List<MajorEntity> majorEntitiesByUnivCode = getAllMajorEntitiesByUnivCode(univCode);
        for(int i = 0; i < majorEntitiesByUnivCode.size(); i++) {
            if(majorCode.equals(majorEntitiesByUnivCode.get(i).getCode())) {
                throw new CodeDuplicationException();
            }
        }
    }
    
    public MajorEntity saveMajor(MajorEntity major) {
        return majorRepo.save(major);
    }
    
    public UserEntity getUserEntityById(int id) throws NotFoundOnDataBaseException {
        UserEntity output = userRepo.findById(id).get();
        if(output == null) {
            throw new NotFoundOnDataBaseException();
        } else {
            return output;
        }
    }
    
    public UserEntity getUserEntityByUnivCodeAndStudentId(String univCode, String studentId) throws NotFoundOnDataBaseException, InternalDataBaseException {
        int univId = getUnivEntityByUnivCode(univCode).getId();
        List<UserEntity> userList = userRepo.findByUnivIdAndStudentId(univId, studentId);
        
        if(userList.size() == 1) {
            return userList.get(0);
        } else if(userList.size() == 0) {
            throw new NotFoundOnDataBaseException();
        } else {
            throw new InternalDataBaseException();
        }
    }
    
    public UserEntity getUserEntityByToken(String token) throws NotFoundOnDataBaseException, InternalDataBaseException {
        List<UserEntity> userList = userRepo.findByToken(token);
        
        if(userList.size() == 1) {
            return userList.get(0);
        } else if(userList.size() == 0) {
            throw new NotFoundOnDataBaseException();
        } else {
            throw new InternalDataBaseException();
        }
    }
    
    public UserEntity saveUser(UserEntity user) {
        return userRepo.save(user);
    }
    
    public void checkUserDuplicationByUnivCodeAndStudentId(String univCode, String studentId) throws CodeDuplicationException, NotFoundOnDataBaseException, InternalDataBaseException {
        int univId = getUnivEntityByUnivCode(univCode).getId();
        List<UserEntity> userListFromDb = userRepo.findByUnivIdAndStudentId(univId, studentId);
        if(userListFromDb.size() != 0) {
            throw new CodeDuplicationException();
        }
    }
    
    public void checkUserDuplicationByToken(String token) throws CodeDuplicationException {
        List<UserEntity> userListFromDb = userRepo.findByToken(token);
        if(userListFromDb.size() != 0) {
            throw new CodeDuplicationException();
        }
    }
    
    public List<PermissionEntity> getAllPermissionEntitiesByUnivCodeAndDeptCode(String univCode, String deptCode) throws InternalDataBaseException {
        try {
            int deptId = getDeptEntityByUnivCodeAndDeptCode(univCode, deptCode).getId();
            return permissionRepo.findByDeptId(deptId);
        } catch(NotFoundOnDataBaseException e) {
            return new ArrayList<>();
        }
    }
    
    public List<PermissionEntity> getAllPermissionEntitiesByUnivCodeAndStudentId(String univCode, String studentId) throws InternalDataBaseException {
        try {
            int userId = getUserEntityByUnivCodeAndStudentId(univCode, studentId).getId();
            return permissionRepo.findByUserId(userId);
        } catch(NotFoundOnDataBaseException e) {
            return new ArrayList<>();
        }
    }
    
    public PermissionEntity getPermissionEntityByUnivCodeAndStudentIdAndDeptCode(String univCode, String studentId, String deptCode) throws NotFoundOnDataBaseException, InternalDataBaseException {
        int deptId;
        int userId;
        
        deptId = getDeptEntityByUnivCodeAndDeptCode(univCode, deptCode).getId();
        userId = getUserEntityByUnivCodeAndStudentId(univCode, studentId).getId();
        
        List<PermissionEntity> permissionListFromDb = permissionRepo.findByUserIdAndDeptId(userId, deptId);
        if(permissionListFromDb.size() == 0) {
            throw new NotFoundOnDataBaseException();
        } else if (permissionListFromDb.size() == 1) {
            return permissionListFromDb.get(0);
        } else {
            throw new InternalDataBaseException();
        }
    }
    
    public void checkPermissionDuplication(String univCode, String studentId, String deptCode) throws CodeDuplicationException, NotFoundOnDataBaseException, InternalDataBaseException {
        int userId = getUserEntityByUnivCodeAndStudentId(univCode, studentId).getId();
        int deptId = getDeptEntityByUnivCodeAndDeptCode(univCode, deptCode).getId();
        List<PermissionEntity> permissionList = permissionRepo.findByUserIdAndDeptId(userId, deptId);
        if(permissionList.size() != 0) {
            throw new CodeDuplicationException();
        }
    }
    
    public PermissionEntity savePermission(PermissionEntity permission) {
        return permissionRepo.save(permission);
    }
    
    public ThingEntity getThingEntity(int id) throws NotFoundOnDataBaseException {
        ThingEntity output = thingRepo.findById(id).get();
        if(output == null) {
            throw new NotFoundOnDataBaseException();
        } else {
            return output;
        }
    }
    
    public List<ThingEntity> getAllThingEntitiesByUnivCodeAndDeptCode(String univCode, String deptCode) throws NotFoundOnDataBaseException, InternalDataBaseException {
        int deptId = getDeptEntityByUnivCodeAndDeptCode(univCode, deptCode).getId();
        List<ThingEntity> output = thingRepo.findByDeptId(deptId);
        return output;
    }
    
    public ThingEntity getThingEntityByUnivCodeAndDeptCodeAndThingCode(String univCode, String deptCode, String thingCode) throws NotFoundOnDataBaseException, InternalDataBaseException {
        int deptId = getDeptEntityByUnivCodeAndDeptCode(univCode, deptCode).getId();
        List<ThingEntity> thingList = thingRepo.findByDeptIdAndCode(deptId, thingCode);
        if(thingList.size() == 0) {
            throw new NotFoundOnDataBaseException();
        } else if(thingList.size() == 1) {
            return thingList.get(0);
        } else {
            throw new InternalDataBaseException();
        }
    }
    
    public ThingEntity saveThing(ThingEntity thing) {
        return thingRepo.save(thing);
    }
    
    public void checkThingDuplication(String univCode, String deptCode, String thingCode) throws CodeDuplicationException, NotFoundOnDataBaseException, InternalDataBaseException {
        int deptId = getDeptEntityByUnivCodeAndDeptCode(univCode, deptCode).getId();
        List<ThingEntity> thingList = thingRepo.findByDeptIdAndCode(deptId, thingCode);
        if(thingList.size() != 0) {
            throw new CodeDuplicationException();
        }
    }
    
    public ItemEntity getItemEntityById(int id) throws NotFoundOnDataBaseException {
        ItemEntity output = itemRepo.findById(id).get();
        if(output == null) {
            throw new NotFoundOnDataBaseException();
        } else {
            return output;
        }
    }
    
    public ItemEntity getItemEntityByUnivCodeAndDeptCodeAndThingCodeAndItemNum(String univCode, String deptCode, String thingCode, int itemNum) throws NotFoundOnDataBaseException, InternalDataBaseException {
        int thingId = getThingEntityByUnivCodeAndDeptCodeAndThingCode(univCode, deptCode, thingCode).getId();
        List<ItemEntity> itemList = itemRepo.findByThingIdAndNum(thingId, itemNum);
        if(itemList.size() == 0) {
            throw new NotFoundOnDataBaseException();
        } else if(itemList.size() == 1) {
            return itemList.get(0);
        } else {
            throw new InternalDataBaseException();
        }
    }
    
    public List<ItemEntity> getAllItemEntitiesByUnivCodeAndDeptCode(String univCode, String deptCode) throws NotFoundOnDataBaseException, InternalDataBaseException {
        List<ThingEntity> thingList = getAllThingEntitiesByUnivCodeAndDeptCode(univCode, deptCode);
        List<ItemEntity> output = new ArrayList<>();
        for(int i = 0; i < thingList.size(); i++) {
            int thingId = thingList.get(i).getId();
            output.addAll(itemRepo.findByThingId(thingId));
        }
        return output;
    }   
    
    public List<ItemEntity> getAllItemEntitiesByUnivCodeAndDeptCodeAndThingCode(String univCode, String deptCode, String thingCode) throws NotFoundOnDataBaseException, InternalDataBaseException {
        ThingEntity thing = getThingEntityByUnivCodeAndDeptCodeAndThingCode(univCode, deptCode, thingCode);
        List<ItemEntity> output = new ArrayList<>();
        output.addAll(itemRepo.findByThingId(thing.getId()));
        
        return output;
    }
    
    public void checkItemDuplication(String univCode, String deptCode, String thingCode, int itemNum) throws CodeDuplicationException, NotFoundOnDataBaseException, InternalDataBaseException {
        int thingId = getThingEntityByUnivCodeAndDeptCodeAndThingCode(univCode, deptCode, thingCode).getId();
        List<ItemEntity> itemList = itemRepo.findByThingIdAndNum(thingId, itemNum);
        if(itemList.size() != 0) {
            throw new CodeDuplicationException();
        }
    }
    
    public ItemEntity saveItem(ItemEntity item) {
        return itemRepo.save(item);
    }
    
    public EventEntity getEventEntityById(int id) throws NotFoundOnDataBaseException {
        EventEntity output = eventRepo.findById(id).get();
        if(output == null) {
            throw new NotFoundOnDataBaseException();
        } else {
            return output;
        }
    }
    
    public List<EventEntity> getAllEventEntitiesByUnivCodeAndDeptCode(String univCode, String deptCode) throws NotFoundOnDataBaseException, InternalDataBaseException {
        List<ItemEntity> itemList = getAllItemEntitiesByUnivCodeAndDeptCode(univCode, deptCode);
        List<EventEntity> output = new ArrayList<>();
        for(int i = 0; i < itemList.size(); i++) {
            int itemId = itemList.get(i).getId();
            output.addAll(eventRepo.findByItemId(itemId));
        }
        return output;
    }
    
    public List<EventEntity> getAllEventEntitiesByUnivCodeAndDeptCodeAndUserStudnetId(String univCode, String deptCode, String userStudentId) throws NotFoundOnDataBaseException, InternalDataBaseException {
        List<ItemEntity> itemList = getAllItemEntitiesByUnivCodeAndDeptCode(univCode, deptCode);
        
        int userId = getUserEntityByUnivCodeAndStudentId(univCode, userStudentId).getId();
        
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
    
    public List<EventEntity> getAllEventEntitiesByUnivCodeAndDeptCodeAndThingCodeAndItemNum(String univCode, String deptCode, String thingCode, int itemNum) throws NotFoundOnDataBaseException, InternalDataBaseException {
        int itemId = getItemEntityByUnivCodeAndDeptCodeAndThingCodeAndItemNum(univCode, deptCode, thingCode, itemNum).getId();
        return eventRepo.findByItemId(itemId);
    }
    
    public EventEntity getEventEntityByUnivCodeAndDeptCodeAndThingCodeAndItemNumAndEventNum(String univCode, String deptCode, String thingCode, int itemNum, int eventNum) throws NotFoundOnDataBaseException, InternalDataBaseException {
        int itemId = getItemEntityByUnivCodeAndDeptCodeAndThingCodeAndItemNum(univCode, deptCode, thingCode, itemNum).getId();
        List<EventEntity> eventList = eventRepo.findByItemIdAndNum(itemId, eventNum);
        if(eventList.size() == 0) {
            throw new NotFoundOnDataBaseException();
        } else if(eventList.size() == 1) {
            return eventList.get(0);
        } else {
            throw new InternalDataBaseException();
        }
    }
    
    public void checkEventDuplication(String univCode, String deptCode, String thingCode, int itemNum, int eventNum) throws CodeDuplicationException, NotFoundOnDataBaseException, InternalDataBaseException {
        int itemId = getItemEntityByUnivCodeAndDeptCodeAndThingCodeAndItemNum(univCode, deptCode, thingCode, itemNum).getId();
        List<EventEntity> eventList = eventRepo.findByItemIdAndNum(itemId, eventNum);
        if(eventList.size() != 0) {
            throw new CodeDuplicationException();
        }
    }
    
    public EventEntity saveEvent(EventEntity event) {
        return eventRepo.save(event);
    }
}