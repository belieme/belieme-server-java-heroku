package com.hanyang.belieme.demoserver.item;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.hanyang.belieme.demoserver.thing.*;
import com.hanyang.belieme.demoserver.university.University;
import com.hanyang.belieme.demoserver.university.UniversityRepository;
import com.hanyang.belieme.demoserver.user.User;
import com.hanyang.belieme.demoserver.user.UserDB;
import com.hanyang.belieme.demoserver.user.UserRepository;
import com.hanyang.belieme.demoserver.user.permission.PermissionRepository;
import com.hanyang.belieme.demoserver.event.*;
import com.hanyang.belieme.demoserver.exception.NotFoundException;
import com.hanyang.belieme.demoserver.exception.WrongInDataBaseException;
import com.hanyang.belieme.demoserver.common.*;
import com.hanyang.belieme.demoserver.department.DepartmentDB;
import com.hanyang.belieme.demoserver.department.DepartmentRepository;
import com.hanyang.belieme.demoserver.department.DepartmentResponse;
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
    public ResponseWrapper<ListResponse> getAllItems(@RequestHeader("user-token") String userToken, @PathVariable String univCode, @PathVariable String deptCode, @PathVariable int thingId) {
        if(userToken == null) {
            return new ResponseWrapper<>(ResponseHeader.LACK_OF_REQUEST_HEADER_EXCEPTION, null);
        }
        
        University univ;
        try {
            univ = University.findByUnivCode(universityRepository, univCode);
        } catch(NotFoundException e) {
            return new ResponseWrapper<>(ResponseHeader.NOT_FOUND_EXCEPTION, null);
        } catch(WrongInDataBaseException e) {
            return new ResponseWrapper<>(ResponseHeader.WRONG_IN_DATABASE_EXCEPTION, null);
        }
        
        DepartmentResponse dept;
        try {
            dept = DepartmentDB.findByUnivCodeAndDeptCode(universityRepository, departmentRepository, univCode, deptCode).toDepartmentResponse(majorRepository);
        } catch(NotFoundException e) {
            return new ResponseWrapper<>(ResponseHeader.NOT_FOUND_EXCEPTION, null);
        } catch(WrongInDataBaseException e) {
            return new ResponseWrapper<>(ResponseHeader.WRONG_IN_DATABASE_EXCEPTION, null);
        }
        int deptId = dept.getId();
        
        Thing thing;
        try {
            thing = ThingDB.findByThingIdAndDeptId(thingRepository, thingId, deptId).toThing(userRepository, itemRepository, eventRepository);
        } catch(NotFoundException e) {
            return new ResponseWrapper<>(ResponseHeader.NOT_FOUND_EXCEPTION, null);
        }
        
        User user;
        try {
            user = UserDB.findByToken(userRepository, userToken).toUser(departmentRepository, majorRepository, permissionRepository);    
        } catch(NotFoundException e) {
            return new ResponseWrapper<>(ResponseHeader.EXPIRED_USER_TOKEN_EXCEPTION, null);
        } catch(WrongInDataBaseException e) {
            return new ResponseWrapper<>(ResponseHeader.WRONG_IN_DATABASE_EXCEPTION, null);
        }
        
        if(!user.hasUserPermission(deptCode)) {
            return new ResponseWrapper<>(ResponseHeader.USER_PERMISSION_DENIED_EXCEPTION, null);
        }
        
        List<Item> output = new ArrayList<>();       
        List<ItemDB> itemListByThingId = itemRepository.findByThingId(thingId);
        for(int i = 0; i < itemListByThingId.size(); i++) {
            output.add(itemListByThingId.get(i).toItem(userRepository, eventRepository));    
            
        }
        return new ResponseWrapper<>(ResponseHeader.OK, new ListResponse(univ, dept, thing, output));
    }

    @GetMapping("/{itemNum}") 
    public ResponseWrapper<Response> getItemByThingIdAndNum(@RequestHeader("user-token") String userToken, @PathVariable String univCode, @PathVariable String deptCode, @PathVariable int thingId, @PathVariable int itemNum) {
        if(userToken == null) {
            return new ResponseWrapper<>(ResponseHeader.LACK_OF_REQUEST_HEADER_EXCEPTION, null);
        }
        
        University univ;
        try {
            univ = University.findByUnivCode(universityRepository, univCode);
        } catch(NotFoundException e) {
            return new ResponseWrapper<>(ResponseHeader.NOT_FOUND_EXCEPTION, null);
        } catch(WrongInDataBaseException e) {
            return new ResponseWrapper<>(ResponseHeader.WRONG_IN_DATABASE_EXCEPTION, null);
        }
        
        DepartmentResponse dept;
        try {
            dept = DepartmentDB.findByUnivCodeAndDeptCode(universityRepository, departmentRepository, univCode, deptCode).toDepartmentResponse(majorRepository);
        } catch(NotFoundException e) {
            return new ResponseWrapper<>(ResponseHeader.NOT_FOUND_EXCEPTION, null);
        } catch(WrongInDataBaseException e) {
            return new ResponseWrapper<>(ResponseHeader.WRONG_IN_DATABASE_EXCEPTION, null);
        }
        int deptId = dept.getId();
        
        Thing thing;
        try {
            thing = ThingDB.findByThingIdAndDeptId(thingRepository, thingId, deptId).toThing(userRepository, itemRepository, eventRepository);
        } catch(NotFoundException e) {
            return new ResponseWrapper<>(ResponseHeader.NOT_FOUND_EXCEPTION, null);
        }
        
        User user;
        try {
            user = UserDB.findByToken(userRepository, userToken).toUser(departmentRepository, majorRepository, permissionRepository);    
        } catch(NotFoundException e) {
            return new ResponseWrapper<>(ResponseHeader.EXPIRED_USER_TOKEN_EXCEPTION, null);
        } catch(WrongInDataBaseException e) {
            return new ResponseWrapper<>(ResponseHeader.WRONG_IN_DATABASE_EXCEPTION, null);
        }
        
        if(!user.hasUserPermission(deptCode)) {
            return new ResponseWrapper<>(ResponseHeader.USER_PERMISSION_DENIED_EXCEPTION, null);
        }
        
        List<ItemDB> itemList = itemRepository.findByThingIdAndNum(thingId, itemNum);
        Item output;
        if(itemList.size() == 1) {
            ItemDB itemDB = itemList.get(0);
            output = itemDB.toItem(userRepository, eventRepository);
            
            return new ResponseWrapper<>(ResponseHeader.OK, new Response(univ, dept, thing, output));
        } else if(itemList.size() == 0) {
            return new ResponseWrapper<>(ResponseHeader.NOT_FOUND_EXCEPTION, null);
        } else { //Warning 으로 바꿀까?? 그건 좀 귀찮긴 할 듯
            return new ResponseWrapper<>(ResponseHeader.WRONG_IN_DATABASE_EXCEPTION, null);
        }
    }

    @PostMapping("")
    public ResponseWrapper<Response> createNewItem(@RequestHeader("user-token") String userToken, @PathVariable String univCode, @PathVariable String deptCode, @PathVariable int thingId) {
        if(userToken == null) {
            return new ResponseWrapper<>(ResponseHeader.LACK_OF_REQUEST_HEADER_EXCEPTION, null);
        }
        
        University univ;
        try {
            univ = University.findByUnivCode(universityRepository, univCode);
        } catch(NotFoundException e) {
            return new ResponseWrapper<>(ResponseHeader.NOT_FOUND_EXCEPTION, null);
        } catch(WrongInDataBaseException e) {
            return new ResponseWrapper<>(ResponseHeader.WRONG_IN_DATABASE_EXCEPTION, null);
        }
        
        DepartmentResponse dept;
        try {
            dept = DepartmentDB.findByUnivCodeAndDeptCode(universityRepository, departmentRepository, univCode, deptCode).toDepartmentResponse(majorRepository);
        } catch(NotFoundException e) {
            return new ResponseWrapper<>(ResponseHeader.NOT_FOUND_EXCEPTION, null);
        } catch(WrongInDataBaseException e) {
            return new ResponseWrapper<>(ResponseHeader.WRONG_IN_DATABASE_EXCEPTION, null);
        }
        int deptId = dept.getId();
        
        Thing thing;
        try {
            thing = ThingDB.findByThingIdAndDeptId(thingRepository, thingId, deptId).toThing(userRepository, itemRepository, eventRepository);
        } catch(NotFoundException e) {
            return new ResponseWrapper<>(ResponseHeader.NOT_FOUND_EXCEPTION, null);
        }
        
        User user;
        try {
            user = UserDB.findByToken(userRepository, userToken).toUser(departmentRepository, majorRepository, permissionRepository);    
        } catch(NotFoundException e) {
            return new ResponseWrapper<>(ResponseHeader.EXPIRED_USER_TOKEN_EXCEPTION, null);
        } catch(WrongInDataBaseException e) {
            return new ResponseWrapper<>(ResponseHeader.WRONG_IN_DATABASE_EXCEPTION, null);
        }
        
        if(!user.hasUserPermission(deptCode)) {
            return new ResponseWrapper<>(ResponseHeader.USER_PERMISSION_DENIED_EXCEPTION, null);
        }
        
        List<ItemDB> itemListByThingId = itemRepository.findByThingId(thingId);

        int max = 0;
        for(int i = 0; i < itemListByThingId.size(); i++) {
            ItemDB tmp = itemListByThingId.get(i);
            if(max < tmp.getNum()) {
                max = tmp.getNum();
            }
        }
        ItemDB newItem = new ItemDB(thingId, max+1); 
        
        Optional<ThingDB> thingOptional = thingRepository.findById(thingId);
        if(thingOptional.isPresent()) {
            Item output = itemRepository.save(newItem).toItem(userRepository, eventRepository);
            thing = thingOptional.get().toThing(userRepository, itemRepository, eventRepository);
            
            return new ResponseWrapper<>(ResponseHeader.OK, new Response(univ, dept, thing, output));
        }
        else {
            return new ResponseWrapper<>(ResponseHeader.NOT_FOUND_EXCEPTION, null);
        }
    }
    
    public class Response {
        University university;
        DepartmentResponse department;
        Thing thing;
        Item item;

        public Response(University university, DepartmentResponse department, Thing thing, Item item) {
            this.university = new University(university);
            this.department = new DepartmentResponse(department);
            this.thing = new Thing(thing);
            this.item = new Item(item);
        }

        public University getUniversity() {
            if(university == null) {
                return null;
            }
            return new University(university);
        }

        public DepartmentResponse getDepartment() {
            if(department == null) {
                return null;
            }
            return new DepartmentResponse(department);
        }
        
        public Thing getThing() {
            if(thing == null) {
                return null;
            }
            return new Thing(thing);
        }
        
        public Item getItem() {
            if(item == null) {
                return null;
            }
            return new Item(item);
        }
    } 
    
    public class ListResponse {
        University university;
        DepartmentResponse department;
        Thing thing;
        List<Item> items;

        public ListResponse(University university, DepartmentResponse department, Thing thing, List<Item> items) {
            this.university = university;
            this.department = department;
            this.thing = thing;
            this.items = new ArrayList<>(items);
        }

        public University getUniversity() {
            if(university == null) {
                return null;
            }
            return new University(university);
        }
        
        public DepartmentResponse getDepartment() {
            if(department == null) {
                return null;
            }
            return new DepartmentResponse(department);
        }
        
        public Thing getThing() {
            if(thing == null) {
                return null;
            }
            return new Thing(thing);
        }

        public List<Item> getItems() {
            return new ArrayList<>(items);
        }
    }
    
}