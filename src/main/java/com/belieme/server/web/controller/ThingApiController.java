package com.belieme.server.web.controller;

import com.belieme.server.domain.department.DepartmentDto;
import com.belieme.server.domain.event.EventDto;
import com.belieme.server.domain.university.UniversityDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import com.belieme.server.domain.item.ItemDto;
import com.belieme.server.domain.thing.ThingDto;
import com.belieme.server.domain.user.UserDto;

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


// TODO ThingCode는 저장할 때는 대 소문자 구분되어 저장되지만 비교시에는 대 소문자 구분 x => 중복 체크도 대 소문자 구분
//      Item, Event ApiController에도 다 적용시키기 ...(4)

@RestController
@RequestMapping(path="/univs/{univCode}/depts/{deptCode}/things")
public class ThingApiController extends ApiController {
    @Autowired
    public ThingApiController(UniversityRepository univRepo, DepartmentRepository deptRepo, MajorRepository majorRepo, UserRepository userRepo, PermissionRepository permissionRepo, ThingRepository thingRepo, ItemRepository itemRepo, EventRepository eventRepo) {
        super(univRepo, deptRepo, majorRepo, userRepo, permissionRepo, thingRepo, itemRepo, eventRepo);
    }

    @GetMapping("")
    public ResponseEntity<ListResponse> getAllThings(@RequestHeader("user-token") String userToken, @PathVariable String univCode, @PathVariable String deptCode) throws UnauthorizedException, InternalServerErrorException, NotFoundException, ForbiddenException {
        init(userToken, univCode, deptCode);

        if(!requester.hasUserPermission(deptCode)) {
            throw new ForbiddenException("주어진 user-token에 해당하는 user에는 api에 대한 권한이 없습니다.");
        }
        return createGetListResponseEntity(dataAdapter.findAllThingsByUnivCodeAndDeptCode(univCode, deptCode));
    }

    @GetMapping("/{thingCode}")
    public ResponseEntity<ResponseWithItems> getThingById(@RequestHeader("user-token") String userToken, @PathVariable String univCode, @PathVariable String deptCode, @PathVariable String thingCode) throws UnauthorizedException, InternalServerErrorException, NotFoundException, ForbiddenException {
        init(userToken, univCode, deptCode);
        
        if(!requester.hasUserPermission(deptCode)) {
        	throw new ForbiddenException("주어진 user-token에 해당하는 user에는 api에 대한 권한이 없습니다.");
        }
        return createGetResponseWithItemsEntity(dataAdapter.findThingByUnivCodeAndDeptCodeAndThingCode(univCode, deptCode, thingCode));
    }

    @PostMapping("")
    public ResponseEntity<ResponseWithItems> createNewThing(@RequestHeader("user-token") String userToken, @PathVariable String univCode, @PathVariable String deptCode, @RequestBody ThingInfoJsonBody requestBody) throws UnauthorizedException, BadRequestException, NotFoundException, InternalServerErrorException, ForbiddenException, MethodNotAllowedException, ConflictException {
        init(userToken, univCode, deptCode);

        if(!requester.hasStaffPermission(deptCode)) {
            throw new ForbiddenException("주어진 user-token에 해당하는 user에는 api에 대한 권한이 없습니다.");
        }
        
        if(requestBody.getCode() == null || requestBody.getName() == null || requestBody.getEmoji() == null) {
        	throw new BadRequestException("Request body에 정보가 부족합니다.\n필요한 정보 : name(String), emoji(String), description(String)(optional), amount(int)(optional)");
        }
        
        if(requestBody.getAmount() != null && requestBody.getAmount() < 0) {
            throw new BadRequestException("amount는 음수가 될 수 없습니다.");
        }

        ThingDto newThing = new ThingDto();
        newThing.setCode(requestBody.getCode());
        newThing.setName(requestBody.getName());
        newThing.setEmoji(requestBody.getEmoji());
        newThing.setDescription(requestBody.getDescription());
        
        newThing.setUnivCode(univCode);
        newThing.setDeptCode(deptCode);
        
        ThingDto savedThing = dataAdapter.saveThing(newThing);
        
        if(requestBody.getAmount() != null) {
            for(int i = 0; i < requestBody.getAmount(); i++) {
                ItemDto newItem = new ItemDto();
                newItem.setUnivCode(univCode);
                newItem.setDeptCode(deptCode);
                newItem.setThingCode(requestBody.getCode());
                newItem.setNum(i+1);
                newItem.setLastEventNum(0);
                dataAdapter.saveItem(newItem);
            }
        }

        String location = Globals.serverUrl + "/univs/" + univCode + "/depts/" + deptCode + "/things/" + requestBody.getCode();
        return createPostResponseWithItemsEntity(location, savedThing);
    }

    @PatchMapping("/{thingCode}")
    public ResponseEntity<ResponseWithItems> updateNameAndEmojiOfThing(@RequestHeader("user-token") String userToken, @PathVariable String univCode, @PathVariable String deptCode, @PathVariable String thingCode, @RequestBody ThingJsonBody requestBody) throws UnauthorizedException, BadRequestException, NotFoundException, InternalServerErrorException, ForbiddenException, MethodNotAllowedException, ConflictException {
        init(userToken, univCode, deptCode);

        if(!requester.hasStaffPermission(deptCode)) {
            throw new ForbiddenException("주어진 user-token에 해당하는 user에는 api에 대한 권한이 없습니다.");
        }

        if(requestBody.getCode() == null && requestBody.getName() == null && requestBody.getEmoji() == null && requestBody.getDescription() == null) {
            throw new BadRequestException("Request body에 정보가 부족합니다.\n필요한 정보 : name(String), emoji(String), description(String) 중 하나 이상");
        }
        
        ThingDto target = dataAdapter.findThingByUnivCodeAndDeptCodeAndThingCode(univCode, deptCode, thingCode);
        
        if(requestBody.getCode() != null) {
            target.setCode(requestBody.getCode());    
        }
        if(requestBody.getName() != null) {
            target.setName(requestBody.getName());    
        }
        if(requestBody.getEmoji() != null) {
            target.setEmoji(requestBody.getEmoji());    
        }
        if(requestBody.getDescription() != null) {
            target.setDescription(requestBody.getDescription());
        }
        
        ThingDto newAndSavedThing = dataAdapter.updateThing(univCode, deptCode, thingCode, target);
        return createGetResponseWithItemsEntity(newAndSavedThing);
    }

    private UserDto requester;
    private UniversityDto univ;
    private DepartmentDto dept;

    private void init(String userToken, String univCode, String deptCode) throws UnauthorizedException, NotFoundException, InternalServerErrorException {
        requester = dataAdapter.findUserByToken(userToken);
        univ = dataAdapter.findUnivByCode(univCode);
        dept = dataAdapter.findDeptByUnivCodeAndDeptCode(univCode, deptCode);
    }

    private ResponseEntity<Response> createGetResponseEntity(ThingDto output) throws InternalServerErrorException, NotFoundException {
        return ResponseEntity.ok().body(createResponse(output));
    }

    private ResponseEntity<ResponseWithItems> createGetResponseWithItemsEntity(ThingDto output) throws InternalServerErrorException, NotFoundException {
        return ResponseEntity.ok().body(createResponseWithItems(output));
    }

    private ResponseEntity<Response> createPostResponseEntity(String location, ThingDto output) throws InternalServerErrorException, NotFoundException {
        URI uri = createUri(location);
        return ResponseEntity.created(uri).body(createResponse(output));
    }

    private ResponseEntity<ResponseWithItems> createPostResponseWithItemsEntity(String location, ThingDto output) throws InternalServerErrorException, NotFoundException {
        URI uri = createUri(location);
        return ResponseEntity.created(uri).body(createResponseWithItems(output));
    }

    private ResponseEntity<ListResponse> createGetListResponseEntity(List<ThingDto> output) throws InternalServerErrorException, NotFoundException {
        return ResponseEntity.ok().body(createListResponse(output));
    }

    public Response createResponse(ThingDto thingDto) throws InternalServerErrorException, NotFoundException {
        UniversityJsonBody univJsonBody = jsonBodyProjector.toUniversityJsonBody(univ);
        DepartmentJsonBody deptJsonBody = jsonBodyProjector.toDepartmentJsonBody(dept);
        ThingJsonBody thingJsonBody = jsonBodyProjector.toThingJsonBody(thingDto);
        return new Response(univJsonBody, deptJsonBody, thingJsonBody);
    }

    public ResponseWithItems createResponseWithItems(ThingDto thingDto) throws InternalServerErrorException, NotFoundException {
        UniversityJsonBody univJsonBody = jsonBodyProjector.toUniversityJsonBody(univ);
        DepartmentJsonBody deptJsonBody = jsonBodyProjector.toDepartmentJsonBody(dept);
        ThingJsonBodyWithItems thingJsonBodyWithItems = jsonBodyProjector.toThingJsonBodyWithItems(thingDto);
        return new ResponseWithItems(univJsonBody, deptJsonBody, thingJsonBodyWithItems);
    }

    private ListResponse createListResponse(List<ThingDto> thingDtoList) throws InternalServerErrorException, NotFoundException {
        UniversityJsonBody univJsonBody = jsonBodyProjector.toUniversityJsonBody(univ);
        DepartmentJsonBody deptJsonBody = jsonBodyProjector.toDepartmentJsonBody(dept);
        List<ThingJsonBody> thingList = new ArrayList<>();
        for(int i = 0; i < thingDtoList.size(); i++) {
            thingList.add(jsonBodyProjector.toThingJsonBody(thingDtoList.get(i)));
        }
        return new ListResponse(univJsonBody, deptJsonBody, thingList);
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
    
    public class Response {
        UniversityJsonBody university;
        DepartmentJsonBody department;
        ThingJsonBody thing;

        public Response(UniversityJsonBody university, DepartmentJsonBody department, ThingJsonBody thing) {
            this.university = university;
            this.department = department;
            this.thing = thing;
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
    }
    
    public class ResponseWithItems {
        UniversityJsonBody university;
        DepartmentJsonBody department;
        ThingJsonBodyWithItems thing;

        public ResponseWithItems(UniversityJsonBody university, DepartmentJsonBody department, ThingJsonBodyWithItems thing) {
            this.university = university;
            this.department = department;
            this.thing = thing;
        }

        public UniversityJsonBody getUniversity() {
            return university;
        }

        public DepartmentJsonBody getDepartment() {
            return department;
        }
        
        public ThingJsonBodyWithItems getThing() {
            return thing;
        }
    }
    
    
    public class ListResponse {
        UniversityJsonBody university;
        DepartmentJsonBody department;
        List<ThingJsonBody> things;

        public ListResponse(UniversityJsonBody university, DepartmentJsonBody department, List<ThingJsonBody> things) {
            this.university = university;
            this.department = department;
            this.things = things;
        }

        public UniversityJsonBody getUniversity() {
            return university;
        }
        
        public DepartmentJsonBody getDepartment() {
            return department;
        }

        public List<ThingJsonBody> getThings() {
            return things;
        }
    }
}