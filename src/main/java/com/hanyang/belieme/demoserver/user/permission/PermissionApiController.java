package com.hanyang.belieme.demoserver.user.permission;

import java.util.List;

import com.hanyang.belieme.demoserver.common.ResponseHeader;
import com.hanyang.belieme.demoserver.common.ResponseWrapper;
import com.hanyang.belieme.demoserver.department.DepartmentDB;
import com.hanyang.belieme.demoserver.department.DepartmentRepository;
import com.hanyang.belieme.demoserver.department.major.MajorRepository;
import com.hanyang.belieme.demoserver.exception.NotFoundException;
import com.hanyang.belieme.demoserver.exception.WrongInDataBaseException;
import com.hanyang.belieme.demoserver.university.University;
import com.hanyang.belieme.demoserver.university.UniversityRepository;
import com.hanyang.belieme.demoserver.user.User;
import com.hanyang.belieme.demoserver.user.UserDB;
import com.hanyang.belieme.demoserver.user.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path="/univs/{univCode}/users/{studentId}/permissions")
public class PermissionApiController {
    @Autowired
    private UniversityRepository universityRepository;
    
    @Autowired
    private DepartmentRepository departmentRepository;
    
    @Autowired
    private UserRepository userRepository;
        
    @Autowired
    private MajorRepository majorRepository;
    
    @Autowired
    private PermissionRepository permissionRepository;
    
    @PostMapping("") //TODO 일단은 user를 output으로 하지만 permission을 output으로 하는 것이 맞지 않을까라는 생각이 든다.
    public ResponseWrapper<Response> postNewPermission(@PathVariable String univCode, @PathVariable String studentId, @RequestBody PermissionRequestBody requestBody) {
        if(requestBody.getDeptCode() == null || requestBody.getPermission() == null) {
            return new ResponseWrapper<>(ResponseHeader.LACK_OF_REQUEST_BODY_EXCEPTION, null);
        }
        
        University univ;
        try {
            univ = University.findByUnivCode(universityRepository, univCode);
        } catch(NotFoundException e) {
            return new ResponseWrapper<>(ResponseHeader.NOT_FOUND_EXCEPTION, null);
        } catch(WrongInDataBaseException e) {
            return new ResponseWrapper<>(ResponseHeader.WRONG_IN_DATABASE_EXCEPTION, null);
        }
        
        DepartmentDB dept;
        try {
            dept = DepartmentDB.findByUnivCodeAndDeptCode(universityRepository, departmentRepository, univCode, requestBody.getDeptCode());
        } catch(NotFoundException e) {
            return new ResponseWrapper<>(ResponseHeader.NOT_FOUND_EXCEPTION, null);
        } catch(WrongInDataBaseException e) {
            return new ResponseWrapper<>(ResponseHeader.WRONG_IN_DATABASE_EXCEPTION, null);
        }
        
        UserDB userDB;
        try {
            userDB = UserDB.findByUnivCodeAndStudentId(universityRepository, userRepository, univCode, studentId);    
        } catch(NotFoundException e) {
            return new ResponseWrapper<>(ResponseHeader.EXPIRED_USER_TOKEN_EXCEPTION, null);
        } catch(WrongInDataBaseException e) {
            return new ResponseWrapper<>(ResponseHeader.WRONG_IN_DATABASE_EXCEPTION, null);
        }
        
        User targetUser = userDB.toUser(departmentRepository, majorRepository, permissionRepository);
        
        PermissionDB newPermissionDB = new PermissionDB();
        if(targetUser.permissionsContainsKey(requestBody.getDeptCode())) {
            List<PermissionDB> tmp = permissionRepository.findByUserIdAndDeptId(userDB.getId(), dept.getId());
            if(tmp.size() == 1) {
                newPermissionDB = tmp.get(0);    
            } else {
                return new ResponseWrapper<>(ResponseHeader.WRONG_IN_DATABASE_EXCEPTION, null);
            }
        }
        newPermissionDB.setUserId(userDB.getId());
        newPermissionDB.setDeptId(dept.getId());
        
        switch(requestBody.getPermission()) {
            case "USER" : {
                newPermissionDB.setPermissionUser();
                permissionRepository.save(newPermissionDB);
                break;
            }
            case "STAFF" : {
                newPermissionDB.setPermissionStaff();
                permissionRepository.save(newPermissionDB);
                break;
            }
            case "MASTER" : {
                newPermissionDB.setPermissionMaster();
                permissionRepository.save(newPermissionDB);
                break;
            }
            default :
                return new ResponseWrapper<>(ResponseHeader.WRONG_PERMISSION_EXCEPTION, null);
        }
        
        User output = userDB.toUser(departmentRepository, majorRepository, permissionRepository);
        return new ResponseWrapper<>(ResponseHeader.OK, new Response(univ, output));
        
    }
    
    public class Response {
        University university;
        User user;

        public Response(University university, User user) {
            if(university == null) {
                this.university = null;
            } else {
                this.university = new University(university);    
            }
            
            if(user == null) {
                this.user = null;
            } else {
                this.user = user;    
            }
        }

        public University getUniversity() {
            if(university == null) {
                return null;
            }
            return new University(university);
        }
        
        public User getUser() {
            if(user == null) {
                return null;
            }
            return user;
        }
    } 
}