package com.hanyang.belieme.demoserver.user;

import org.json.simple.JSONArray;
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


@RestController
@RequestMapping(path="/universities/HYU/users")
public class UserApiController {
    private static final String client_id = "a4b1abe746f384c3d43fa82a17f222";
    
    @Autowired
    private UserRepository userRepository;
    
    @GetMapping("/all")
    public Iterable<UserDB> getAll(){
        return userRepository.findAll();
    }
    
    @GetMapping("/{accessTokenString}")
    public ResponseWrapper<UserDB> getUserInfoFromUnivApi(@PathVariable String accessTokenString) {
        System.out.println(accessTokenString);
        UserDB outputResponse;
        try {
            URL url = new URL("https://api.hanyang.ac.kr/rs/user/loginList.json");
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET"); 
            con.setRequestProperty("Host", "https://api.hanyang.ac.kr/");
            con.setRequestProperty("client_id", client_id);
            con.setRequestProperty("swap_key", Long.toString(System.currentTimeMillis()/1000));
            con.setRequestProperty("access_token", accessTokenString);
            
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
            JSONArray list = (JSONArray) response.get(response);
            
            JSONObject tmp = (JSONObject) list.get(0);
            
            List<UserDB> existUserList = userRepository.findByStudentId((String) (tmp.get("gaeinNo")));
            
            UserDB newUserInfo;
            if(existUserList.size() == 0) {
                newUserInfo = new UserDB();
                newUserInfo.setCreateTimeStampNow();
            } else if(existUserList.size() == 1) {
                newUserInfo = existUserList.get(0);
            } else {
                return new ResponseWrapper<>(ResponseHeader.WRONG_IN_DATABASE_EXCEPTION, null);
            }
            
            newUserInfo.setStudentId((String) (tmp.get("gaeinNo")));
            newUserInfo.setName((String) (tmp.get("userNm")));
            newUserInfo.setEntranceYear(Integer.getInteger((String) (tmp.get("iphakYear"))));
            newUserInfo.permissionSetUser(); //TODO status 얻어오기
            
            newUserInfo.setNewToken();
            newUserInfo.setApprovalTimeStampNow();
            
            outputResponse = userRepository.save(newUserInfo);
            
            if(bos != null) bos.close();
            if(in != null) in.close();            
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseWrapper<>(ResponseHeader.WRONG_IN_CONNECTION_EXCEPTION, null);
        }
        return new ResponseWrapper<>(ResponseHeader.OK, outputResponse);
    }
    
    
}
