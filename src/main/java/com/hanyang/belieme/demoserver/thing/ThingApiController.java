package com.hanyang.belieme.demoserver.thing;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import com.hanyang.belieme.demoserver.item.*;
import com.hanyang.belieme.demoserver.university.University;
import com.hanyang.belieme.demoserver.university.UniversityRepository;
import com.hanyang.belieme.demoserver.user.User;
import com.hanyang.belieme.demoserver.user.UserRepository;
import com.hanyang.belieme.demoserver.user.permission.PermissionRepository;
import com.hanyang.belieme.demoserver.event.*;
import com.hanyang.belieme.demoserver.exception.NotFoundException;
import com.hanyang.belieme.demoserver.exception.WrongInDataBaseException;
import com.hanyang.belieme.demoserver.common.*;
import com.hanyang.belieme.demoserver.department.Department;
import com.hanyang.belieme.demoserver.department.DepartmentRepository;
import com.hanyang.belieme.demoserver.department.DepartmentResponse;
import com.hanyang.belieme.demoserver.department.major.MajorRepository;

@RestController
@RequestMapping(path="/univs/{univCode}/depts/{deptCode}/things")
public class ThingApiController {
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
    private ThingRepository thingRepository;
    
    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private EventRepository eventRepository;

    @GetMapping("")
    public ResponseWrapper<ListResponse> getAllThings(@RequestHeader("user-token") String userToken, @PathVariable String univCode, @PathVariable String deptCode) {
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
            dept = Department.findByUnivCodeAndDeptCode(universityRepository, departmentRepository, univCode, deptCode).toDepartmentResponse(majorRepository);
        } catch(NotFoundException e) {
            return new ResponseWrapper<>(ResponseHeader.NOT_FOUND_EXCEPTION, null);
        } catch(WrongInDataBaseException e) {
            return new ResponseWrapper<>(ResponseHeader.WRONG_IN_DATABASE_EXCEPTION, null);
        }
        int deptId = dept.getId();
        
        User user;
        try {
            user = User.findByToken(userRepository, userToken).toUser(universityRepository, departmentRepository, majorRepository, permissionRepository);    
        } catch(NotFoundException e) {
            return new ResponseWrapper<>(ResponseHeader.EXPIRED_USER_TOKEN_EXCEPTION, null);
        } catch(WrongInDataBaseException e) {
            return new ResponseWrapper<>(ResponseHeader.WRONG_IN_DATABASE_EXCEPTION, null);
        }
        
        if(!user.hasUserPermission(deptCode)) {
            return new ResponseWrapper<>(ResponseHeader.USER_PERMISSION_DENIED_EXCEPTION, null);
        }
            
        Iterable<ThingDB> allThingDBList = thingRepository.findByDepartmentId(deptId);
        ArrayList<Thing> output = new ArrayList<>();
        for (Iterator<ThingDB> it = allThingDBList.iterator(); it.hasNext(); ) {
            Thing tmp = it.next().toThing(userRepository, itemRepository, eventRepository); 
            output.add(tmp);
        }
        return new ResponseWrapper<>(ResponseHeader.OK, new ListResponse(univ, dept, output));
    }

    @GetMapping("/{id}")
    public ResponseWrapper<ResponseWithItems> getThingById(@RequestHeader("user-token") String userToken, @PathVariable String univCode, @PathVariable String deptCode, @PathVariable int id) {
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
            dept = Department.findByUnivCodeAndDeptCode(universityRepository, departmentRepository, univCode, deptCode).toDepartmentResponse(majorRepository);
        } catch(NotFoundException e) {
            return new ResponseWrapper<>(ResponseHeader.NOT_FOUND_EXCEPTION, null);
        } catch(WrongInDataBaseException e) {
            return new ResponseWrapper<>(ResponseHeader.WRONG_IN_DATABASE_EXCEPTION, null);
        }
        int deptId = dept.getId();
        
        User user;
        try {
            user = User.findByToken(userRepository, userToken).toUser(universityRepository, departmentRepository, majorRepository, permissionRepository);    
        } catch(NotFoundException e) {
            return new ResponseWrapper<>(ResponseHeader.EXPIRED_USER_TOKEN_EXCEPTION, null);
        } catch(WrongInDataBaseException e) {
            return new ResponseWrapper<>(ResponseHeader.WRONG_IN_DATABASE_EXCEPTION, null);
        }
        
        if(!user.hasUserPermission(deptCode)) {
            return new ResponseWrapper<>(ResponseHeader.USER_PERMISSION_DENIED_EXCEPTION, null);
        }
        
        Optional<ThingDB> targetOptional = thingRepository.findById(id);
        if(targetOptional.isPresent()) {
            if(deptId != targetOptional.get().getDepartmentId()) {
                return new ResponseWrapper<>(ResponseHeader.NOT_FOUND_EXCEPTION, null); // TODO Exception 바꿀까?
            }
            ThingWithItems output = targetOptional.get().toThingWithItems(userRepository, itemRepository, eventRepository);
            return new ResponseWrapper<>(ResponseHeader.OK, new ResponseWithItems(univ, dept, output));
        }
        return new ResponseWrapper<>(ResponseHeader.NOT_FOUND_EXCEPTION, null);
    }

    @PostMapping("")
    public ResponseWrapper<ResponseWithItems> createNewThing(@RequestHeader("user-token") String userToken, @PathVariable String univCode, @PathVariable String deptCode, @RequestBody ThingRequestBody requestBody) {
        if(userToken == null) {
            return new ResponseWrapper<>(ResponseHeader.LACK_OF_REQUEST_HEADER_EXCEPTION, null);
        }
        
        if(requestBody.getName() == null || requestBody.getEmoji() == null || requestBody.getDescription() == null) { //getAmount는 체크 안하는 이유가 amout를 입력 안하면 0으로 자동저장 되어서 item이 0개인 thing이 생성된다.
            return new ResponseWrapper<>(ResponseHeader.LACK_OF_REQUEST_BODY_EXCEPTION, null);
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
            dept = Department.findByUnivCodeAndDeptCode(universityRepository, departmentRepository, univCode, deptCode).toDepartmentResponse(majorRepository);
        } catch(NotFoundException e) {
            return new ResponseWrapper<>(ResponseHeader.NOT_FOUND_EXCEPTION, null);
        } catch(WrongInDataBaseException e) {
            return new ResponseWrapper<>(ResponseHeader.WRONG_IN_DATABASE_EXCEPTION, null);
        }
        int deptId = dept.getId();
        
        User user;
        try {
            user = User.findByToken(userRepository, userToken).toUser(universityRepository, departmentRepository, majorRepository, permissionRepository);    
        } catch(NotFoundException e) {
            return new ResponseWrapper<>(ResponseHeader.EXPIRED_USER_TOKEN_EXCEPTION, null);
        } catch(WrongInDataBaseException e) {
            return new ResponseWrapper<>(ResponseHeader.WRONG_IN_DATABASE_EXCEPTION, null);
        }
        
        if(!user.hasUserPermission(deptCode)) {
            return new ResponseWrapper<>(ResponseHeader.USER_PERMISSION_DENIED_EXCEPTION, null);
        }
        
        ThingDB requestBodyDB = requestBody.toThingDB();
        requestBodyDB.setDepartmentId(deptId);
        
        ThingDB savedThingDB = thingRepository.save(requestBodyDB);
        for(int i = 0; i < requestBody.getAmount(); i++) { // requestBody에 amout값이 주어졌을때 작동 됨
            ItemDB newItem = new ItemDB(savedThingDB.getId(), i + 1);
            itemRepository.save(newItem);
        }
        ThingWithItems savedThing = savedThingDB.toThingWithItems(userRepository, itemRepository, eventRepository); 
        return new ResponseWrapper<>(ResponseHeader.OK, new ResponseWithItems(univ, dept, savedThing));
    }

    @PatchMapping("/{id}")
    public ResponseWrapper<ResponseWithItems> updateNameAndEmojiOfThing(@RequestHeader("user-token") String userToken, @PathVariable String univCode, @PathVariable String deptCode, @PathVariable int id, @RequestBody ThingRequestBody requestBody){
        if(userToken == null) {
            return new ResponseWrapper<>(ResponseHeader.LACK_OF_REQUEST_HEADER_EXCEPTION, null);
        }
        
        if(requestBody.getName() == null && requestBody.getEmoji() == null && requestBody.getDescription() == null) {
            return new ResponseWrapper<>(ResponseHeader.LACK_OF_REQUEST_BODY_EXCEPTION, null);
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
            dept = Department.findByUnivCodeAndDeptCode(universityRepository, departmentRepository, univCode, deptCode).toDepartmentResponse(majorRepository);
        } catch(NotFoundException e) {
            return new ResponseWrapper<>(ResponseHeader.NOT_FOUND_EXCEPTION, null);
        } catch(WrongInDataBaseException e) {
            return new ResponseWrapper<>(ResponseHeader.WRONG_IN_DATABASE_EXCEPTION, null);
        }
        int deptId = dept.getId();
        
        User user;
        try {
            user = User.findByToken(userRepository, userToken).toUser(universityRepository, departmentRepository, majorRepository, permissionRepository);    
        } catch(NotFoundException e) {
            return new ResponseWrapper<>(ResponseHeader.EXPIRED_USER_TOKEN_EXCEPTION, null);
        } catch(WrongInDataBaseException e) {
            return new ResponseWrapper<>(ResponseHeader.WRONG_IN_DATABASE_EXCEPTION, null);
        }
        
        if(!user.hasUserPermission(deptCode)) {
            return new ResponseWrapper<>(ResponseHeader.USER_PERMISSION_DENIED_EXCEPTION, null);
        }
        
        Optional<ThingDB> targetOptional = thingRepository.findById(id);
        if(targetOptional.isPresent()) {
            ThingDB target = targetOptional.get();
            
            if(target.getDepartmentId() != deptId) {
                return new ResponseWrapper<>(ResponseHeader.NOT_FOUND_EXCEPTION, null); // TODO Exception 바꿀까?
            }
            
            ThingDB requestBodyDB = requestBody.toThingDB();
            if(requestBodyDB.getName() != null) {
                target.setName(requestBodyDB.getName());    
            }
            if(requestBodyDB.getEmojiByte() != 0) {
                target.setEmojiByte(requestBodyDB.getEmojiByte());    
            }
            if(requestBodyDB.getDescription() != null) {
                target.setDescription(requestBodyDB.getDescription());
            }
            ThingWithItems output = thingRepository.save(target).toThingWithItems(userRepository, itemRepository, eventRepository);
            return new ResponseWrapper<>(ResponseHeader.OK, new ResponseWithItems(univ, dept, output));
        }
        return new ResponseWrapper<>(ResponseHeader.NOT_FOUND_EXCEPTION,null);
    }
    
    public class Response {
        University university;
        DepartmentResponse department;
        Thing thing;

        public Response(University university, DepartmentResponse department, Thing thing) {
            this.university = new University(university);
            this.department = new DepartmentResponse(department);
            this.thing = new Thing(thing);
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
    }
    
    public class ResponseWithItems {
        University university;
        DepartmentResponse department;
        ThingWithItems thing;

        public ResponseWithItems(University university, DepartmentResponse department, ThingWithItems thing) {
            this.university = new University(university);
            this.department = new DepartmentResponse(department);
            this.thing = new ThingWithItems(thing);
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
        
        public ThingWithItems getThing() {
            if(thing == null) {
                return null;
            }
            return new ThingWithItems(thing);
        }
    }
    
    
    public class ListResponse {
        University university;
        DepartmentResponse department;
        List<Thing> things;

        public ListResponse(University university, DepartmentResponse department, List<Thing> things) {
            this.university = university;
            this.department = department;
            this.things = new ArrayList<>(things);
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

        public List<Thing> getThings() {
            return new ArrayList<>(things);
        }
    }

    // deactivate Thing을 만들긴 해야할 거 같지만 생각좀 해봐야 할 듯
    // @PutMapping("/deactivate/{id}/")
    // public ResponseWrapper<Void> deactivateThing(@PathVariable int id) {
    //     if(itemTypeRepository.findById(id).isPresent()) {
    //         List<Item> itemList = itemRepository.findByTypeId(id);
    //         for (int i = 0; i < itemList.size(); i++) {
    //             itemList.get(i).deactivate();
    //             itemRepository.save(itemList.get(i));
    //         }
    //         return new ResponseWrapper<>(ResponseHeader.OK, null);
    //     }
    //     else {
    //         return new ResponseWrapper<>(ResponseHeader.NOT_FOUND_EXCEPTION, null);
    //     }
    // }
}