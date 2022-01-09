package com.belieme.server.web.controller;

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
    public ResponseEntity<ListResponse> getAllItems(@RequestHeader("user-token") String userToken, @PathVariable String univCode, @PathVariable String deptCode, @PathVariable String thingCode) throws UnauthorizedException, NotFoundException, InternalServerErrorException, ForbiddenException {
        init(userToken, univCode, deptCode, thingCode);
        
        if(!requester.hasUserPermission(deptCode)) {
            throw new ForbiddenException("주어진 user-token에 해당하는 user에는 api에 대한 권한이 없습니다.");
        }
        
        ThingDto thing = dataAdapter.findThingByUnivCodeAndDeptCodeAndThingCode(univCode, deptCode, thingCode);
        List<ItemDto> items = dataAdapter.findAllItemsByUnivCodeAndDeptCodeAndThingCode(univCode, deptCode, thingCode);
        
        return createGetListResponseEntity(items);
    }

    @GetMapping("/{itemNum}") 
    public ResponseEntity<Response> getItemByThingIdAndNum(@RequestHeader("user-token") String userToken, @PathVariable String univCode, @PathVariable String deptCode, @PathVariable String thingCode, @PathVariable int itemNum) throws UnauthorizedException, NotFoundException, InternalServerErrorException, ForbiddenException {
        init(userToken, univCode, deptCode, thingCode);
        
        if(!requester.hasUserPermission(deptCode)) {
            throw new ForbiddenException("주어진 user-token에 해당하는 user에는 api에 대한 권한이 없습니다.");
        }

        ItemDto item = dataAdapter.findItemByUnivCodeAndDeptCodeAndThingCodeAndItemNum(univCode, deptCode, thingCode, itemNum);
        return createGetResponseEntity(item);
    }

    @PostMapping("")
    public ResponseEntity<Response> createNewItem(@RequestHeader("user-token") String userToken, @PathVariable String univCode, @PathVariable String deptCode, @PathVariable String thingCode) throws UnauthorizedException, NotFoundException, InternalServerErrorException, ForbiddenException, MethodNotAllowedException, ConflictException {
        init(userToken, univCode, deptCode, thingCode);

        if(!requester.hasStaffPermission(deptCode)) {
            throw new ForbiddenException("주어진 user-token에 해당하는 user에는 api에 대한 권한이 없습니다.");
        }

        List<ItemDto> items = dataAdapter.findAllItemsByUnivCodeAndDeptCodeAndThingCode(univCode, deptCode, thingCode);

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
        
        ItemDto output = dataAdapter.saveItem(newItem);
        thing = dataAdapter.findThingByUnivCodeAndDeptCodeAndThingCode(univCode, deptCode, thingCode);
        
        String location = Globals.serverUrl + "/univs/" + univCode + "/depts/" + deptCode + "/things/" + thingCode + "/items/" + max + 1;
        
        return createPostResponseEntity(location, output);
    }

    private UserDto requester;
    private UniversityDto univ;
    private DepartmentDto dept;
    private ThingDto thing;

    private void init(String userToken, String univCode, String deptCode, String thingCode) throws UnauthorizedException, NotFoundException, InternalServerErrorException {
        requester = dataAdapter.findUserByToken(userToken);
        univ = dataAdapter.findUnivByCode(univCode);
        dept = dataAdapter.findDeptByUnivCodeAndDeptCode(univCode, deptCode);
        thing = dataAdapter.findThingByUnivCodeAndDeptCodeAndThingCode(univCode,deptCode,thingCode);
    }

    private URI createUri(String uri) throws InternalServerErrorException {
        URI location;
        try {
            location = new URI(uri);
        } catch(URISyntaxException e) {
            e.printStackTrace();
            throw new InternalServerErrorException("안알랴줌");
        }
        return location;
    }

    private ResponseEntity<Response> createGetResponseEntity(ItemDto itemDto) throws InternalServerErrorException, NotFoundException {
        return ResponseEntity.ok().body(createResponse(itemDto));
    }

    private ResponseEntity<Response> createPostResponseEntity(String location, ItemDto itemDto) throws InternalServerErrorException, NotFoundException {
        URI uri = createUri(location);
        return ResponseEntity.created(uri).body(createResponse(itemDto));
    }

    private ResponseEntity<ListResponse> createGetListResponseEntity(List<ItemDto> itemDtoList) throws InternalServerErrorException, NotFoundException {
        return ResponseEntity.ok().body(createListResponse(itemDtoList));
    }
    
    private Response createResponse(ItemDto itemDto) throws InternalServerErrorException, NotFoundException {
        UniversityJsonBody univJsonBody = jsonBodyProjector.toUniversityJsonBody(univ);
        DepartmentJsonBody deptJsonBody = jsonBodyProjector.toDepartmentJsonBody(dept);
        ThingJsonBody thingJsonBody = jsonBodyProjector.toThingJsonBody(thing);
        ItemJsonBody itemJsonBody = jsonBodyProjector.toItemJsonBody(itemDto);
        return new Response(univJsonBody, deptJsonBody, thingJsonBody, itemJsonBody);
    }
    
    private ListResponse createListResponse(List<ItemDto> itemDtoList) throws InternalServerErrorException, NotFoundException {
        UniversityJsonBody univJsonBody = jsonBodyProjector.toUniversityJsonBody(univ);
        DepartmentJsonBody deptJsonBody = jsonBodyProjector.toDepartmentJsonBody(dept);
        ThingJsonBody thingJsonBody = jsonBodyProjector.toThingJsonBody(thing);
        List<ItemJsonBody> itemList = new ArrayList<>();
        for(int i = 0; i < itemDtoList.size(); i++) {
            itemList.add(jsonBodyProjector.toItemJsonBody(itemDtoList.get(i)));
        }
        return new ListResponse(univJsonBody, deptJsonBody, thingJsonBody, itemList);
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