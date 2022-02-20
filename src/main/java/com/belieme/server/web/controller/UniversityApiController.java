package com.belieme.server.web.controller;

import com.belieme.server.domain.user.UserDto;
import com.belieme.server.web.common.Globals;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;

import com.belieme.server.domain.university.*;

import com.belieme.server.data.university.*;
import com.belieme.server.data.department.*;
import com.belieme.server.data.major.*;
import com.belieme.server.data.user.*;
import com.belieme.server.data.permission.*;
import com.belieme.server.data.thing.*;
import com.belieme.server.data.item.*;
import com.belieme.server.data.history.*;

import com.belieme.server.web.exception.*;
import com.belieme.server.web.jsonbody.*;

// public method는 클래스 상단에 private method는 클래스 하단에 몰아두기

@RestController
@RequestMapping("/univs")
public class UniversityApiController extends ApiController {
    
    @Autowired
    public UniversityApiController(UniversityRepository univRepo, DepartmentRepository deptRepo, MajorRepository majorRepo, UserRepository userRepo, PermissionRepository permissionRepo, ThingRepository thingRepo, ItemRepository itemRepo, HistoryRepository historyRepo) {
        super(univRepo, deptRepo, majorRepo, userRepo, permissionRepo, thingRepo, itemRepo, historyRepo);
    }
    
    @GetMapping("") 
    public ResponseEntity<ListResponse> getAllUnivsMapping() {
        List<UniversityDto> univDtoList = dataAdapter.findAllUnivs();
        return createGetListResponseEntity(univDtoList);
    }
    
    @GetMapping("/{univCode}") 
    public ResponseEntity<Response> getAnUnivMapping(@RequestHeader("user-token") String userToken, @PathVariable String univCode) throws NotFoundException, InternalServerErrorException, UnauthorizedException, ForbiddenException {
        init(userToken);
        checkIfRequesterIsDeveloper();

        String UpperCaseUnivCode = univCode.toUpperCase();
        UniversityDto output = dataAdapter.findUnivByCode(UpperCaseUnivCode.toUpperCase());
        return createGetResponseEntity(output);
    }
    
    @PostMapping("")
    public ResponseEntity<Response> postNewUnivMapping(@RequestHeader("user-token") String userToken, @RequestBody UniversityJsonBody requestBody) throws BadRequestException, MethodNotAllowedException, InternalServerErrorException, ConflictException, UnauthorizedException, ForbiddenException {
        init(userToken);
        checkIfRequesterIsDeveloper();

        if(requestBody.getName() == null || requestBody.getCode() == null) {
            throw new BadRequestException("Request body에 정보가 부족합니다.\n필요한 정보 : name(String), code(String), apiUrl(String)(Optional)");
        }
        
        UniversityDto savedUniv = dataAdapter.saveUniv(toUniversityDto(requestBody));
        String location = Globals.serverUrl + "/univs/" + requestBody.getCode();
        return createPostResponseEntity(location, savedUniv);
    }
    
    @PatchMapping("/{univCode}")
    public ResponseEntity<Response> updateUniversity(@RequestHeader("user-token") String userToken, @PathVariable String univCode, @RequestBody UniversityJsonBody requestBody) throws BadRequestException, NotFoundException, InternalServerErrorException, MethodNotAllowedException, ConflictException, UnauthorizedException, ForbiddenException {
        init(userToken);
        checkIfRequesterIsDeveloper();

        if(requestBody.getName() == null && requestBody.getCode() == null && requestBody.getApiUrl() == null) {
            throw new BadRequestException("Request body에 정보가 부족합니다.\n필요한 정보 : name(String), code(String), apiUrl(String) 중 최소 하나");
        }
        String UpperCaseUnivCode = univCode.toUpperCase();

        UniversityJsonBody target = jsonBodyProjector.toUniversityJsonBody(dataAdapter.findUnivByCode(UpperCaseUnivCode));
        if(requestBody.getName() == null) {
            requestBody.setName(target.getName());
        }
        if(requestBody.getCode() == null) {
            requestBody.setCode(target.getCode());
        }
        if(requestBody.getApiUrl() == null) {
            requestBody.setApiUrl(target.getApiUrl());
        }
        
        UniversityDto updatedUniv = dataAdapter.updateUniv(UpperCaseUnivCode, toUniversityDto(requestBody));
        return createGetResponseEntity(updatedUniv);
    }

    private UserDto requester;

    private void init(String userToken) throws UnauthorizedException, InternalServerErrorException {
        requester = dataAdapter.findUserByToken(userToken);
    }

    private void checkIfRequesterIsDeveloper() throws ForbiddenException {
        if(!requester.hasDeveloperPermission()) {
            throw new ForbiddenException("주어진 user-token에 해당하는 user에는 api에 대한 권한이 없습니다.");
        }
    }

    private UniversityDto toUniversityDto(UniversityJsonBody univJsonBody) {
        UniversityDto output = new UniversityDto();
        output.setCode(univJsonBody.getCode().toUpperCase());
        output.setName(univJsonBody.getName());
        output.setApiUrl(univJsonBody.getApiUrl());
        
        return output;
    }

    private ResponseEntity<Response> createGetResponseEntity(UniversityDto output) {
        return ResponseEntity.ok().body(createResponse(output));
    }

    private ResponseEntity<Response> createPostResponseEntity(String location, UniversityDto output) throws InternalServerErrorException {
        URI uri = createUri(location);
        return ResponseEntity.created(uri).body(createResponse(output));
    }

    private ResponseEntity<ListResponse> createGetListResponseEntity(List<UniversityDto> output) {
        return ResponseEntity.ok().body(createListResponse(output));
    }

    private Response createResponse(UniversityDto univDto) {
        UniversityJsonBody univ = jsonBodyProjector.toUniversityJsonBody(univDto);
        return new Response(univ);
    }

    private ListResponse createListResponse(List<UniversityDto> univDtoList) {
        List<UniversityJsonBody> univList = new ArrayList<>();
        for(int i = 0; i < univDtoList.size(); i++) {
            univList.add(jsonBodyProjector.toUniversityJsonBody(univDtoList.get(i)));
        }
        return new ListResponse(univList);
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
        private UniversityJsonBody univ;
        
        public Response(UniversityJsonBody univ) {
            this.univ = univ;
        }

        public UniversityJsonBody getUniv() {
            return univ;
        }
    }
    
    public class ListResponse {
        List<UniversityJsonBody> univs;

        public ListResponse(List<UniversityJsonBody> univs) {
            this.univs = univs;
        }

        public List<UniversityJsonBody> getUniversities() {
            return univs;
        }
    }
}