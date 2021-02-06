package com.belieme.server.web;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(path="/univs/{univCode}")
public class UserApiController extends ApiController { //TODO 코드 정독하면서 정리하기
    public UserApiController() {
        super();
    }
    
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
