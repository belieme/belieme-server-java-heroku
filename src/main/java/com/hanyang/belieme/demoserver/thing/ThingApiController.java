package com.hanyang.belieme.demoserver.thing;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import com.hanyang.belieme.demoserver.item.*;
import com.hanyang.belieme.demoserver.university.University;
import com.hanyang.belieme.demoserver.university.UniversityRepository;
import com.hanyang.belieme.demoserver.user.User;
import com.hanyang.belieme.demoserver.user.UserDB;
import com.hanyang.belieme.demoserver.user.UserRepository;
import com.hanyang.belieme.demoserver.user.permission.PermissionRepository;
import com.hanyang.belieme.demoserver.event.*;
import com.hanyang.belieme.demoserver.exception.BadRequestException;
import com.hanyang.belieme.demoserver.exception.ForbiddenException;
import com.hanyang.belieme.demoserver.exception.HttpException;
import com.hanyang.belieme.demoserver.exception.InternalServerErrorException;
import com.hanyang.belieme.demoserver.exception.MethodNotAllowedException;
import com.hanyang.belieme.demoserver.exception.NotFoundException;
import com.hanyang.belieme.demoserver.exception.UnauthorizedException;
import com.hanyang.belieme.demoserver.common.*;
import com.hanyang.belieme.demoserver.department.DepartmentDB;
import com.hanyang.belieme.demoserver.department.DepartmentRepository;
import com.hanyang.belieme.demoserver.department.Department;
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
    public ResponseEntity<ListResponse> getAllThings(@RequestHeader("user-token") String userToken, @PathVariable String univCode, @PathVariable String deptCode) throws HttpException {
        if(userToken == null) {
            throw new UnauthorizedException("인증이 진행되지 않았습니다. user-token을 header로 전달해 주시길 바랍니다.");
        }
        
        University univ = University.findByUnivCode(universityRepository, univCode);
        
        Department dept = DepartmentDB.findByUnivCodeAndDeptCode(universityRepository, departmentRepository, univCode, deptCode).toDepartment(majorRepository);
        
        int deptId = dept.getId();
        
        User user = UserDB.findByToken(userRepository, userToken).toUser(departmentRepository, majorRepository, permissionRepository);    
        
        if(!user.hasUserPermission(deptCode)) {
            throw new ForbiddenException("주어진 user-token에 해당하는 user에는 api에 대한 권한이 없습니다.");
        }
            
        Iterable<ThingDB> allThingDBList = thingRepository.findByDepartmentId(deptId);
        ArrayList<Thing> output = new ArrayList<>();
        for (Iterator<ThingDB> it = allThingDBList.iterator(); it.hasNext(); ) {
            Thing tmp = it.next().toThing(userRepository, itemRepository, eventRepository); 
            output.add(tmp);
        }
        return ResponseEntity.ok().body(new ListResponse(univ, dept, output));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ResponseWithItems> getThingById(@RequestHeader("user-token") String userToken, @PathVariable String univCode, @PathVariable String deptCode, @PathVariable int id) throws HttpException {
        if(userToken == null) {
            throw new UnauthorizedException("인증이 진행되지 않았습니다. user-token을 header로 전달해 주시길 바랍니다.");
        }
        
        University univ = University.findByUnivCode(universityRepository, univCode);
        
        Department dept = DepartmentDB.findByUnivCodeAndDeptCode(universityRepository, departmentRepository, univCode, deptCode).toDepartment(majorRepository);
        int deptId = dept.getId();
        
        User user = UserDB.findByToken(userRepository, userToken).toUser(departmentRepository, majorRepository, permissionRepository);    
        
        if(!user.hasUserPermission(deptCode)) {
            throw new ForbiddenException("주어진 user-token에 해당하는 user에는 api에 대한 권한이 없습니다.");
        }
        
        Optional<ThingDB> targetOptional = thingRepository.findById(id);
        if(targetOptional.isPresent()) {
            if(deptId != targetOptional.get().getDepartmentId()) {
                throw new MethodNotAllowedException("물품 id가 " + id + "인 물품은 " + univCode + "와 " + deptCode + "를 학교 코드와 학과 코드를 갖는 학과의 것이 아닙니다."); // TODO Exception 바꿀까?
            }
            ThingWithItems output = targetOptional.get().toThingWithItems(userRepository, itemRepository, eventRepository);
            return ResponseEntity.ok().body(new ResponseWithItems(univ, dept, output));
        }
        throw new NotFoundException(id + "를 id로 갖는 물픔을 찾을 수 없습니다.");
    }

    @PostMapping("")
    public ResponseEntity<ResponseWithItems> createNewThing(@RequestHeader("user-token") String userToken, @PathVariable String univCode, @PathVariable String deptCode, @RequestBody ThingRequestBody requestBody) throws HttpException {
        if(userToken == null) {
            throw new UnauthorizedException("인증이 진행되지 않았습니다. user-token을 header로 전달해 주시길 바랍니다.");
        }
        
        if(requestBody.getName() == null || requestBody.getEmoji() == null || requestBody.getDescription() == null) { //getAmount는 체크 안하는 이유가 amout를 입력 안하면 0으로 자동저장 되어서 item이 0개인 thing이 생성된다.
            throw new BadRequestException("Request body에 정보가 부족합니다.\n필요한 정보 : name(String), emoji(String), description(String), amount(int)(optional)");
        }
        
        University univ = University.findByUnivCode(universityRepository, univCode);
        
        Department dept = DepartmentDB.findByUnivCodeAndDeptCode(universityRepository, departmentRepository, univCode, deptCode).toDepartment(majorRepository);
        int deptId = dept.getId();
        
        User user = UserDB.findByToken(userRepository, userToken).toUser(departmentRepository, majorRepository, permissionRepository);    
        
        if(!user.hasStaffPermission(deptCode)) {
            throw new ForbiddenException("주어진 user-token에 해당하는 user에는 api에 대한 권한이 없습니다.");
        }
        
        ThingDB requestBodyDB = requestBody.toThingDB();
        requestBodyDB.setDepartmentId(deptId);
        
        ThingDB savedThingDB = thingRepository.save(requestBodyDB);
        for(int i = 0; i < requestBody.getAmount(); i++) { // requestBody에 amout값이 주어졌을때 작동 됨
            ItemDB newItem = new ItemDB(savedThingDB.getId(), i + 1);
            itemRepository.save(newItem);
        }
        ThingWithItems savedThing = savedThingDB.toThingWithItems(userRepository, itemRepository, eventRepository);
        
        URI location;
        try {
            location = new URI(Globals.serverUrl + "/univs/" + univCode + "/depts/" + deptCode + "/things/" + requestBody.getId());    
        } catch(URISyntaxException e) {
            e.printStackTrace();
            throw new InternalServerErrorException("안알랴줌");
        }
        
        return ResponseEntity.created(location).body(new ResponseWithItems(univ, dept, savedThing));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ResponseWithItems> updateNameAndEmojiOfThing(@RequestHeader("user-token") String userToken, @PathVariable String univCode, @PathVariable String deptCode, @PathVariable int id, @RequestBody ThingRequestBody requestBody) throws HttpException {
        if(userToken == null) {
            throw new UnauthorizedException("인증이 진행되지 않았습니다. user-token을 header로 전달해 주시길 바랍니다.");
        }
        
        if(requestBody.getName() == null && requestBody.getEmoji() == null && requestBody.getDescription() == null) {
            throw new BadRequestException("Request body에 정보가 부족합니다.\n필요한 정보 : name(String), emoji(String), description(String) 중 하나 이상");
        }
        
        University univ = University.findByUnivCode(universityRepository, univCode);
        
        Department dept = DepartmentDB.findByUnivCodeAndDeptCode(universityRepository, departmentRepository, univCode, deptCode).toDepartment(majorRepository);
        int deptId = dept.getId();
        
        User user = UserDB.findByToken(userRepository, userToken).toUser(departmentRepository, majorRepository, permissionRepository);    
        
        if(!user.hasStaffPermission(deptCode)) {
            throw new ForbiddenException("주어진 user-token에 해당하는 user에는 api에 대한 권한이 없습니다.");
        }
        
        Optional<ThingDB> targetOptional = thingRepository.findById(id);
        if(targetOptional.isPresent()) {
            ThingDB target = targetOptional.get();
            
            if(target.getDepartmentId() != deptId) {
                throw new MethodNotAllowedException("물품 id가 " + id + "인 물품은 " + univCode + "와 " + deptCode + "를 학교 코드와 학과 코드를 갖는 학과의 것이 아닙니다."); // TODO Exception 바꿀까?
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
            return ResponseEntity.ok().body(new ResponseWithItems(univ, dept, output));
        }
        throw new NotFoundException(id + "를 id로 갖는 물품을 찾을 수 없습니다.");
    }
    
    public class Response {
        University university;
        Department department;
        Thing thing;

        public Response(University university, Department department, Thing thing) {
            this.university = new University(university);
            this.department = new Department(department);
            this.thing = new Thing(thing);
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
    }
    
    public class ResponseWithItems {
        University university;
        Department department;
        ThingWithItems thing;

        public ResponseWithItems(University university, Department department, ThingWithItems thing) {
            this.university = new University(university);
            this.department = new Department(department);
            this.thing = new ThingWithItems(thing);
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
        
        public ThingWithItems getThing() {
            if(thing == null) {
                return null;
            }
            return new ThingWithItems(thing);
        }
    }
    
    
    public class ListResponse {
        University university;
        Department department;
        List<Thing> things;

        public ListResponse(University university, Department department, List<Thing> things) {
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
        
        public Department getDepartment() {
            if(department == null) {
                return null;
            }
            return new Department(department);
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