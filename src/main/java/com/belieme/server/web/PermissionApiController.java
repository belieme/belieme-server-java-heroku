package com.belieme.server.web;

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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(path="/univs/{univCode}/users/{studentId}/permissions")
public class PermissionApiController extends ApiController {
    @Autowired
    public PermissionApiController(UniversityRepository univRepo, DepartmentRepository deptRepo, MajorRepository majorRepo, UserRepository userRepo, PermissionRepository permissionRepo, ThingRepository thingRepo, ItemRepository itemRepo, EventRepository eventRepo) {
        super(univRepo, deptRepo, majorRepo, userRepo, permissionRepo, thingRepo, itemRepo, eventRepo);
    }
    
    @PostMapping("") // TODO 권한 수정도 이걸로 하기 (현재 있는 권한 변경은 duplicateException걸려서 안된다. -> 권한 제거는 Permissions에 추가적으로 BAN이라는 걸 만들어서 USER를 BAN만들어서 제거 하기로)
    public ResponseEntity<Response> postNewPermission(@PathVariable String univCode, @PathVariable String studentId, @RequestBody PermissionInfoJsonBody requestBody) throws HttpException, ServerDomainException {
        if(requestBody.getDeptCode() == null || requestBody.getPermission() == null) {
            throw new BadRequestException("Request body에 정보가 부족합니다.\n필요한 정보 : deptCode(String), permission(String)");
        }
        
        UniversityDto univ = univDao.findByCode(univCode);
        
        UserDto user = userDao.findByUnivCodeAndStudentId(univCode, studentId);
        
        PermissionDto permission = new PermissionDto();
        permission.setUnivCode(univCode);
        permission.setDeptCode(requestBody.getDeptCode());
        permission.setStudentId(studentId);
        permission.setPermission(Permissions.valueOf(requestBody.getPermission()));
        if(permission.getPermission() == null) { //TODO 헤더로 token받아서 permission 확인하고 줄 수 있는 권한자만 가능하게 하기
            throw new BadRequestException("RequestBody의 permission은 BANNED, USER, STAFF, MASTER 중 하나여야 합니다.");
        }
        
        PermissionDto savedPermission;
        try {
            savedPermission = permissionDao.save(permission);
        } catch(CodeDuplicationException e) {
            savedPermission = permissionDao.update(univCode, studentId, requestBody.getDeptCode(), permission);
        }
        
        return ResponseEntity.ok().body(createResponse(univ, user, savedPermission));
    }
    
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