package com.belieme.server.web.controller;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import com.belieme.server.domain.department.*;

import com.belieme.server.data.university.*;
import com.belieme.server.data.department.*;
import com.belieme.server.data.major.*;
import com.belieme.server.data.user.*;
import com.belieme.server.data.permission.*;
import com.belieme.server.data.thing.*;
import com.belieme.server.data.item.*;
import com.belieme.server.data.event.*;

import com.belieme.server.domain.university.UniversityDto;
import com.belieme.server.domain.user.UserDto;
import com.belieme.server.web.common.Globals;
import com.belieme.server.web.exception.*;
import com.belieme.server.web.jsonbody.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

// TODO Code들 UPPERCASE로 만들어 버리기...(4)

@RestController
@RequestMapping("/univs/{univCode}/depts")
public class DepartmentApiController extends ApiController {
    @Autowired
    public DepartmentApiController(UniversityRepository univRepo, DepartmentRepository deptRepo, MajorRepository majorRepo, UserRepository userRepo, PermissionRepository permissionRepo, ThingRepository thingRepo, ItemRepository itemRepo, EventRepository eventRepo) {
        super(univRepo, deptRepo, majorRepo, userRepo, permissionRepo, thingRepo, itemRepo, eventRepo);
    }
    
    @GetMapping("")
    public ResponseEntity<ListResponse> getDepartments(@RequestHeader("user-token") String userToken, @PathVariable String univCode) throws NotFoundException, InternalServerErrorException, UnauthorizedException, ForbiddenException {
        init(userToken, univCode);
        checkIfRequesterIsDeveloper();
        
        List<DepartmentDto> deptDtoList = dataAdapter.findAllDeptsByUnivCode(univCode);
        return createGetListResponseEntity(deptDtoList);
    }

    @GetMapping("/{deptCode}")
    public ResponseEntity<Response> getDepartment(@RequestHeader("user-token") String userToken, @PathVariable String univCode, @PathVariable String deptCode) throws NotFoundException, InternalServerErrorException, UnauthorizedException, ForbiddenException {
        init(userToken, univCode);
        checkIfRequesterIsDeveloper();

        DepartmentDto deptDto = dataAdapter.findDeptByUnivCodeAndDeptCode(univCode, deptCode);
        return createGetResponseEntity(deptDto);
    }
    
    private DepartmentJsonBody getDepartmentByUnivCodeAndDeptCodeAndCastToJsonBody(String univCode, String deptCode) throws InternalServerErrorException, NotFoundException {
        return jsonBodyProjector.toDepartmentJsonBody(dataAdapter.findDeptByUnivCodeAndDeptCode(univCode, deptCode));
    }
    
    @PostMapping("")
    public ResponseEntity<Response> postNewDepartment(@RequestHeader("user-token") String userToken, @PathVariable String univCode, @RequestBody DepartmentJsonBody requestBody) throws BadRequestException, NotFoundException, InternalServerErrorException, MethodNotAllowedException, ConflictException, UnauthorizedException, ForbiddenException {
        init(userToken, univCode);
        checkIfRequesterIsDeveloper();

        if(requestBody.code == null || requestBody.name == null) {
            throw new BadRequestException("Request body에 정보가 부족합니다.\n필요한 정보 : code(String), name(String)");
        }
        
        DepartmentDto savedDept = dataAdapter.saveDept(toDepartmentDto(univCode, requestBody));
        String location = Globals.serverUrl + "/univs/" + univCode + "/depts/" + requestBody.code;
        
        return createPostResponseEntity(location, savedDept);
    }
    
    private DepartmentDto toDepartmentDto(String univCode, DepartmentJsonBody jsonBody) {
        DepartmentDto output = new DepartmentDto();
        output.setCode(jsonBody.code);
        output.setName(jsonBody.name);
        output.setUnivCode(univCode);
        output.setAvailable(jsonBody.available);
     
        return output;
    }
    
    @PatchMapping("/{deptCode}")
    public ResponseEntity<Response> updateDepartment(@RequestHeader("user-token") String userToken, @PathVariable String univCode, @PathVariable String deptCode, @RequestBody DepartmentJsonBody requestBody) throws BadRequestException, NotFoundException, InternalServerErrorException, MethodNotAllowedException, ConflictException, UnauthorizedException, ForbiddenException {
        init(userToken, univCode);
        checkIfRequesterIsDeveloper();

        if(requestBody.name == null && requestBody.code == null) {
            throw new BadRequestException("Request body에 정보가 부족합니다.\n필요한 정보 : code(String), name(String) 중 최소 하나");
        }

        DepartmentJsonBody target = getDepartmentByUnivCodeAndDeptCodeAndCastToJsonBody(univCode, deptCode);
        if(requestBody.name == null) {
            requestBody.name = target.name;
        }
        if(requestBody.code == null) {
            requestBody.code = target.code;
        }
        
        DepartmentDto savedDept = dataAdapter.updateDept(univCode, deptCode, toDepartmentDto(univCode, requestBody));
        return createGetResponseEntity(savedDept);
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

    private ResponseEntity<Response> createGetResponseEntity(DepartmentDto deptDto) throws InternalServerErrorException {
        return ResponseEntity.ok().body(createResponse(deptDto));
    }

    private ResponseEntity<Response> createPostResponseEntity(String location, DepartmentDto deptDto) throws InternalServerErrorException {
        URI uri = createUri(location);
        return ResponseEntity.created(uri).body(createResponse(deptDto));
    }

    private ResponseEntity<ListResponse> createGetListResponseEntity(List<DepartmentDto> deptDtoList) throws InternalServerErrorException {
        return ResponseEntity.ok().body(createListResponse(deptDtoList));
    }

    private Response createResponse(DepartmentDto deptDto) throws InternalServerErrorException {
        UniversityJsonBody univJsonBody = jsonBodyProjector.toUniversityJsonBody(univ);
        DepartmentJsonBody deptJsonBody = jsonBodyProjector.toDepartmentJsonBody(deptDto);
        return new Response(univJsonBody, deptJsonBody);
    }

    private ListResponse createListResponse(List<DepartmentDto> deptDtoList) throws InternalServerErrorException {
        UniversityJsonBody univJsonBody = jsonBodyProjector.toUniversityJsonBody(univ);
        List<DepartmentJsonBody> deptJsonBodyList = new ArrayList<>();
        for(int i = 0; i < deptDtoList.size(); i++) {
            deptJsonBodyList.add(jsonBodyProjector.toDepartmentJsonBody(deptDtoList.get(i)));
        }
        return new ListResponse(univJsonBody, deptJsonBodyList);
    }

    //TODO 활성화/비활성화 patch
    public class Response {
        UniversityJsonBody university;
        DepartmentJsonBody department;

        public Response(UniversityJsonBody university, DepartmentJsonBody department) {
            this.university = university;
            this.department = department;
        }

        public UniversityJsonBody getUniversity() {
            if(university == null) {
                return null;
            }
            return university;
        }

        public DepartmentJsonBody getDepartment() {
            if(department == null) {
                return null;
            }
            return department;
        }
    }

    public class ListResponse {
        UniversityJsonBody university;
        ArrayList<DepartmentJsonBody> departments;

        public ListResponse(UniversityJsonBody university, List<DepartmentJsonBody> departments) {
            this.university = university;
            this.departments = new ArrayList<>(departments);
        }

        public UniversityJsonBody getUniversity() {
            if(university == null) {
                return null;
            }
            return university;
        }

        public List<DepartmentJsonBody> getDepartments() {
            return departments;
        }
    }
}