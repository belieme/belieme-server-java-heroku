package com.belieme.server.web.controller;

import com.belieme.server.domain.university.UniversityDto;
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
import com.belieme.server.data.history.*;

import com.belieme.server.web.common.*;
import com.belieme.server.web.exception.*;
import com.belieme.server.web.jsonbody.*;

@RestController
public class GeneralApiController extends ApiController {
    private static final String client_id = "a4b1abe746f384c3d43fa82a17f222";
    private static final String HYU_CODE = "HYU";
    private static final String CKU_CODE = "CKU";
    private static final String SNU_CODE = "SNU";
    
    @Autowired
    public GeneralApiController(UniversityRepository univRepo, DepartmentRepository deptRepo, MajorRepository majorRepo, UserRepository userRepo, PermissionRepository permissionRepo, ThingRepository thingRepo, ItemRepository itemRepo, HistoryRepository historyRepo) {
        super(univRepo, deptRepo, majorRepo, userRepo, permissionRepo, thingRepo, itemRepo, historyRepo);
    }
    
    @GetMapping("/apiVer")
    public String getVer() {
        String result = "1.1";
        return result;
    }
    
    @PatchMapping("/login")
    public ResponseEntity<ResponseBody> getUserInfoFromUnivApi(@RequestBody LoginInfoJsonBody requestBody) throws BadRequestException, GateWayTimeOutException, InternalServerErrorException, NotFoundException, MethodNotAllowedException, ConflictException {
        if(requestBody.getUnivCode() == null || requestBody.getApiToken() == null) {
            throw new BadRequestException("Request body에 정보가 부족합니다. 필요한 정보 : univCode(String), apiToken(String)");
        }
        String univCode = requestBody.getUnivCode();
        String studentId;

        switch(univCode) {
            case HYU_CODE : {
            	JSONObject jsonFromApi;
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
                    jsonFromApi = (JSONObject) response.get("item");
                    
                    if(bos != null) bos.close();
                    if(in != null) in.close();
                    
                } catch (Exception e) {
                     e.printStackTrace();
                     throw new GateWayTimeOutException("서버 내부에서 https://api.hanyang.ac.kr과 통신하는데 문제가 발생하였습니다.");
                }
                     
                studentId = (String) (jsonFromApi.get("gaeinNo"));
                boolean isNew = false; 
                UserDto newUser;
                UserDto savedUser;
                UniversityDto univ;
                
                try {
                	newUser = dataAdapter.findUserByUnivCodeAndStudentId(univCode, studentId);    
                } catch (NotFoundException e) {
                    newUser = new UserDto();
                    newUser.setCreateTimeStampNow();
                    newUser.setUnivCode(univCode);
                    isNew = true;
                } catch (InternalServerErrorException e) {
                 	throw new InternalServerErrorException(e);
                }
                     
                newUser.setStudentId((String) (jsonFromApi.get("gaeinNo")));
                newUser.setName((String) (jsonFromApi.get("userNm")));
                newUser.setEntranceYear(Integer.parseInt(((String) (jsonFromApi.get("gaeinNo"))).substring(0,4)));
                newUser.setNewToken();
                newUser.setApprovalTimeStampNow();
                
                String sosokId = (String) jsonFromApi.get("sosokId"); 
                MajorDto major = dataAdapter.findMajorByUnivCodeAndMajorCode(univCode, sosokId);
                 
                if(!newUser.permissionsContainsKey(major.getDeptCode())) {
                    newUser.addPermission(major.getDeptCode(), Permissions.USER);
                }
                
                ResponseEntity.BodyBuilder responseBodyBuilder = ResponseEntity.ok();
                URI location;
                try {
                    location = new URI(Globals.serverUrl + "/univs/" + requestBody.getUnivCode() + "/users/" + studentId);    
                } catch(URISyntaxException e) {
                    e.printStackTrace();
                    throw new InternalServerErrorException("안알랴줌");
                }
                
                if(isNew) {
                	responseBodyBuilder = ResponseEntity.created(location);
                    savedUser = dataAdapter.saveUser(newUser);
                    univ = dataAdapter.findUnivByCode(savedUser.getUnivCode());
                } else {
                    savedUser = dataAdapter.updateUser(univCode, studentId, newUser);
                    univ = dataAdapter.findUnivByCode(savedUser.getUnivCode());
                }

                return responseBodyBuilder.body(new ResponseBody(jsonBodyProjector.toUniversityJsonBody(univ), jsonBodyProjector.toUserJsonBody(savedUser)));
                
            }
            default : {
                throw new MethodNotAllowedException("등록되어있지 않은 학교에 대한 요청입니다."); // TODO 새로운 exception으로 바꾸기...(1)
            }
        }
    }
    
    @GetMapping("/me")
    public ResponseEntity<ResponseBody> getUserUsingUserToken(@RequestHeader(value = "User-Token") String userToken) throws NotFoundException, InternalServerErrorException, UnauthorizedException {
        UserDto user = dataAdapter.findUserByToken(userToken);
        UniversityDto univ = dataAdapter.findUnivByCode(user.getUnivCode());
        return ResponseEntity.ok().body(new ResponseBody(jsonBodyProjector.toUniversityJsonBody(univ), jsonBodyProjector.toUserJsonBody(user)));
    }
    
    public class ResponseBody {
        UniversityJsonBody university;
        UserJsonBody user;

        public ResponseBody(UniversityJsonBody university, UserJsonBody user) {
            this.university = university;
            this.user = user;
        }
        
        public UserJsonBody getUser() {
            return user;
        }

        public UniversityJsonBody getUniversity() { return university; }
    }
}