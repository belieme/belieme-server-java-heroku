package com.hanyang.belieme.demoserver.department;

import java.util.ArrayList;
import java.util.Iterator;
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
import org.springframework.web.bind.annotation.PatchMapping;
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
            int univId = University.findIdByUniversityCode(universityRepository, univCode);
            
            List<Department> output = new ArrayList<>();
            Iterator<DepartmentDB> iterator = departmentRepository.findByUniversityId(univId).iterator();
            while(iterator.hasNext()) {
                output.add(iterator.next().toDepartment(universityRepository));
            }
            return new ResponseWrapper<>(ResponseHeader.OK, output);
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
            Optional<DepartmentDB> departmentOptional = departmentRepository.findById(id);
            if(departmentOptional.isPresent()) {
                return new ResponseWrapper<>(ResponseHeader.OK, departmentOptional.get().toDepartment(universityRepository));
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
        int univId;
        try {
            univId = University.findIdByUniversityCode(universityRepository, univCode);
        } catch(NotFoundException e) {
            return new ResponseWrapper<>(ResponseHeader.NOT_FOUND_EXCEPTION, null);
        } catch(WrongInDataBaseException e) {
            return new ResponseWrapper<>(ResponseHeader.WRONG_IN_DATABASE_EXCEPTION, null);
        }
        
        List<DepartmentDB> departmentListByUnivId = departmentRepository.findByUniversityId(univId);
        for(int i = 0; i < departmentListByUnivId.size(); i++) {
            if(departmentListByUnivId.get(i).getDepartmentCode().equals(requestBody.getDepartmentCode())) {
                return new ResponseWrapper<>(ResponseHeader.DUPLICATE_CODE_EXCEPTION, null);
            }
        }
        
        DepartmentDB newDepartmentDB = requestBody.toDepartmentDB();
        newDepartmentDB.setUniversityId(univId);
        newDepartmentDB.setMajorCodes(new ArrayList<String>());
        //newDepartmentDB.able();// TODO default는 무엇인가...
        Department output = departmentRepository.save(newDepartmentDB).toDepartment(universityRepository);
        return new ResponseWrapper<Department>(ResponseHeader.OK, output);
    }
    
    @PatchMapping("/{departmentCode}")
    public ResponseWrapper<Department> updateDepartment(@PathVariable String univCode, @PathVariable String departmentCode, @RequestBody Department requestBody) {
        if(requestBody.getDepartmentName() == null || requestBody.getDepartmentCode() == null) {
            return new ResponseWrapper<>(ResponseHeader.LACK_OF_REQUEST_BODY_EXCEPTION, null);
        }
        
        int id;
        try {
            id = Department.findIdByUniversityCodeAndDepartmentCode(universityRepository, departmentRepository, univCode, departmentCode);    
        } catch(NotFoundException e) {
            return new ResponseWrapper<>(ResponseHeader.NOT_FOUND_EXCEPTION, null);
        } catch(WrongInDataBaseException e) {
            return new ResponseWrapper<>(ResponseHeader.WRONG_IN_DATABASE_EXCEPTION, null);
        }
        
        int univId;
        try {
            univId = University.findIdByUniversityCode(universityRepository, univCode);
        } catch(NotFoundException e) {
            return new ResponseWrapper<>(ResponseHeader.NOT_FOUND_EXCEPTION, null);
        } catch(WrongInDataBaseException e) {
            return new ResponseWrapper<>(ResponseHeader.WRONG_IN_DATABASE_EXCEPTION, null);
        }
        
        Optional<DepartmentDB> targetOptional = departmentRepository.findById(id);
        if(targetOptional.isPresent()) {
            DepartmentDB target = targetOptional.get();
            if(departmentCode.equals(requestBody.getDepartmentCode())) {
                target.setDepartmentName(requestBody.getDepartmentName());
                Department output = departmentRepository.save(target).toDepartment(universityRepository);
                return new ResponseWrapper<>(ResponseHeader.OK, output);
            }
            List<DepartmentDB> departmentListByUnivId = departmentRepository.findByUniversityId(univId);
            for(int i = 0; i < departmentListByUnivId.size(); i++) {
                if(departmentListByUnivId.get(i).getDepartmentCode().equals(requestBody.getDepartmentCode())) {
                    return new ResponseWrapper<>(ResponseHeader.DUPLICATE_CODE_EXCEPTION, null);
                }
            }
            target.setDepartmentCode(requestBody.getDepartmentCode());
            target.setDepartmentName(requestBody.getDepartmentName());
            Department output = departmentRepository.save(target).toDepartment(universityRepository);
            return new ResponseWrapper<>(ResponseHeader.OK, output);
        } else {
            return new ResponseWrapper<>(ResponseHeader.NOT_FOUND_EXCEPTION, null);
        }
    }
    
    //TODO 활성화/비활성화 patch
    
    //TODO majorList 추가/제거
}