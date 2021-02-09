package com.belieme.server.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import com.belieme.server.domain.university.*;
import com.belieme.server.domain.department.*;
import com.belieme.server.domain.user.*;
import com.belieme.server.domain.thing.*;
import com.belieme.server.domain.item.*;
import com.belieme.server.domain.exception.ServerDomainException;

import com.belieme.server.data.university.*;
import com.belieme.server.data.department.*;
import com.belieme.server.data.major.*;
import com.belieme.server.data.user.*;
import com.belieme.server.data.permission.*;
import com.belieme.server.data.thing.*;
import com.belieme.server.data.item.*;
import com.belieme.server.data.event.*;

import com.belieme.server.web.common.*;
import com.belieme.server.web.exception.*;
import com.belieme.server.web.jsonbody.*;

@RestController
@RequestMapping(path="/univs/{univCode}/depts/{deptCode}/things/{thingCode}/items")
public class ItemApiController extends ApiController {
    @Autowired
    public ItemApiController(UniversityRepository univRepo, DepartmentRepository deptRepo, MajorRepository majorRepo, UserRepository userRepo, PermissionRepository permissionRepo, ThingRepository thingRepo, ItemRepository itemRepo, EventRepository eventRepo) {
        super(univRepo, deptRepo, majorRepo, userRepo, permissionRepo, thingRepo, itemRepo, eventRepo);
    }
    @GetMapping("")
    public ResponseEntity<ListResponse> getAllItems(@RequestHeader("user-token") String userToken, @PathVariable String univCode, @PathVariable String deptCode, @PathVariable String thingCode) throws HttpException, ServerDomainException {
        if(userToken == null) {
            throw new UnauthorizedException("인증이 진행되지 않았습니다. user-token을 header로 전달해 주시길 바랍니다.");
        }
        
        UniversityDto univ = univDao.findByCode(univCode);
        DepartmentDto dept = deptDao.findByUnivCodeAndDeptCode(univCode, deptCode);
        
        UserDto user = userDao.findByToken(userToken); 
        
        if(!user.hasUserPermission(deptCode)) {
            throw new ForbiddenException("주어진 user-token에 해당하는 user에는 api에 대한 권한이 없습니다.");
        }
        
        ThingDto thing = thingDao.findByUnivCodeAndDeptCodeAndCode(univCode, deptCode, thingCode);
        List<ItemDto> items = itemDao.findByUnivCodeAndDeptCodeAndThingCode(univCode, deptCode, thingCode);
        
        return ResponseEntity.ok().body(createListResponse(univ, dept, thing, items));
    }

    @GetMapping("/{itemNum}") 
    public ResponseEntity<Response> getItemByThingIdAndNum(@RequestHeader("user-token") String userToken, @PathVariable String univCode, @PathVariable String deptCode, @PathVariable String thingCode, @PathVariable int itemNum) throws HttpException, ServerDomainException {
        if(userToken == null) {
            throw new UnauthorizedException("인증이 진행되지 않았습니다. user-token을 header로 전달해 주시길 바랍니다.");
        }
        
        UniversityDto univ = univDao.findByCode(univCode);
        DepartmentDto dept = deptDao.findByUnivCodeAndDeptCode(univCode, deptCode);
        
        UserDto user = userDao.findByToken(userToken);    
        if(!user.hasUserPermission(deptCode)) {
            throw new ForbiddenException("주어진 user-token에 해당하는 user에는 api에 대한 권한이 없습니다.");
        }
        
        ThingDto thing = thingDao.findByUnivCodeAndDeptCodeAndCode(univCode, deptCode, thingCode);
        ItemDto item = itemDao.findByUnivCodeAndDeptCodeAndThingCodeAndNum(univCode, deptCode, thingCode, itemNum);
        return ResponseEntity.ok().body(createResponse(univ, dept, thing, item));
    }

    @PostMapping("")
    public ResponseEntity<Response> createNewItem(@RequestHeader("user-token") String userToken, @PathVariable String univCode, @PathVariable String deptCode, @PathVariable String thingCode) throws HttpException, ServerDomainException {
        if(userToken == null) {
            throw new UnauthorizedException("인증이 진행되지 않았습니다. user-token을 header로 전달해 주시길 바랍니다.");
        }
        
        UniversityDto univ = univDao.findByCode(univCode);
        DepartmentDto dept = deptDao.findByUnivCodeAndDeptCode(univCode, deptCode);
        
        UserDto user = userDao.findByToken(userToken);
        
        if(!user.hasStaffPermission(deptCode)) {
            throw new ForbiddenException("주어진 user-token에 해당하는 user에는 api에 대한 권한이 없습니다.");
        }

        ThingDto thing = thingDao.findByUnivCodeAndDeptCodeAndCode(univCode, deptCode, thingCode);
        List<ItemDto> items = itemDao.findByUnivCodeAndDeptCodeAndThingCode(univCode, deptCode, thingCode);

        int max = 0;
        for(int i = 0; i < items.size(); i++) {
            ItemDto tmp = items.get(i);
            if(max < tmp.getNum()) {
                max = tmp.getNum();
            }
        }
        
        ItemDto newItem = new ItemDto();
        newItem.setUnivCode(univCode);
        newItem.setDeptCode(deptCode);
        newItem.setThingCode(thingCode);
        newItem.setNum(max+1);
        newItem.setLastEventNum(0); // 초기화?
        
        ItemDto output = itemDao.save(newItem);
        
        URI location;
        try {
            location = new URI(Globals.serverUrl + "/univs/" + univCode + "/depts/" + deptCode + "/things/" + thingCode + "/items/" + max + 1);    
        } catch(URISyntaxException e) {
            e.printStackTrace();
            throw new InternalServerErrorException("안알랴줌");
        }
        
        return ResponseEntity.created(location).body(createResponse(univ, dept, thing, output));
    }
    
    private Response createResponse(UniversityDto univDto, DepartmentDto deptDto, ThingDto thingDto, ItemDto itemDto) throws ServerDomainException {
        UniversityJsonBody univ = jsonBodyProjector.toUniversityJsonBody(univDto);
        DepartmentJsonBody dept = jsonBodyProjector.toDepartmentJsonBody(deptDto);
        ThingJsonBody thing = jsonBodyProjector.toThingJsonBody(thingDto);
        ItemJsonBody item = jsonBodyProjector.toItemJsonBody(itemDto);
        return new Response(univ, dept, thing, item);
    }
    
    private ListResponse createListResponse(UniversityDto univDto, DepartmentDto deptDto, ThingDto thingDto, List<ItemDto> itemDtoList) throws ServerDomainException {
        UniversityJsonBody univ = jsonBodyProjector.toUniversityJsonBody(univDto);
        DepartmentJsonBody dept = jsonBodyProjector.toDepartmentJsonBody(deptDto);
        ThingJsonBody thing = jsonBodyProjector.toThingJsonBody(thingDto);
        List<ItemJsonBody> itemList = new ArrayList<>();
        for(int i = 0; i < itemDtoList.size(); i++) {
            itemList.add(jsonBodyProjector.toItemJsonBody(itemDtoList.get(i)));
        }
        return new ListResponse(univ, dept, thing, itemList);
    }
    
    public class Response {
        UniversityJsonBody university;
        DepartmentJsonBody department;
        ThingJsonBody thing;
        ItemJsonBody item;

        public Response(UniversityJsonBody university, DepartmentJsonBody department, ThingJsonBody thing, ItemJsonBody item) {
            this.university = university;
            this.department = department;
            this.thing = thing;
            this.item = item;
        }

        public UniversityJsonBody getUniversity() {
            return university;
        }

        public DepartmentJsonBody getDepartment() {
            return department;
        }
        
        public ThingJsonBody getThing() {
            return thing;
        }
        
        public ItemJsonBody getItem() {
            return item;
        }
    } 
    
    public class ListResponse {
        UniversityJsonBody university;
        DepartmentJsonBody department;
        ThingJsonBody thing;
        List<ItemJsonBody> items;

        public ListResponse(UniversityJsonBody university, DepartmentJsonBody department, ThingJsonBody thing, List<ItemJsonBody> items) {
            this.university = university;
            this.department = department;
            this.thing = thing;
            this.items = items;
        }

        public UniversityJsonBody getUniversity() {
            return university;
        }

        public DepartmentJsonBody getDepartment() {
            return department;
        }
        
        public ThingJsonBody getThing() {
            return thing;
        }

        public List<ItemJsonBody> getItems() {
            return items;
        }
    }
    
}