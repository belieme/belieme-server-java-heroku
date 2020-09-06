package com.hanyang.belieme.demoserver.department.major;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.hanyang.belieme.demoserver.common.ResponseHeader;
import com.hanyang.belieme.demoserver.common.ResponseWrapper;
import com.hanyang.belieme.demoserver.department.Department;
import com.hanyang.belieme.demoserver.department.DepartmentDB;
import com.hanyang.belieme.demoserver.department.DepartmentRepository;
import com.hanyang.belieme.demoserver.exception.NotFoundException;
import com.hanyang.belieme.demoserver.exception.WrongInDataBaseException;
import com.hanyang.belieme.demoserver.university.University;
import com.hanyang.belieme.demoserver.university.UniversityRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path="/universities/{univCode}/departments/{departmentCode}/majors")
public class MajorApiController {
    @Autowired
    UniversityRepository universityRepository;
    
    @Autowired
    DepartmentRepository departmentRepository;
    
    @Autowired
    MajorRepository majorRepository;
    
    @PostMapping("")
    public ResponseWrapper<Major> postNewMajor(@PathVariable String univCode, @PathVariable String departmentCode, @RequestBody Major requestBody) {
        if(requestBody.getMajorCode() == null) {
            return new ResponseWrapper<>(ResponseHeader.LACK_OF_REQUEST_BODY_EXCEPTION, null);
        }
        
        int univId;
        try {
            univId = University.findIdByUniversityCode(universityRepository, univCode);
        } catch(NotFoundException e) {
            return new ResponseWrapper<>(ResponseHeader.NOT_FOUND_EXCEPTION, null);
        } catch(WrongInDataBaseException e) {
            return new ResponseWrapper<>(ResponseHeader.WRONG_IN_DATABASE_EXCEPTION, null);
        }
        
        
        int departmentId;
        try {
            departmentId = Department.findIdByUniversityCodeAndDepartmentCode(universityRepository, departmentRepository, univCode, departmentCode);
        } catch(NotFoundException e) {
            return new ResponseWrapper<>(ResponseHeader.NOT_FOUND_EXCEPTION, null);
        } catch(WrongInDataBaseException e) {
            return new ResponseWrapper<>(ResponseHeader.WRONG_IN_DATABASE_EXCEPTION, null);
        }
        
        List<DepartmentDB> departmentsByUnivId = departmentRepository.findByUniversityId(univId);
        List<Major> majorsByUnivId = new ArrayList<Major>();
        for(int i = 0; i < departmentsByUnivId.size(); i++) {
            majorsByUnivId.addAll(majorRepository.findByDepartmentId(departmentsByUnivId.get(i).getId()));
        }
        
        for(int i = 0; i < majorsByUnivId.size(); i++) {
            if(requestBody.getMajorCode().equals(majorsByUnivId.get(i).getMajorCode())) {
                return new ResponseWrapper<>(ResponseHeader.DUPLICATE_CODE_EXCEPTION, null);
            }
        }
        
        requestBody.setDepartmentId(departmentId);
        Major output = majorRepository.save(requestBody);
        return new ResponseWrapper<>(ResponseHeader.OK, output);
    }
}