package com.belieme.server.web.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.net.URISyntaxException;

import com.belieme.server.domain.exception.*;
import com.belieme.server.domain.major.*;
import com.belieme.server.domain.university.*;

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
@RequestMapping(path="/univs/{univCode}/majors")
public class MajorApiController extends ApiController {
    @Autowired
    public MajorApiController(UniversityRepository univRepo, DepartmentRepository deptRepo, MajorRepository majorRepo, UserRepository userRepo, PermissionRepository permissionRepo, ThingRepository thingRepo, ItemRepository itemRepo, EventRepository eventRepo) {
        super(univRepo, deptRepo, majorRepo, userRepo, permissionRepo, thingRepo, itemRepo, eventRepo);
    }
    
    @PostMapping("")
    public ResponseEntity<Response> postNewMajor(@PathVariable String univCode, @RequestBody MajorInfoJsonBody requestBody) throws BadRequestException, NotFoundException, InternalServerErrorException, MethodNotAllowedException, ConflictException {
        if(requestBody.getMajorCode() == null || requestBody.getDeptCode() == null) {
            throw new BadRequestException("Request body에 정보가 부족합니다.\n필요한 정보 : majorCode(String), deptCode(String)");
        }
        System.out.println("AAAA");
        UniversityDto univ = dataAdapter.findUnivByCode(univCode);
        System.out.println("BBBB");
        
        MajorDto newMajor = new MajorDto();
        newMajor.setUnivCode(univ.getCode());
        newMajor.setCode(requestBody.getMajorCode());
        newMajor.setDeptCode(requestBody.getDeptCode());
        
        MajorDto savedMajor = dataAdapter.saveMajor(newMajor);
        System.out.println("CCCC");
        
        URI location;
        try {
            location = new URI(Globals.serverUrl + "/univs/" + univCode + "/majors/" + requestBody.getMajorCode());    
        } catch(URISyntaxException e) {
            e.printStackTrace();
            throw new InternalServerErrorException("안알랴줌");
        }
        
        return ResponseEntity.created(location).body(createResponse(univ, savedMajor));
    }
    
    private Response createResponse(UniversityDto univDto, MajorDto majorDto) throws NotFoundException, InternalServerErrorException {
        UniversityJsonBody univ = jsonBodyProjector.toUniversityJsonBody(univDto);
        MajorJsonBody major = jsonBodyProjector.toMajorJsonBody(majorDto);
        return new Response(univ, major);
    }
    
    public class Response {
        UniversityJsonBody university;
        MajorJsonBody major;

        public Response(UniversityJsonBody university, MajorJsonBody major) {
            this.university = university;
            this.major = major;
        }

        public UniversityJsonBody getUniversity() {
            return university;
        }

        public MajorJsonBody getMajor() {
            return major;
        }
    }
}