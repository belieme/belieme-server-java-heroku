package com.belieme.server.web;

import java.net.URI;
import java.net.URISyntaxException;

import com.belieme.server.domain.exception.*;
import com.belieme.server.domain.major.*;
import com.belieme.server.domain.university.*;

import com.belieme.server.web.common.*;
import com.belieme.server.web.exception.*;
import com.belieme.server.web.jsonbody.*;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(path="/univs/{univCode}/majors")
public class MajorApiController extends ApiController {
    public MajorApiController() {
        super();
    }
    
    @PostMapping("")
    public ResponseEntity<Response> postNewMajor(@PathVariable String univCode, @RequestBody MajorInfoJsonBody requestBody) throws HttpException, ServerDomainException {
        if(requestBody.getMajorCode() == null || requestBody.getDeptCode() == null) {
            throw new BadRequestException("Request body에 정보가 부족합니다.\n필요한 정보 : majorCode(String), deptCode(String)");
        }
        
        UniversityDto univ = univDao.findByCode(univCode);
        
        MajorDto newMajor = new MajorDto();
        newMajor.setUnivCode(univ.getCode());
        newMajor.setCode(requestBody.getMajorCode());
        newMajor.setDeptCode(requestBody.getDeptCode());
        
        MajorDto savedMajor = majorDao.save(newMajor);
        
        URI location;
        try {
            location = new URI(Globals.serverUrl + "/univs/" + univCode + "/majors/" + requestBody.getMajorCode());    
        } catch(URISyntaxException e) {
            e.printStackTrace();
            throw new InternalServerErrorException("안알랴줌");
        }
        
        return ResponseEntity.created(location).body(createResponse(univ, savedMajor));
    }
    
    private Response createResponse(UniversityDto univDto, MajorDto majorDto) throws ServerDomainException {
        UniversityJsonBody univ = jsonBodyProjector.toUniversityJsonBody(univDto);
        MajorJsonBody major = jsonBodyProjector.toMajorJsonBody(majorDto);
        return new Response(univ, major);
    }
    
    public class Response {
        UniversityJsonBody university;
        MajorJsonBody major;

        public Response(UniversityJsonBody university, MajorJsonBody major) {
            this.university = university;
            this.major = major;
        }

        public UniversityJsonBody getUniversity() {
            return university;
        }

        public MajorJsonBody getMajor() {
            return major;
        }
    }
}