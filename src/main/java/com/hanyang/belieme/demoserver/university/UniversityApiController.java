package com.hanyang.belieme.demoserver.university;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Iterator;
import java.util.Optional;

import com.hanyang.belieme.demoserver.common.Globals;
import com.hanyang.belieme.demoserver.exception.BadRequestException;
import com.hanyang.belieme.demoserver.exception.HttpException;
import com.hanyang.belieme.demoserver.exception.InternalServerErrorException;
import com.hanyang.belieme.demoserver.exception.MethodNotAllowedException;
import com.hanyang.belieme.demoserver.exception.NotFoundException;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/univs")
public class UniversityApiController {
    @Autowired
    UniversityRepository universityRepository;
    
    @GetMapping("") 
    public ResponseEntity<ListResponseBody> getUniversities() {
        return ResponseEntity.ok(new ListResponseBody(universityRepository.findAll()));
    }
    
    @GetMapping("/{univCode}") 
    public ResponseEntity<ResponseBody> getUniversityByUnivCode(@PathVariable String univCode) throws HttpException {
        University univ = University.findByUnivCode(universityRepository, univCode);
        Optional<University> univOptional = universityRepository.findById(univ.getId());
        if(univOptional.isPresent()) {
            return ResponseEntity.ok(new ResponseBody(univOptional.get()));    
        } else {
            throw new NotFoundException("학교 코드가 " + univCode + "인 학교를 찾을 수 없습니다.");
        }    
    }
    
    @PostMapping("")
    public ResponseEntity<ResponseBody> postNewUniverity(@RequestBody University requestBody) throws HttpException {
        if(requestBody.getName() == null || requestBody.getCode() == null) {
            throw new BadRequestException("Request body에 정보가 부족합니다.\n필요한 정보 : name(String), code(String), apiUrl(String)(Optional)");
        } else {
            Iterable<University> universityList = universityRepository.findAll();
            Iterator<University> iter = universityList.iterator();
            while(iter.hasNext()) {
                if(iter.next().getCode().equals(requestBody.getCode())) {
                    throw new MethodNotAllowedException("서버에 학교코드가 " + requestBody.getCode() + "인 학교가 이미 존재합니다.");
                }
            }
            
            URI location;
            try {
                location = new URI(Globals.serverUrl + "/univs");    
            } catch(URISyntaxException e) {
                e.printStackTrace();
                throw new InternalServerErrorException("안알랴줌");
            }
            
            University output = universityRepository.save(requestBody);
            return ResponseEntity.created(location).body(new ResponseBody(output));
        }
    }
    
    @PatchMapping("/{univCode}")
    public ResponseEntity<ResponseBody> updateUniverity(@PathVariable String univCode, @RequestBody University requestBody) throws HttpException {
        if(requestBody.getName() == null && requestBody.getCode() == null && requestBody.getApiUrl() == null) {
            throw new BadRequestException("Request body에 정보가 부족합니다.\n필요한 정보 : name(String), code(String), apiUrl(String) 중 최소 하나");
        }
        University target = University.findByUnivCode(universityRepository, univCode);
        if(requestBody.getName() != null) {
            target.setName(requestBody.getName()); 
        }
        if(requestBody.getCode() != null && !univCode.equals(requestBody.getCode())) {
            Iterable<University> universityList = universityRepository.findAll();
            Iterator<University> iter = universityList.iterator();
            while(iter.hasNext()) {
                if(iter.next().getCode().equals(requestBody.getCode())) {
                    throw new MethodNotAllowedException("서버에 학교코드가 " + requestBody.getCode() + "인 학교가 이미 존재합니다.");
                }
            }
            target.setCode(requestBody.getCode());
        }
        if(requestBody.getApiUrl() != null) {
            target.setApiUrl(requestBody.getApiUrl());
        }
        University output = universityRepository.save(target);
        return ResponseEntity.ok(new ResponseBody(output)); 
    }
    
    public class ResponseBody {
        private University university;
        
        public ResponseBody(University university) {
            this.university = new University(university);
        }

        public University getUniversity() {
            if(university == null) {
                return null;
            }
            return new University(university);
        }
    }
    
    public class ListResponseBody {
        Iterable<University> univs;

        public ListResponseBody(Iterable<University> univs) {
            this.univs = univs;
        }

        public Iterable<University> getUniversity() {
            return univs;
        }
    }
    
}