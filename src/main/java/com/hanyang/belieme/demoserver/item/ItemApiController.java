package com.hanyang.belieme.demoserver.item;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import com.hanyang.belieme.demoserver.thing.*;
import com.hanyang.belieme.demoserver.university.University;
import com.hanyang.belieme.demoserver.university.UniversityRepository;
import com.hanyang.belieme.demoserver.user.User;
import com.hanyang.belieme.demoserver.user.UserDB;
import com.hanyang.belieme.demoserver.user.UserRepository;
import com.hanyang.belieme.demoserver.user.permission.PermissionRepository;
import com.hanyang.belieme.demoserver.event.*;
import com.hanyang.belieme.demoserver.exception.ForbiddenException;
import com.hanyang.belieme.demoserver.exception.HttpException;
import com.hanyang.belieme.demoserver.exception.InternalServerErrorException;
import com.hanyang.belieme.demoserver.exception.UnauthorizedException;
import com.hanyang.belieme.demoserver.common.*;
import com.hanyang.belieme.demoserver.department.DepartmentDB;
import com.hanyang.belieme.demoserver.department.DepartmentRepository;
import com.hanyang.belieme.demoserver.department.Department;
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
    public ResponseEntity<ListResponse> getAllItems(@RequestHeader("user-token") String userToken, @PathVariable String univCode, @PathVariable String deptCode, @PathVariable int thingId) throws HttpException {
        if(userToken == null) {
            throw new UnauthorizedException("인증이 진행되지 않았습니다. user-token을 header로 전달해 주시길 바랍니다.");
        }
        
        User user = UserDB.findByToken(userRepository, userToken).toUser(departmentRepository, majorRepository, permissionRepository);    
        if(!user.hasUserPermission(deptCode)) {
            throw new ForbiddenException("주어진 user-token에 해당하는 user에는 api에 대한 권한이 없습니다.");
        }
        
        University univ = University.findByUnivCode(universityRepository, univCode);
        Department dept = DepartmentDB.findByUnivCodeAndDeptCode(universityRepository, departmentRepository, univCode, deptCode).toDepartment(majorRepository);
        int deptId = dept.getId();
        Thing thing = ThingDB.findByThingIdAndDeptId(thingRepository, thingId, deptId).toThing(userRepository, itemRepository, eventRepository);
        
        
        List<Item> output = new ArrayList<>();       
        List<ItemDB> itemListByThingId = itemRepository.findByThingId(thingId);
        for(int i = 0; i < itemListByThingId.size(); i++) {
            output.add(itemListByThingId.get(i).toItem(userRepository, eventRepository));    
            
        }
        return ResponseEntity.ok().body(new ListResponse(univ, dept, thing, output));
    }

    @GetMapping("/{itemNum}") 
    public ResponseEntity<Response> getItemByThingIdAndNum(@RequestHeader("user-token") String userToken, @PathVariable String univCode, @PathVariable String deptCode, @PathVariable int thingId, @PathVariable int itemNum) throws HttpException {
                if(userToken == null) {
            throw new UnauthorizedException("인증이 진행되지 않았습니다. user-token을 header로 전달해 주시길 바랍니다.");
        }
        
        User user = UserDB.findByToken(userRepository, userToken).toUser(departmentRepository, majorRepository, permissionRepository);    
        if(!user.hasUserPermission(deptCode)) {
            throw new ForbiddenException("주어진 user-token에 해당하는 user에는 api에 대한 권한이 없습니다.");
        }
        
        University univ = University.findByUnivCode(universityRepository, univCode);
        Department dept = DepartmentDB.findByUnivCodeAndDeptCode(universityRepository, departmentRepository, univCode, deptCode).toDepartment(majorRepository);
        int deptId = dept.getId();
        Thing thing = ThingDB.findByThingIdAndDeptId(thingRepository, thingId, deptId).toThing(userRepository, itemRepository, eventRepository);
        
        Item output = ItemDB.findByThingIdAndNum(itemRepository, thingId, itemNum).toItem(userRepository, eventRepository);    
        return ResponseEntity.ok().body(new Response(univ, dept, thing, output));
    }

    @PostMapping("")
    public ResponseEntity<Response> createNewItem(@RequestHeader("user-token") String userToken, @PathVariable String univCode, @PathVariable String deptCode, @PathVariable int thingId) throws HttpException {
        if(userToken == null) {
            throw new UnauthorizedException("인증이 진행되지 않았습니다. user-token을 header로 전달해 주시길 바랍니다.");
        }
        
        User user = UserDB.findByToken(userRepository, userToken).toUser(departmentRepository, majorRepository, permissionRepository);    
        if(!user.hasStaffPermission(deptCode)) {
            throw new ForbiddenException("주어진 user-token에 해당하는 user에는 api에 대한 권한이 없습니다.");
        }
        
        University univ = University.findByUnivCode(universityRepository, univCode);
        Department dept = DepartmentDB.findByUnivCodeAndDeptCode(universityRepository, departmentRepository, univCode, deptCode).toDepartment(majorRepository);
        int deptId = dept.getId();
        Thing thing = ThingDB.findByThingIdAndDeptId(thingRepository, thingId, deptId).toThing(userRepository, itemRepository, eventRepository);
        
        List<ItemDB> itemListByThingId = itemRepository.findByThingId(thingId);

        int max = 0;
        for(int i = 0; i < itemListByThingId.size(); i++) {
            ItemDB tmp = itemListByThingId.get(i);
            if(max < tmp.getNum()) {
                max = tmp.getNum();
            }
        }
        ItemDB newItem = new ItemDB(thingId, max+1); 
        
        Item output = itemRepository.save(newItem).toItem(userRepository, eventRepository);
        
        URI location;
        try {
            location = new URI(Globals.serverUrl + "/univs/" + univCode + "/depts/" + deptCode + "/things/" + thingId + "/items/" + max + 1);    
        } catch(URISyntaxException e) {
            e.printStackTrace();
            throw new InternalServerErrorException("안알랴줌");
        }
        
        return ResponseEntity.created(location).body(new Response(univ, dept, thing, output));
    }
    
    public class Response {
        University university;
        Department department;
        Thing thing;
        Item item;

        public Response(University university, Department department, Thing thing, Item item) {
            this.university = new University(university);
            this.department = new Department(department);
            this.thing = new Thing(thing);
            this.item = new Item(item);
        }

        public University getUniversity() {
            if(university == null) {
                return null;
            }
            return new University(university);
        }

        public Department getDepartment() {
            if(department == null) {
                return null;
            }
            return new Department(department);
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
        Department department;
        Thing thing;
        List<Item> items;

        public ListResponse(University university, Department department, Thing thing, List<Item> items) {
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
        
        public Department getDepartment() {
            if(department == null) {
                return null;
            }
            return new Department(department);
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