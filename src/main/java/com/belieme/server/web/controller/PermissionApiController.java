package com.belieme.server.web.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.belieme.server.domain.exception.*;
import com.belieme.server.domain.university.*;
import com.belieme.server.domain.user.*;
import com.belieme.server.domain.permission.*;

import com.belieme.server.data.university.*;
import com.belieme.server.data.department.*;
import com.belieme.server.data.major.*;
import com.belieme.server.data.user.*;
import com.belieme.server.data.permission.*;
import com.belieme.server.data.thing.*;
import com.belieme.server.data.item.*;
import com.belieme.server.data.event.*;

import com.belieme.server.web.exception.*;
import com.belieme.server.web.jsonbody.*;


@RestController
@RequestMapping(path="/univs/{univCode}/users/{studentId}/permissions")
public class PermissionApiController extends ApiController {
    @Autowired
    public PermissionApiController(UniversityRepository univRepo, DepartmentRepository deptRepo, MajorRepository majorRepo, UserRepository userRepo, PermissionRepository permissionRepo, ThingRepository thingRepo, ItemRepository itemRepo, EventRepository eventRepo) {
        super(univRepo, deptRepo, majorRepo, userRepo, permissionRepo, thingRepo, itemRepo, eventRepo);
    }
    
    @PostMapping("") // TODO UserToken 받아서 아무나 못 바꾸게 하기
    public ResponseEntity<Response> postNewPermission(@PathVariable String univCode, @PathVariable String studentId, @RequestBody PermissionInfoJsonBody requestBody) throws BadRequestException, NotFoundException, InternalServerErrorException, MethodNotAllowedException, ConflictException {
        if(requestBody.getDeptCode() == null || requestBody.getPermission() == null) {
            throw new BadRequestException("Request body에 정보가 부족합니다.\n필요한 정보 : deptCode(String), permission(String)");
        }
        
        UniversityDto univ = dataAdapter.findUnivByCode(univCode);
        
        PermissionDto permission = new PermissionDto();
        permission.setUnivCode(univCode);
        permission.setDeptCode(requestBody.getDeptCode());
        permission.setStudentId(studentId);
        
        try { // TODO 좀 더 깔쌈하게 하기
            permission.setPermission(Permissions.valueOf(requestBody.getPermission()));    
        } catch(IllegalArgumentException e) {
            throw new BadRequestException("RequestBody의 permission은 BANNED, USER, STAFF, MASTER 중 하나여야 합니다.");
        }
        
        PermissionDto savedPermission;
        try {
            savedPermission = dataAdapter.savePermission(permission);
        } catch(ConflictException e) {
            savedPermission = dataAdapter.updatePermission(univCode, studentId, requestBody.getDeptCode(), permission);
        }
        
        UserDto user = dataAdapter.findUserByUnivCodeAndStudentId(univCode, studentId);
        return ResponseEntity.ok().body(createResponse(univ, user, savedPermission));
    }
    
    private Response createResponse(UniversityDto univ, UserDto user, PermissionDto permission) throws NotFoundException, InternalServerErrorException {
        UniversityJsonBody univJsonBody = jsonBodyProjector.toUniversityJsonBody(univ);
        UserJsonBodyWithoutToken userJsonBody = jsonBodyProjector.toUserJsonBodyWithoutToken(user);
        PermissionJsonBody permissionJsonBody = jsonBodyProjector.toPermissionJsonBody(permission);
        
        return new Response(univJsonBody, userJsonBody, permissionJsonBody);
    }
    
    public class Response {
        UniversityJsonBody university;
        UserJsonBodyWithoutToken user;
        PermissionJsonBody permission;

        public Response(UniversityJsonBody university, UserJsonBodyWithoutToken user, PermissionJsonBody permission) {
            this.university = university;
            this.user = user;
            this.permission = permission;
        }

        public UniversityJsonBody getUniversity() {
            return university;
        }
        
        public UserJsonBodyWithoutToken getUser() {
            return user;
        }
        
        public PermissionJsonBody getPermission() {
            return permission;
        }
    } 
}