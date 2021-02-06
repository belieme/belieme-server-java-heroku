package com.belieme.server.web;

import com.belieme.server.domain.exception.*;
import com.belieme.server.domain.university.*;
import com.belieme.server.domain.user.*;
import com.belieme.server.domain.permission.*;

import com.belieme.server.web.exception.*;
import com.belieme.server.web.jsonbody.*;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(path="/univs/{univCode}/users/{studentId}/permissions")
public class PermissionApiController extends ApiController {
    public PermissionApiController() {
        super();
    }
    
    @PostMapping("")
    public ResponseEntity<Response> postNewPermission(@PathVariable String univCode, @PathVariable String studentId, @RequestBody PermissionInfoJsonBody requestBody) throws HttpException, ServerDomainException {
        if(requestBody.getDeptCode() == null || requestBody.getPermission() == null) {
            throw new BadRequestException("Request body에 정보가 부족합니다.\n필요한 정보 : deptCode(String), permission(String)");
        }
        
        UniversityDto univ = univDao.findByCode(univCode);
        
        UserDto user = userDao.findByUnivCodeAndStudentId(univCode, studentId);
        
        PermissionDto permission = new PermissionDto();
        permission.setUnivCode(univCode);
        permission.setDeptCode(requestBody.getDeptCode());
        permission.setPermission(Permissions.valueOf(requestBody.getPermission()));
        if(permission.getPermission() == null) { //TODO 헤더로 token받아서 permission 확인하고 줄 수 있는 권한자만 가능하게 하기
            throw new BadRequestException("RequestBody의 permission은 USER, STAFF, MASTER 중 하나여야 합니다.");
        }
        
        PermissionDto savedPermission = permissionDao.save(permission);
        return ResponseEntity.ok().body(createResponse(univ, user, savedPermission));
        
    }
    
    // TODO Patch 만들기(권환 늘리기 / 줄이기)
    
    private Response createResponse(UniversityDto univ, UserDto user, PermissionDto permission) throws ServerDomainException {
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