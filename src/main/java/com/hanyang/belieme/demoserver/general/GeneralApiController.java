package com.hanyang.belieme.demoserver.general;

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
import com.hanyang.belieme.demoserver.user.UserDB;
import com.hanyang.belieme.demoserver.user.UserRepository;
import com.hanyang.belieme.demoserver.user.UserWithToken;
import com.hanyang.belieme.demoserver.user.UserWithUniversity;
import com.hanyang.belieme.demoserver.user.permission.PermissionDB;
import com.hanyang.belieme.demoserver.user.permission.PermissionRepository;

@RestController
public class GeneralApiController {
    private static final String client_id = "a4b1abe746f384c3d43fa82a17f222";
    private static final int HYU_ID = 1;
    private static final int CKU_ID = 2;
    private static final int SNU_ID = 3;
    
    @Autowired
    private UniversityRepository universityRepository;
    
    @Autowired
    private DepartmentRepository departmentRepository;
    
    @Autowired
    private MajorRepository majorRepository;
    
    @Autowired
    private PermissionRepository permissionRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @GetMapping("/apiVer")
    public String getVer() {
        String result = "1.1";
        return result;
    }
    
    @GetMapping("/login")
    public ResponseWrapper<ResponseWithToken> getUserInfoFromUnivApi(@RequestBody LoginInfo requestBody) {
        if(requestBody.getUnivCode() == null || requestBody.getApiToken() == null) {
            return new ResponseWrapper<>(ResponseHeader.LACK_OF_REQUEST_BODY_EXCEPTION, null);
        }
        
        UserDB outputResponse;
        University univ;
        int univId;
        try {
            univ = University.findByUnivCode(universityRepository, requestBody.getUnivCode()); 
            univId = univ.getId();
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
                    con.setRequestProperty("access_token", requestBody.getApiToken());
            
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
    
                    List<UserDB> existUserList = userRepository.findByUniversityIdAndStudentId(univId, (String) (tmp.get("gaeinNo")));
            
                    UserDB newUserInfo = null;
                    for(int i = 0; i < existUserList.size(); i++) {
                        if(newUserInfo == null) {
                            newUserInfo = existUserList.get(i);
                        } else {
                            return new ResponseWrapper<>(ResponseHeader.WRONG_IN_DATABASE_EXCEPTION, null);
                        }
                    }
                    if(newUserInfo == null) {                   
                        newUserInfo = new UserDB();
                        newUserInfo.setCreateTimeStampNow();
                        newUserInfo.setUniversityId(univId);
                    }
                     
                    List<DepartmentDB> departmentsByUnivId = departmentRepository.findByUniversityId(univId);
                    List<Major> majorsByUnivId = new ArrayList<Major>();
                    for(int i = 0; i < departmentsByUnivId.size(); i++) {
                        majorsByUnivId.addAll(majorRepository.findByDepartmentId(departmentsByUnivId.get(i).getId()));
                    }
                    
                    String sosokId = (String) tmp.get("sosokId");
                    int newDeptId = 0;
                    for(int i = 0; i < majorsByUnivId.size(); i++) {
                        if(sosokId.equals(majorsByUnivId.get(i).getCode())) {
                            int newMajorId = majorsByUnivId.get(i).getId();
                            if(!newUserInfo.getMajorIds().contains(newMajorId)) {
                                newUserInfo.getMajorIds().add(newMajorId);
                                newDeptId = majorsByUnivId.get(i).getDepartmentId();
                            }
                        }
                    }
                     
                    newUserInfo.setStudentId((String) (tmp.get("gaeinNo")));
                    newUserInfo.setName((String) (tmp.get("userNm")));
                    newUserInfo.setEntranceYear(Integer.parseInt(((String) (tmp.get("gaeinNo"))).substring(0,4)));             
            
                    newUserInfo.setNewToken(userRepository);
                    newUserInfo.setApprovalTimeStampNow();
                    outputResponse = userRepository.save(newUserInfo);
                     
                    boolean isNew = true; 
                    if(newDeptId != 0) {
                        List<PermissionDB> permissionListByUserId = permissionRepository.findByUserId(outputResponse.getId());
                        for(int i = 0; i < permissionListByUserId.size(); i++) {
                            if(newDeptId == permissionListByUserId.get(i).getDeptId()) {
                                isNew = false;                                
                            }
                        }
                        if(isNew) {
                            PermissionDB newPermissionDB = new PermissionDB();
                            newPermissionDB.setUserId(outputResponse.getId());
                            newPermissionDB.setDeptId(newDeptId);
                            newPermissionDB.setPermissionUser();
                            permissionRepository.save(newPermissionDB);
                        }
                    }
                    if(bos != null) bos.close();
                    if(in != null) in.close();            
                } catch (Exception e) {
                     e.printStackTrace();
                     return new ResponseWrapper<>(ResponseHeader.WRONG_IN_CONNECTION_EXCEPTION, null);
                }
                return new ResponseWrapper<>(ResponseHeader.OK, new ResponseWithToken(univ, outputResponse.toUserWithToken(universityRepository, departmentRepository, majorRepository, permissionRepository)));
            }
            default : {
                return new ResponseWrapper<>(ResponseHeader.UNREGISTERED_UNIVERSITY_EXCEPTION, null);
            }
        }
    }
    
    @GetMapping("/me")
    public ResponseWrapper<ResponseWithUniversity> getUserUsingUserToken(@RequestHeader(value = "User-Token") String userToken) {
        List<UserDB> userListByToken = userRepository.findByToken(userToken);
        if(userListByToken.size() == 0) {
            return new ResponseWrapper<>(ResponseHeader.NOT_FOUND_EXCEPTION, null);
        } else if(userListByToken.size() != 1) {
            return new ResponseWrapper<>(ResponseHeader.WRONG_IN_DATABASE_EXCEPTION, null);
        } else {
            UserDB target = userListByToken.get(0);
            if(System.currentTimeMillis()/1000 < target.tokenExpiredTime()) {
                return new ResponseWrapper<>(ResponseHeader.OK, new ResponseWithUniversity(userListByToken.get(0).toUserWithUniversity(universityRepository, departmentRepository, majorRepository, permissionRepository)));    
            } else {
                target.setApprovalTimeStampZero();
                target.resetToken();
                userRepository.save(target);
                return new ResponseWrapper<>(ResponseHeader.EXPIRED_USER_TOKEN_EXCEPTION, null);
            }
        }
    }
    
    public class ResponseWithUniversity {
        UserWithUniversity user;

        public ResponseWithUniversity(UserWithUniversity user) {
            if(user == null) {
                this.user = null;
            } else {
                this.user = user;    
            }
        }
        
        public UserWithUniversity getUser() {
            if(user == null) {
                return null;
            }
            return user;
        }
    }
    
    public class ResponseWithToken {
        UserWithToken user;

        public ResponseWithToken(University university, UserWithToken user) {
            if(user == null) {
                this.user = null;
            } else {
                this.user = user;    
            }
        }
        
        public UserWithToken getUser() {
            if(user == null) {
                return null;
            }
            return user;
        }
    } 
}