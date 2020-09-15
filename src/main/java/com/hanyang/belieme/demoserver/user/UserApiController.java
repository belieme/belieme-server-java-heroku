package com.hanyang.belieme.demoserver.user;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import com.hanyang.belieme.demoserver.common.*;
import com.hanyang.belieme.demoserver.department.DepartmentDB;
import com.hanyang.belieme.demoserver.department.DepartmentRepository;
import com.hanyang.belieme.demoserver.department.major.Major;
import com.hanyang.belieme.demoserver.department.major.MajorRepository;
import com.hanyang.belieme.demoserver.exception.NotFoundException;
import com.hanyang.belieme.demoserver.exception.WrongInDataBaseException;
import com.hanyang.belieme.demoserver.university.University;
import com.hanyang.belieme.demoserver.university.UniversityRepository;


@RestController
@RequestMapping(path="/univs/{univCode}")
public class UserApiController {

    
    @Autowired
    private UniversityRepository universityRepository;
    
    @Autowired
    private DepartmentRepository departmentRepository;
    
    @Autowired
    private MajorRepository majorRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    // TODO 특정 department의 모든 user불러오는 api
    
    // @GetMapping("/users")
    // public ResponseWrapper<Iterable<UserDB>> getAll(@RequestHeader("user-token") String userToken, @PathVariable String univCode) {
    //     if(userToken == null) {
    //         return new ResponseWrapper<>(ResponseHeader.LACK_OF_REQUEST_HEADER_EXCEPTION, null);
    //     }
        
    //     int univId;
    //     try {
    //         univId = University.findIdByUnivCode(universityRepository, univCode);
    //     } catch(NotFoundException e) {
    //         return new ResponseWrapper<>(ResponseHeader.NOT_FOUND_EXCEPTION, null);
    //     } catch(WrongInDataBaseException e) {
    //         return new ResponseWrapper<>(ResponseHeader.WRONG_IN_DATABASE_EXCEPTION, null);
    //     }
                
    //     int userId;
    //     try {
    //         userId = User.findIdByUnivCodeAndStudentId(universityRepository, userRepository, univCode, userToken);    
    //     } catch(NotFoundException e) {
    //         return new ResponseWrapper<>(ResponseHeader.EXPIRED_USER_TOKEN_EXCEPTION, null);
    //     } catch(WrongInDataBaseException e) {
    //         return new ResponseWrapper<>(ResponseHeader.WRONG_IN_DATABASE_EXCEPTION, null);
    //     }
        
    //     UserDB userDB = userRepository.findById(userId).get();
        
    //     if(userDB.getUniversityId() != univId) {
    //         return new ResponseWrapper<>(ResponseHeader.USER_PERMISSION_DENIED_EXCEPTION, null);    
    //     }
    //        
    // }
}
