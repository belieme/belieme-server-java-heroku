package com.hanyang.belieme.demoserver.item;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.hanyang.belieme.demoserver.thing.*;
import com.hanyang.belieme.demoserver.university.UniversityRepository;
import com.hanyang.belieme.demoserver.user.User;
import com.hanyang.belieme.demoserver.user.UserDB;
import com.hanyang.belieme.demoserver.user.UserRepository;
import com.hanyang.belieme.demoserver.user.permission.PermissionRepository;
import com.hanyang.belieme.demoserver.event.*;
import com.hanyang.belieme.demoserver.exception.NotFoundException;
import com.hanyang.belieme.demoserver.exception.WrongInDataBaseException;
import com.hanyang.belieme.demoserver.common.*;
import com.hanyang.belieme.demoserver.department.Department;
import com.hanyang.belieme.demoserver.department.DepartmentRepository;
import com.hanyang.belieme.demoserver.department.major.MajorRepository;

@RestController
@RequestMapping(path="/univs/{univCode}/depts/{deptCode}/things/{thingId}/items")
public class ItemApiController {
    @Autowired
    private UniversityRepository universityRepository;
    
    @Autowired
    private DepartmentRepository departmentRepository;
    
    @Autowired
    private MajorRepository majorRepository;
    
    @Autowired
    private PermissionRepository permissionRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private ThingRepository thingRepository;

    @Autowired
    private EventRepository eventRepository;

    @GetMapping("")
    public ResponseWrapper<List<Item>> getAllItems(@RequestHeader("user-token") String userToken, @PathVariable String univCode, @PathVariable String deptCode, @PathVariable int thingId) {
        if(userToken == null) {
            return new ResponseWrapper<>(ResponseHeader.LACK_OF_REQUEST_HEADER_EXCEPTION, null);
        }
        
        int deptId;
        try {
            deptId = Department.findIdByUnivCodeAndDeptCode(universityRepository, departmentRepository, univCode, deptCode);
        } catch(NotFoundException e) {
            return new ResponseWrapper<>(ResponseHeader.NOT_FOUND_EXCEPTION, null);
        } catch(WrongInDataBaseException e) {
            return new ResponseWrapper<>(ResponseHeader.WRONG_IN_DATABASE_EXCEPTION, null);
        }
        
        int userId;
        try {
            userId = User.findIdByToken(userRepository, userToken);    
        } catch(NotFoundException e) {
            return new ResponseWrapper<>(ResponseHeader.EXPIRED_USER_TOKEN_EXCEPTION, null);
        } catch(WrongInDataBaseException e) {
            return new ResponseWrapper<>(ResponseHeader.WRONG_IN_DATABASE_EXCEPTION, null);
        }
        
        UserDB userDB = userRepository.findById(userId).get();
        User user;
        if(userDB == null) {
            return new ResponseWrapper<>(ResponseHeader.NOT_FOUND_EXCEPTION, null);
        } else {
            user = userDB.toUser(universityRepository, departmentRepository, majorRepository, permissionRepository);    
        }
        
        if(!user.hasUserPermission(deptCode)) {
            return new ResponseWrapper<>(ResponseHeader.USER_PERMISSION_DENIED_EXCEPTION, null);
        }
         
        Optional<ThingDB> targetThingOptional = thingRepository.findById(thingId);
        if(!targetThingOptional.isPresent()) {
            return new ResponseWrapper<>(ResponseHeader.NOT_FOUND_EXCEPTION, null);
        } else if(targetThingOptional.get().getDepartmentId() != deptId) {
            return new ResponseWrapper<>(ResponseHeader.NOT_FOUND_EXCEPTION, null); //TODO Exception바꿀까?
        }
        
        List<Item> output = new ArrayList<>();       
        List<ItemDB> itemListByThingId = itemRepository.findByThingId(thingId);
        for(int i = 0; i < itemListByThingId.size(); i++) {
            output.add(itemListByThingId.get(i).toItem(universityRepository, departmentRepository, majorRepository, userRepository, thingRepository, eventRepository));    
            
        }
        return new ResponseWrapper<>(ResponseHeader.OK, output);
    }

    @GetMapping("/{itemNum}") 
    public ResponseWrapper<Item> getItemByThingIdAndNum(@RequestHeader("user-token") String userToken, @PathVariable String univCode, @PathVariable String deptCode, @PathVariable int thingId, @PathVariable int itemNum) {
        if(userToken == null) {
            return new ResponseWrapper<>(ResponseHeader.LACK_OF_REQUEST_HEADER_EXCEPTION, null);
        }
        
        int deptId;
        try {
            deptId = Department.findIdByUnivCodeAndDeptCode(universityRepository, departmentRepository, univCode, deptCode);
        } catch(NotFoundException e) {
            return new ResponseWrapper<>(ResponseHeader.NOT_FOUND_EXCEPTION, null);
        } catch(WrongInDataBaseException e) {
            return new ResponseWrapper<>(ResponseHeader.WRONG_IN_DATABASE_EXCEPTION, null);
        }
        
        int userId;
        try {
            userId = User.findIdByToken(userRepository, userToken);    
        } catch(NotFoundException e) {
            return new ResponseWrapper<>(ResponseHeader.EXPIRED_USER_TOKEN_EXCEPTION, null);
        } catch(WrongInDataBaseException e) {
            return new ResponseWrapper<>(ResponseHeader.WRONG_IN_DATABASE_EXCEPTION, null);
        }
        
        UserDB userDB = userRepository.findById(userId).get();
        User user;
        if(userDB == null) {
            return new ResponseWrapper<>(ResponseHeader.NOT_FOUND_EXCEPTION, null);
        } else {
            user = userDB.toUser(universityRepository, departmentRepository, majorRepository, permissionRepository);    
        }
        
        if(!user.hasUserPermission(deptCode)) {
            return new ResponseWrapper<>(ResponseHeader.USER_PERMISSION_DENIED_EXCEPTION, null);
        }
        
        Optional<ThingDB> targetThingOptional = thingRepository.findById(thingId);
        if(!targetThingOptional.isPresent()) {
            return new ResponseWrapper<>(ResponseHeader.NOT_FOUND_EXCEPTION, null);
        } else if(targetThingOptional.get().getDepartmentId() != deptId) {
            return new ResponseWrapper<>(ResponseHeader.NOT_FOUND_EXCEPTION, null); //TODO Exception바꿀까?
        }
        
        List<ItemDB> itemList = itemRepository.findByThingIdAndNum(thingId, itemNum);
        Item output;
        if(itemList.size() == 1) {
            ItemDB itemDB = itemList.get(0);
            output = itemDB.toItem(universityRepository, departmentRepository, majorRepository, userRepository, thingRepository, eventRepository);
            
            return new ResponseWrapper<>(ResponseHeader.OK, output);
        } else if(itemList.size() == 0) {
            return new ResponseWrapper<>(ResponseHeader.NOT_FOUND_EXCEPTION, null);
        } else { //Warning 으로 바꿀까?? 그건 좀 귀찮긴 할 듯
            return new ResponseWrapper<>(ResponseHeader.WRONG_IN_DATABASE_EXCEPTION, null);
        }
    }

    @PostMapping("")
    public ResponseWrapper<Item> createNewItem(@RequestHeader("user-token") String userToken, @PathVariable String univCode, @PathVariable String deptCode, @PathVariable int thingId) {
        if(userToken == null) {
            return new ResponseWrapper<>(ResponseHeader.LACK_OF_REQUEST_HEADER_EXCEPTION, null);
        }
        
        int deptId;
        try {
            deptId = Department.findIdByUnivCodeAndDeptCode(universityRepository, departmentRepository, univCode, deptCode);
        } catch(NotFoundException e) {
            return new ResponseWrapper<>(ResponseHeader.NOT_FOUND_EXCEPTION, null);
        } catch(WrongInDataBaseException e) {
            return new ResponseWrapper<>(ResponseHeader.WRONG_IN_DATABASE_EXCEPTION, null);
        }
        
        int userId;
        try {
            userId = User.findIdByToken(userRepository, userToken);    
        } catch(NotFoundException e) {
            return new ResponseWrapper<>(ResponseHeader.EXPIRED_USER_TOKEN_EXCEPTION, null);
        } catch(WrongInDataBaseException e) {
            return new ResponseWrapper<>(ResponseHeader.WRONG_IN_DATABASE_EXCEPTION, null);
        }
        
        UserDB userDB = userRepository.findById(userId).get();
        User user;
        if(userDB == null) {
            return new ResponseWrapper<>(ResponseHeader.NOT_FOUND_EXCEPTION, null);
        } else {
            user = userDB.toUser(universityRepository, departmentRepository, majorRepository, permissionRepository);    
        }
        
        if(!user.hasStaffPermission(deptCode)) {
            return new ResponseWrapper<>(ResponseHeader.USER_PERMISSION_DENIED_EXCEPTION, null);
        }
         
        Optional<ThingDB> targetThingOptional = thingRepository.findById(thingId);
        if(!targetThingOptional.isPresent()) {
            return new ResponseWrapper<>(ResponseHeader.NOT_FOUND_EXCEPTION, null);
        } else if(targetThingOptional.get().getDepartmentId() != deptId) {
            return new ResponseWrapper<>(ResponseHeader.NOT_FOUND_EXCEPTION, null); //TODO Exception바꿀까?
        }
        
        List<ItemDB> itemListByThingId = itemRepository.findByThingId(thingId);

        Optional<ThingDB> thingOptional = thingRepository.findById(thingId);

        int max = 0;
        for(int i = 0; i < itemListByThingId.size(); i++) {
            ItemDB tmp = itemListByThingId.get(i);
            if(max < tmp.getNum()) {
                max = tmp.getNum();
            }
        }
        ItemDB newItem = new ItemDB(thingId, max+1); 

        if(thingOptional.isPresent()) {
            Item output = itemRepository.save(newItem).toItem(universityRepository, departmentRepository, majorRepository, userRepository, thingRepository, eventRepository);
            
            return new ResponseWrapper<>(ResponseHeader.OK, output);
        }
        else {
            return new ResponseWrapper<>(ResponseHeader.NOT_FOUND_EXCEPTION, null);
        }
    }
}