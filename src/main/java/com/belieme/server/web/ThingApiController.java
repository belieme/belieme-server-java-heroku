package com.belieme.server.web;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import com.belieme.server.domain.exception.ServerDomainException;
import com.belieme.server.domain.thing.ThingDto;
import com.belieme.server.domain.user.UserDto;
import com.belieme.server.web.common.*;
import com.belieme.server.web.exception.*;
import com.belieme.server.web.jsonbody.*;

@RestController
@RequestMapping(path="/univs/{univCode}/depts/{deptCode}/things")
public class ThingApiController extends ApiController {
    public ThingApiController() {
        super();
    }

    @GetMapping("")
    public ResponseEntity<ListResponse> getAllThings(@RequestHeader("user-token") String userToken, @PathVariable String univCode, @PathVariable String deptCode) throws HttpException, ServerDomainException {
        if(userToken == null) {
            throw new UnauthorizedException("인증이 진행되지 않았습니다. user-token을 header로 전달해 주시길 바랍니다.");
        }
        
        UniversityJsonBody univ = jsonBodyProjector.toUniversityJsonBody(univDao.findByCode(univCode));
        DepartmentJsonBody dept = jsonBodyProjector.toDepartmentJsonBody(deptDao.findByUnivCodeAndDeptCode(univCode, deptCode));
        
        UserDto user = userDao.findByToken(userToken);
        
        if(!user.hasUserPermission(deptCode)) {
            throw new ForbiddenException("주어진 user-token에 해당하는 user에는 api에 대한 권한이 없습니다.");
        }
            
        List<ThingDto> thingDtoList = thingDao.findByUnivCodeAndDeptCode(univCode, deptCode);
        ArrayList<ThingJsonBody> output = new ArrayList<>();
        for (int i = 0; i < thingDtoList.size(); i++) {
            ThingJsonBody tmp = jsonBodyProjector.toThingJsonBody(thingDtoList.get(i)); 
            output.add(tmp);
        }
        return ResponseEntity.ok().body(new ListResponse(univ, dept, output));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ResponseWithItems> getThingById(@RequestHeader("user-token") String userToken, @PathVariable String univCode, @PathVariable String deptCode, @PathVariable String thingCode) throws HttpException, ServerDomainException {
        if(userToken == null) {
            throw new UnauthorizedException("인증이 진행되지 않았습니다. user-token을 header로 전달해 주시길 바랍니다.");
        }
        
        UniversityJsonBody univ = jsonBodyProjector.toUniversityJsonBody(univDao.findByCode(univCode));
        DepartmentJsonBody dept = jsonBodyProjector.toDepartmentJsonBody(deptDao.findByUnivCodeAndDeptCode(univCode, deptCode));
        
        UserDto user = userDao.findByToken(userToken);
        
        if(!user.hasUserPermission(deptCode)) {
            throw new ForbiddenException("주어진 user-token에 해당하는 user에는 api에 대한 권한이 없습니다.");
        }
        
        ThingDto target = thingDao.findByUnivCodeAndDeptCodeAndCode(univCode, deptCode, thingCode);
        
        ThingJsonBodyWithItems output = jsonBodyProjector.toThingJsonBodyWithItems(target);
        return ResponseEntity.ok().body(new ResponseWithItems(univ, dept, output));
    }

    @PostMapping("")
    public ResponseEntity<ResponseWithItems> createNewThing(@RequestHeader("user-token") String userToken, @PathVariable String univCode, @PathVariable String deptCode, @RequestBody ThingJsonBody requestBody) throws HttpException, ServerDomainException {
        if(userToken == null) {
            throw new UnauthorizedException("인증이 진행되지 않았습니다. user-token을 header로 전달해 주시길 바랍니다.");
        }
        
        if(requestBody.getCode() == null || requestBody.getName() == null || requestBody.getEmoji() == null || requestBody.getDescription() == null) {
            throw new BadRequestException("Request body에 정보가 부족합니다.\n필요한 정보 : name(String), emoji(String), description(String), amount(int)(optional)");
        }
        
        UniversityJsonBody univ = jsonBodyProjector.toUniversityJsonBody(univDao.findByCode(univCode));
        DepartmentJsonBody dept = jsonBodyProjector.toDepartmentJsonBody(deptDao.findByUnivCodeAndDeptCode(univCode, deptCode));
        
        UserDto user = userDao.findByToken(userToken);
        
        if(!user.hasStaffPermission(deptCode)) {
            throw new ForbiddenException("주어진 user-token에 해당하는 user에는 api에 대한 권한이 없습니다.");
        }
        
        ThingDto newThing = new ThingDto();
        newThing.setCode(requestBody.getCode());
        newThing.setName(requestBody.getName());
        newThing.setEmoji(requestBody.getEmoji());
        newThing.setDescription(requestBody.getDescription());
        
        newThing.setUnivCode(univCode);
        newThing.setDeptCode(deptCode);
        
        ThingDto savedThing = thingDao.save(newThing);
        ThingJsonBodyWithItems output = jsonBodyProjector.toThingJsonBodyWithItems(savedThing);
        
        URI location;
        try {
            location = new URI(Globals.serverUrl + "/univs/" + univCode + "/depts/" + deptCode + "/things/" + requestBody.getCode());
        } catch(URISyntaxException e) {
            e.printStackTrace();
            throw new InternalServerErrorException("안알랴줌");
        }
        
        return ResponseEntity.created(location).body(new ResponseWithItems(univ, dept, output));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ResponseWithItems> updateNameAndEmojiOfThing(@RequestHeader("user-token") String userToken, @PathVariable String univCode, @PathVariable String deptCode, @PathVariable String thingCode, @RequestBody ThingJsonBody requestBody) throws HttpException, ServerDomainException {
        if(userToken == null) {
            throw new UnauthorizedException("인증이 진행되지 않았습니다. user-token을 header로 전달해 주시길 바랍니다.");
        }
        
        if(requestBody.getCode() == null && requestBody.getName() == null && requestBody.getEmoji() == null && requestBody.getDescription() == null) {
            throw new BadRequestException("Request body에 정보가 부족합니다.\n필요한 정보 : name(String), emoji(String), description(String) 중 하나 이상");
        }
        
        UniversityJsonBody univ = jsonBodyProjector.toUniversityJsonBody(univDao.findByCode(univCode));
        DepartmentJsonBody dept = jsonBodyProjector.toDepartmentJsonBody(deptDao.findByUnivCodeAndDeptCode(univCode, deptCode));
        
        UserDto user = userDao.findByToken(userToken);  
        
        if(!user.hasStaffPermission(deptCode)) {
            throw new ForbiddenException("주어진 user-token에 해당하는 user에는 api에 대한 권한이 없습니다.");
        }
        
        ThingDto target = thingDao.findByUnivCodeAndDeptCodeAndCode(univCode, deptCode, thingCode);
        
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
        
        ThingDto newAndSavedThing = thingDao.update(univCode, deptCode, thingCode, target);
        ThingJsonBodyWithItems output = jsonBodyProjector.toThingJsonBodyWithItems(newAndSavedThing);
        return ResponseEntity.ok().body(new ResponseWithItems(univ, dept, output));
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