package com.hanyang.belieme.demoserver.general;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;


import com.hanyang.belieme.demoserver.common.*;
import com.hanyang.belieme.demoserver.department.DepartmentDB;
import com.hanyang.belieme.demoserver.department.DepartmentRepository;
import com.hanyang.belieme.demoserver.department.major.Major;
import com.hanyang.belieme.demoserver.department.major.MajorRepository;
import com.hanyang.belieme.demoserver.exception.BadRequestException;
import com.hanyang.belieme.demoserver.exception.GateWayTimeOutException;
import com.hanyang.belieme.demoserver.exception.HttpException;
import com.hanyang.belieme.demoserver.exception.InternalServerErrorException;
import com.hanyang.belieme.demoserver.exception.MethodNotAllowedException;
import com.hanyang.belieme.demoserver.exception.NotFoundException;
import com.hanyang.belieme.demoserver.exception.UnauthorizedException;
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
    public ResponseEntity<ResponseWithToken> getUserInfoFromUnivApi(@RequestBody LoginInfo requestBody) throws HttpException {
        if(requestBody.getUnivCode() == null || requestBody.getApiToken() == null) {
            throw new BadRequestException("Request body에 정보가 부족합니다.\n필요한 정보 : univCode(String), apiToken(String)");
        }
        
        ResponseEntity.BodyBuilder responseBodyBuilder = ResponseEntity.ok();

        
        UserDB outputResponse;
        University univ = University.findByUnivCode(universityRepository, requestBody.getUnivCode()); 
        int univId = univ.getId();
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
                     
                    URI location;
                    try {
                        location = new URI(Globals.serverUrl + "/univs/" + requestBody.getUnivCode() + "/users/" + (String) (tmp.get("gaeinNo")));    
                    } catch(URISyntaxException e) {
                        e.printStackTrace();
                        throw new InternalServerErrorException("안알랴줌");
                    }
            
                    UserDB newUserInfo = null;
                    try {
                        newUserInfo = UserDB.findByUnivCodeAndStudentId(universityRepository, userRepository, requestBody.getUnivCode(), (String) (tmp.get("gaeinNo")));    
                    } catch (NotFoundException e) {
                        newUserInfo = new UserDB();
                        newUserInfo.setCreateTimeStampNow();
                        newUserInfo.setUniversityId(univId);
                        responseBodyBuilder = ResponseEntity.created(location);
                    } catch (InternalServerErrorException e) {
                        throw e;
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
                     throw new GateWayTimeOutException("서버 내부에서 https://api.hanyang.ac.kr과 통신하는데 문제가 발생하였습니다.");
                }
                return responseBodyBuilder.body(new ResponseWithToken(univ, outputResponse.toUserWithToken(universityRepository, departmentRepository, majorRepository, permissionRepository)));
            }
            default : {
                throw new MethodNotAllowedException("등록되어있지 않은 학교에 대한 요청입니다."); // TODO 새로운 exception으로 바꾸기
            }
        }
    }
    
    @GetMapping("/me")
    public ResponseEntity<ResponseWithUniversity> getUserUsingUserToken(@RequestHeader(value = "User-Token") String userToken) throws HttpException {
        UserDB target = UserDB.findByToken(userRepository, userToken);
        if(System.currentTimeMillis()/1000 < target.tokenExpiredTime()) {
            return ResponseEntity.ok().body(new ResponseWithUniversity(target.toUserWithUniversity(universityRepository, departmentRepository, majorRepository, permissionRepository)));    
        } else {
            target.setApprovalTimeStampZero();
            target.resetToken();
            userRepository.save(target);
            throw new UnauthorizedException("토큰이 만료되었습니다.");
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