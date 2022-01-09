package com.belieme.server.web.controller;

import com.belieme.server.domain.user.UserDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.net.URISyntaxException;

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
    public ResponseEntity<Response> postNewMajor(@RequestHeader("user-token") String userToken, @PathVariable String univCode, @RequestBody MajorInfoJsonBody requestBody) throws BadRequestException, NotFoundException, InternalServerErrorException, MethodNotAllowedException, ConflictException, UnauthorizedException, ForbiddenException {
        init(userToken, univCode);
        checkIfRequesterIsDeveloper();

        if(requestBody.getMajorCode() == null || requestBody.getDeptCode() == null) {
            throw new BadRequestException("Request body에 정보가 부족합니다.\n필요한 정보 : majorCode(String), deptCode(String)");
        }

        UniversityDto univ = dataAdapter.findUnivByCode(univCode);

        MajorDto newMajor = new MajorDto();
        newMajor.setUnivCode(univ.getCode());
        newMajor.setCode(requestBody.getMajorCode());
        newMajor.setDeptCode(requestBody.getDeptCode());
        
        MajorDto savedMajor = dataAdapter.saveMajor(newMajor);
        
        String location = Globals.serverUrl + "/univs/" + univCode + "/majors/" + requestBody.getMajorCode();

        return createPostResponseEntity(location, savedMajor);
    }

    private UserDto requester;
    private UniversityDto univ;

    private void init(String userToken, String univCode) throws UnauthorizedException, InternalServerErrorException, NotFoundException {
        requester = dataAdapter.findUserByToken(userToken);
        univ = dataAdapter.findUnivByCode(univCode);
    }

    private void checkIfRequesterIsDeveloper() throws ForbiddenException {
        if(!requester.hasDeveloperPermission()) {
            throw new ForbiddenException("주어진 user-token에 해당하는 user에는 api에 대한 권한이 없습니다.");
        }
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

    private ResponseEntity<Response> createPostResponseEntity(String location, MajorDto majorDto) throws InternalServerErrorException, NotFoundException {
        URI uri = createUri(location);
        return ResponseEntity.created(uri).body(createResponse(majorDto));
    }

    private Response createResponse(MajorDto majorDto) throws InternalServerErrorException, NotFoundException {
        UniversityJsonBody univJsonBody = jsonBodyProjector.toUniversityJsonBody(univ);
        MajorJsonBody majorJsonBody = jsonBodyProjector.toMajorJsonBody(majorDto);
        return new Response(univJsonBody, majorJsonBody);
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