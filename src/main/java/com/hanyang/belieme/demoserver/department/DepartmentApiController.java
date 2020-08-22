package com.hanyang.belieme.demoserver.department;

import java.util.List;
import java.util.Optional;

import com.hanyang.belieme.demoserver.common.ResponseHeader;
import com.hanyang.belieme.demoserver.common.ResponseWrapper;
import com.hanyang.belieme.demoserver.exception.NotFoundException;
import com.hanyang.belieme.demoserver.exception.WrongInDataBaseException;
import com.hanyang.belieme.demoserver.university.University;
import com.hanyang.belieme.demoserver.university.UniversityRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("universities/{univCode}/departments")
public class DepartmentApiController {
    @Autowired
    UniversityRepository universityRepository;
    
    @Autowired
    DepartmentRepository departmentRepository;
    
    @GetMapping("")
    public ResponseWrapper<Iterable<Department>> getDepartments(@PathVariable String univCode) {
        try {
            int id = University.findIdByUniversityCode(universityRepository, univCode);
            return new ResponseWrapper<>(ResponseHeader.OK, departmentRepository.findByUniversityId(id));
        } catch(NotFoundException e) {
            return new ResponseWrapper<>(ResponseHeader.NOT_FOUND_EXCEPTION, null);
        } catch(WrongInDataBaseException e) {
            return new ResponseWrapper<>(ResponseHeader.WRONG_IN_DATABASE_EXCEPTION, null);
        }
    }
    
    @GetMapping("/{departmentCode}")
    public ResponseWrapper<Department> getDepartment(@PathVariable String univCode, @PathVariable String departmentCode) {
        try {
            int id = Department.findIdByUniversityCodeAndDepartmentCode(universityRepository, departmentRepository, univCode, departmentCode);
            Optional<Department> departmentOptional = departmentRepository.findById(id);
            if(departmentOptional.isPresent()) {
                return new ResponseWrapper<>(ResponseHeader.OK, departmentOptional.get());
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
    public ResponseWrapper<Department> postNewDepartment(@PathVariable String univCode, @RequestBody Department requestBody) {
        if(requestBody.getDepartmentCode() == null || requestBody.getDepartmentName() == null) {
            return new ResponseWrapper<Department>(ResponseHeader.LACK_OF_REQUEST_BODY_EXCEPTION, null);
        }
        try {
            requestBody.setUniversityId(University.findIdByUniversityCode(universityRepository, univCode));
        } catch(NotFoundException e) {
            return new ResponseWrapper<>(ResponseHeader.NOT_FOUND_EXCEPTION, null);
        } catch(WrongInDataBaseException e) {
            return new ResponseWrapper<>(ResponseHeader.WRONG_IN_DATABASE_EXCEPTION, null);
        }
        // requestBody.able(); TODO default는 무엇인가...
        Department output = departmentRepository.save(requestBody);
        return new ResponseWrapper<Department>(ResponseHeader.OK, output);
    }
}