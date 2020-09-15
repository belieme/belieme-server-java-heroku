package com.hanyang.belieme.demoserver.department;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import com.hanyang.belieme.demoserver.common.ResponseHeader;
import com.hanyang.belieme.demoserver.common.ResponseWrapper;
import com.hanyang.belieme.demoserver.department.major.MajorRepository;
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
@RequestMapping("/univs/{univCode}/depts")
public class DepartmentApiController {
    @Autowired
    private UniversityRepository universityRepository;
    
    @Autowired
    private DepartmentRepository departmentRepository;
    
    @Autowired
    private MajorRepository majorRepository;
    
    @GetMapping("")
    public ResponseWrapper<Iterable<Department>> getDepartments(@PathVariable String univCode) {
        try {
            int univId = University.findIdByUnivCode(universityRepository, univCode);
            
            List<Department> output = new ArrayList<>();
            Iterator<DepartmentDB> iterator = departmentRepository.findByUniversityId(univId).iterator();
            while(iterator.hasNext()) {
                output.add(iterator.next().toDepartment(universityRepository, majorRepository));
            }
            return new ResponseWrapper<>(ResponseHeader.OK, output);
        } catch(NotFoundException e) {
            return new ResponseWrapper<>(ResponseHeader.NOT_FOUND_EXCEPTION, null);
        } catch(WrongInDataBaseException e) {
            return new ResponseWrapper<>(ResponseHeader.WRONG_IN_DATABASE_EXCEPTION, null);
        }
    }
    
    @GetMapping("/{deptCode}")
    public ResponseWrapper<Department> getDepartment(@PathVariable String univCode, @PathVariable String deptCode) {
        try {
            int id = Department.findIdByUnivCodeAndDeptCode(universityRepository, departmentRepository, univCode, deptCode);
            Optional<DepartmentDB> departmentOptional = departmentRepository.findById(id);
            if(departmentOptional.isPresent()) {
                return new ResponseWrapper<>(ResponseHeader.OK, departmentOptional.get().toDepartment(universityRepository, majorRepository));
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
        if(requestBody.getCode() == null || requestBody.getName() == null) {
            return new ResponseWrapper<Department>(ResponseHeader.LACK_OF_REQUEST_BODY_EXCEPTION, null);
        }
        int univId;
        try {
            univId = University.findIdByUnivCode(universityRepository, univCode);
        } catch(NotFoundException e) {
            return new ResponseWrapper<>(ResponseHeader.NOT_FOUND_EXCEPTION, null);
        } catch(WrongInDataBaseException e) {
            return new ResponseWrapper<>(ResponseHeader.WRONG_IN_DATABASE_EXCEPTION, null);
        }
        
        List<DepartmentDB> departmentListByUnivId = departmentRepository.findByUniversityId(univId);
        for(int i = 0; i < departmentListByUnivId.size(); i++) {
            if(departmentListByUnivId.get(i).getCode().equals(requestBody.getCode())) {
                return new ResponseWrapper<>(ResponseHeader.DUPLICATE_CODE_EXCEPTION, null);
            }
        }
        
        DepartmentDB newDepartmentDB = requestBody.toDepartmentDB();
        newDepartmentDB.setUniversityId(univId);
        newDepartmentDB.able();// TODO default는 활성화? 비활성화?
        Department output = departmentRepository.save(newDepartmentDB).toDepartment(universityRepository, majorRepository);
        return new ResponseWrapper<Department>(ResponseHeader.OK, output);
    }
    
    @PatchMapping("/{deptCode}")
    public ResponseWrapper<Department> updateDepartment(@PathVariable String univCode, @PathVariable String deptCode, @RequestBody Department requestBody) {
        if(requestBody.getName() == null && requestBody.getCode() == null) {
            return new ResponseWrapper<>(ResponseHeader.LACK_OF_REQUEST_BODY_EXCEPTION, null);
        }
        
        int id;
        try {
            id = Department.findIdByUnivCodeAndDeptCode(universityRepository, departmentRepository, univCode, deptCode);    
        } catch(NotFoundException e) {
            return new ResponseWrapper<>(ResponseHeader.NOT_FOUND_EXCEPTION, null);
        } catch(WrongInDataBaseException e) {
            return new ResponseWrapper<>(ResponseHeader.WRONG_IN_DATABASE_EXCEPTION, null);
        }
        
        int univId;
        try {
            univId = University.findIdByUnivCode(universityRepository, univCode);
        } catch(NotFoundException e) {
            return new ResponseWrapper<>(ResponseHeader.NOT_FOUND_EXCEPTION, null);
        } catch(WrongInDataBaseException e) {
            return new ResponseWrapper<>(ResponseHeader.WRONG_IN_DATABASE_EXCEPTION, null);
        }
        
        Optional<DepartmentDB> targetOptional = departmentRepository.findById(id);
        if(targetOptional.isPresent()) {
            DepartmentDB target = targetOptional.get();
            if(requestBody.getCode() != null && !requestBody.getCode().equals(deptCode)) {
                List<DepartmentDB> departmentListByUnivId = departmentRepository.findByUniversityId(univId);
                for(int i = 0; i < departmentListByUnivId.size(); i++) {
                    if(requestBody.getCode().equals(departmentListByUnivId.get(i).getCode())) {
                        return new ResponseWrapper<>(ResponseHeader.DUPLICATE_CODE_EXCEPTION, null);
                    }
                }
                target.setCode(requestBody.getCode());
            } 
            if(requestBody.getName() != null && !requestBody.getName().equals(target.getName())) {
               target.setName(requestBody.getName());
            }
            Department output = departmentRepository.save(target).toDepartment(universityRepository, majorRepository);
            return new ResponseWrapper<>(ResponseHeader.OK, output);
        } else {
            return new ResponseWrapper<>(ResponseHeader.NOT_FOUND_EXCEPTION, null);
        }
    }
    
    //TODO 활성화/비활성화 patch
}