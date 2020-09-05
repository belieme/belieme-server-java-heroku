package com.hanyang.belieme.demoserver.user;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import com.hanyang.belieme.demoserver.common.*;
import com.hanyang.belieme.demoserver.exception.NotFoundException;
import com.hanyang.belieme.demoserver.exception.WrongInDataBaseException;
import com.hanyang.belieme.demoserver.university.University;
import com.hanyang.belieme.demoserver.university.UniversityRepository;


@RestController
@RequestMapping(path="") //TODO university 추가하기
public class UserApiController {
    private static final String client_id = "a4b1abe746f384c3d43fa82a17f222";
    private static final int HYU_ID = 1;
    private static final int CKU_ID = 2;
    private static final int SNU_ID = 3;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private UniversityRepository universityRepository;
    
    @GetMapping("/users/all")
    public Iterable<UserDB> getAll(){
        return userRepository.findAll();
    }
    
    @DeleteMapping("/users")
    public void deleteAll() {
        userRepository.deleteAll();
    }
    
    @GetMapping("/universities/{univCode}/users")
    public ResponseWrapper<User> getUserInfoFromUnivApi(@PathVariable String univCode, @RequestParam(value = "apiToken") String apiToken) {
        UserDB outputResponse;
        int univId;
        try {
            univId = University.findIdByUniversityCode(universityRepository, univCode);    
        } catch(NotFoundException e) {
            return new ResponseWrapper<>(ResponseHeader.NOT_FOUND_EXCEPTION, null);
        } catch(WrongInDataBaseException e) {
            return new ResponseWrapper<>(ResponseHeader.WRONG_IN_DATABASE_EXCEPTION, null);
        }
        switch(univId) {
            case HYU_ID : {
                try {
                    URL url = new URL("https://api.hanyang.ac.kr/rs/user/loginInfo.json");
                    HttpURLConnection con = (HttpURLConnection) url.openConnection();
                    con.setRequestMethod("GET");
                    con.setRequestProperty("Host", "https://api.hanyang.ac.kr/");
                    con.setRequestProperty("client_id", client_id);
                    con.setRequestProperty("swap_key", Long.toString(System.currentTimeMillis()/1000));
                    con.setRequestProperty("access_token", apiToken);
            
                    InputStream in = null;
                    ByteArrayOutputStream bos = new ByteArrayOutputStream();
                    byte[] buf = new byte[4096];
                    in = con.getInputStream();
                    while(true) {
                        int readlen = in.read(buf);
                        if( readlen < 1 )
                            break;
                        bos.write(buf, 0, readlen);
                    }
                    String output = new String(bos.toByteArray(), "UTF-8");
                    JSONParser jsonParser = new JSONParser();
                    JSONObject jsonOutput = (JSONObject)jsonParser.parse(output);
        
                    JSONObject response = (JSONObject) jsonOutput.get("response");
        
                    JSONObject tmp = (JSONObject) response.get("item");
    
                    List<UserDB> existUserList = userRepository.findByStudentId((String) (tmp.get("gaeinNo")));
            
                    UserDB newUserInfo = null;
                    
                    for(int i = 0; i < existUserList.size(); i++) {
                        if(existUserList.get(i).getUniversityId() == univId) {
                            if(newUserInfo == null) {
                                newUserInfo = existUserList.get(i);
                            } else {
                                return new ResponseWrapper<>(ResponseHeader.WRONG_IN_DATABASE_EXCEPTION, null);
                            }
                        }
                    }
                    if(newUserInfo == null) {                   
                        newUserInfo = new UserDB();
                        newUserInfo.setCreateTimeStampNow();
                        newUserInfo.setUniversityId(univId);
                        newUserInfo.permissionSetUser();
                    }
                    
                    newUserInfo.setStudentId((String) (tmp.get("gaeinNo")));
                    newUserInfo.setName((String) (tmp.get("userNm")));
                    newUserInfo.setEntranceYear(Integer.parseInt(((String) (tmp.get("gaeinNo"))).substring(0,4)));             
            
                    newUserInfo.setNewToken(userRepository);
                    newUserInfo.setApprovalTimeStampNow();
            
                    outputResponse = userRepository.save(newUserInfo);
            
                    if(bos != null) bos.close();
                    if(in != null) in.close();            
                } catch (Exception e) {
                    return new ResponseWrapper<>(ResponseHeader.WRONG_IN_CONNECTION_EXCEPTION, null);
                }
                try {
                    return new ResponseWrapper<>(ResponseHeader.OK, outputResponse.toUser(universityRepository));   
                } catch(NotFoundException e) {
                    return new ResponseWrapper<>(ResponseHeader.NOT_FOUND_EXCEPTION, null);
                }
            }
            default : {
                return new ResponseWrapper<>(ResponseHeader.UNREGISTERED_UNIVERSITY_EXCEPTION, null);
            }
        }
    }
    
    @GetMapping("/users")
    public ResponseWrapper<User> getUserUsingUserToken(@RequestParam(value = "userToken") String userToken) {
        List<UserDB> userListByToken = userRepository.findByToken(userToken);
        if(userListByToken.size() == 0) {
            return new ResponseWrapper<>(ResponseHeader.NOT_FOUND_EXCEPTION, null);
        } else if(userListByToken.size() != 1) {
            return new ResponseWrapper<>(ResponseHeader.WRONG_IN_DATABASE_EXCEPTION, null);
        } else {
            UserDB target = userListByToken.get(0);
            if(System.currentTimeMillis()/1000 < target.tokenExpiredTime()) {
                try {
                    return new ResponseWrapper<>(ResponseHeader.OK, userListByToken.get(0).toUser(universityRepository));    
                } catch (NotFoundException e) {
                    return new ResponseWrapper<>(ResponseHeader.NOT_FOUND_EXCEPTION, null);
                }
                
            } else {
                target.setApprovalTimeStampZero();
                target.resetToken();
                userRepository.save(target);
                return new ResponseWrapper<>(ResponseHeader.EXPIRED_USER_TOKEN_EXCEPTION, null);
            }
        }
    }
}
