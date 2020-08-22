package com.hanyang.belieme.demoserver.university;

import java.util.Optional;

import com.hanyang.belieme.demoserver.common.ResponseHeader;
import com.hanyang.belieme.demoserver.common.ResponseWrapper;
import com.hanyang.belieme.demoserver.exception.NotFoundException;
import com.hanyang.belieme.demoserver.exception.WrongInDataBaseException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("universities")
public class UniversityApiController {
    @Autowired
    UniversityRepository universityRepository;
    
    @GetMapping("") 
    public ResponseWrapper<Iterable<University>> getUniversities() {
        return new ResponseWrapper<>(ResponseHeader.OK, universityRepository.findAll());
    }
    
    @GetMapping("/{univCode}") 
    public ResponseWrapper<University> getUniversityByUnivCode(@PathVariable String univCode) {
        try {
            int id = University.findIdByUniversityCode(universityRepository, univCode);
            Optional<University> univOptional = universityRepository.findById(id);
            if(univOptional.isPresent()) {
                return new ResponseWrapper<>(ResponseHeader.OK, univOptional.get());    
            } else {
                return new ResponseWrapper<>(ResponseHeader.NOT_FOUND_EXCEPTION, null);
            }
            
        } catch(NotFoundException e) {
            return new ResponseWrapper<>(ResponseHeader.NOT_FOUND_EXCEPTION, null);
        } catch(WrongInDataBaseException e) {
            return new ResponseWrapper<>(ResponseHeader.WRONG_IN_DATABASE_EXCEPTION, null);
        }
    }
    
    @PostMapping("")
    public ResponseWrapper<University> postNewUniverity(@RequestBody University requestBody) {
        if(requestBody.getName() == null || requestBody.getUniversityCode() == null) {
            return new ResponseWrapper<>(ResponseHeader.LACK_OF_REQUEST_BODY_EXCEPTION, null);
        } else {
            University output = universityRepository.save(requestBody);
            return new ResponseWrapper<>(ResponseHeader.OK, output);
        }
    }
    
    @PutMapping("/{id}")
    public ResponseWrapper<University> updateUniverity(@PathVariable int id, @RequestBody University requestBody) {
        if(requestBody.getName() == null || requestBody.getUniversityCode() == null) {
            return new ResponseWrapper<University>(ResponseHeader.LACK_OF_REQUEST_BODY_EXCEPTION, null);
        } else {
            Optional<University> targetOptional = universityRepository.findById(id);
            if(targetOptional.isPresent()) {
                University target = targetOptional.get();
                target.setUniversityCode(requestBody.getUniversityCode());
                target.setName(requestBody.getName());
                University output = universityRepository.save(target);
                return new ResponseWrapper<>(ResponseHeader.OK, output);    
            } else {
                return new ResponseWrapper<>(ResponseHeader.NOT_FOUND_EXCEPTION, null);
            }
        }
    }
}