package com.belieme.server.web;

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

import com.belieme.server.domain.exception.*;
import com.belieme.server.domain.major.*;
import com.belieme.server.domain.permission.Permissions;
import com.belieme.server.domain.user.*;

import com.belieme.server.data.university.*;
import com.belieme.server.data.department.*;
import com.belieme.server.data.major.*;
import com.belieme.server.data.user.*;
import com.belieme.server.data.permission.*;
import com.belieme.server.data.thing.*;
import com.belieme.server.data.item.*;
import com.belieme.server.data.event.*;

import com.belieme.server.web.common.Globals;
import com.belieme.server.web.exception.*;
import com.belieme.server.web.jsonbody.*;

@RestController
public class GeneralApiController extends ApiController {
    private static final String client_id = "a4b1abe746f384c3d43fa82a17f222";
    private static final String HYU_CODE = "HYU";
    private static final String CKU_CODE = "CKU";
    private static final String SNU_CODE = "SNU";
    
    @Autowired
    public GeneralApiController(UniversityRepository univRepo, DepartmentRepository deptRepo, MajorRepository majorRepo, UserRepository userRepo, PermissionRepository permissionRepo, ThingRepository thingRepo, ItemRepository itemRepo, EventRepository eventRepo) {
        super(univRepo, deptRepo, majorRepo, userRepo, permissionRepo, thingRepo, itemRepo, eventRepo);
    }
    
    @GetMapping("/apiVer")
    public String getVer() {
        String result = "1.1";
        return result;
    }
    
    @GetMapping("/login")
    public ResponseEntity<ResponseBody> getUserInfoFromUnivApi(@RequestBody LoginInfoJsonBody requestBody) throws HttpException, ServerDomainException {
        if(requestBody.getUnivCode() == null || requestBody.getApiToken() == null) {
            throw new BadRequestException("Request body에 정보가 부족합니다. 필요한 정보 : univCode(String), apiToken(String)");
        }
        String univCode = requestBody.getUnivCode();
        String studentId;
        
        ResponseEntity.BodyBuilder responseBodyBuilder = ResponseEntity.ok();

        switch(univCode) {
            case HYU_CODE : {
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
                    studentId = (String) (tmp.get("gaeinNo"));
                     
                    URI location;
                    try {
                        location = new URI(Globals.serverUrl + "/univs/" + requestBody.getUnivCode() + "/users/" + studentId);    
                    } catch(URISyntaxException e) {
                        e.printStackTrace();
                        throw new InternalServerErrorException("안알랴줌");
                    }
            
                    boolean isNew = false; 
                    UserDto newUser;
                    UserDto savedUser;
                    try {
                         newUser = userDao.findByUnivCodeAndStudentId(univCode, studentId);    
                    } catch (NotFoundOnDataBaseException e) {
                        newUser = new UserDto();
                        newUser.setCreateTimeStampNow();
                        newUser.setUnivCode(univCode);
                        responseBodyBuilder = ResponseEntity.created(location);
                        isNew = true;
                    } catch (ServerDomainException e) {
                        throw e;
                    }
                     
                    newUser.setStudentId((String) (tmp.get("gaeinNo")));
                    newUser.setName((String) (tmp.get("userNm")));
                    newUser.setEntranceYear(Integer.parseInt(((String) (tmp.get("gaeinNo"))).substring(0,4)));
                    newUser.setNewToken();
                    newUser.setApprovalTimeStampNow();
                    
                    String sosokId = (String) tmp.get("sosokId"); 
                    MajorDto major = majorDao.findByUnivCodeAndMajorCode(univCode, sosokId);
                     
                    if(!newUser.permissionsContainsKey(major.getDeptCode())) {
                        newUser.addPermission(major.getDeptCode(), Permissions.USER);
                    }
                    
                    if(isNew) {
                        savedUser = userDao.save(newUser);
                    } else {
                        savedUser = userDao.update(univCode, studentId, newUser);
                    }

                    if(bos != null) bos.close();
                    if(in != null) in.close();  
                    
                    return responseBodyBuilder.body(new ResponseBody(jsonBodyProjector.toUserJsonBody(savedUser)));
                } catch (Exception e) {
                     e.printStackTrace();
                     throw new GateWayTimeOutException("서버 내부에서 https://api.hanyang.ac.kr과 통신하는데 문제가 발생하였습니다.");
                }
            }
            default : {
                throw new MethodNotAllowedException("등록되어있지 않은 학교에 대한 요청입니다."); // TODO 새로운 exception으로 바꾸기
            }
        }
    }
    
    @GetMapping("/me")
    public ResponseEntity<ResponseBodyWithoutToken> getUserUsingUserToken(@RequestHeader(value = "User-Token") String userToken) throws HttpException, ServerDomainException {
        UserDto target = userDao.findByToken(userToken);
        if(System.currentTimeMillis()/1000 < target.tokenExpiredTime()) {
            return ResponseEntity.ok().body(new ResponseBodyWithoutToken(jsonBodyProjector.toUserJsonBodyWithoutToken(target)));
        } else {
            target.setApprovalTimeStampZero();
            target.resetToken();
            userDao.update(target.getUnivCode(), target.getStudentId(), target);
            throw new UnauthorizedException("토큰이 만료되었습니다.");
        }
    }
    
    public class ResponseBody {
        UserJsonBody user;

        public ResponseBody(UserJsonBody user) {
           this.user = user;
        }
        
        public UserJsonBody getUser() {
            return user;
        }
    }
    
    public class ResponseBodyWithoutToken {
        UserJsonBodyWithoutToken user;

        public ResponseBodyWithoutToken(UserJsonBodyWithoutToken user) {
            this.user = user;
        }
        
        public UserJsonBodyWithoutToken getUser() {
            return user;
        }
    } 
}