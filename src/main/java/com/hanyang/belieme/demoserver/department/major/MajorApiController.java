package com.hanyang.belieme.demoserver.department.major;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import com.hanyang.belieme.demoserver.common.Globals;
import com.hanyang.belieme.demoserver.common.ResponseHeader;
import com.hanyang.belieme.demoserver.common.ResponseWrapper;
import com.hanyang.belieme.demoserver.department.DepartmentDB;
import com.hanyang.belieme.demoserver.department.DepartmentRepository;
import com.hanyang.belieme.demoserver.department.Department;
import com.hanyang.belieme.demoserver.exception.BadRequestException;
import com.hanyang.belieme.demoserver.exception.HttpException;
import com.hanyang.belieme.demoserver.exception.InternalServerErrorException;
import com.hanyang.belieme.demoserver.exception.MethodNotAllowedException;
import com.hanyang.belieme.demoserver.exception.NotFoundException;
import com.hanyang.belieme.demoserver.university.University;
import com.hanyang.belieme.demoserver.university.UniversityRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path="/univs/{univCode}/depts/{deptCode}/majors")
public class MajorApiController {
    @Autowired
    UniversityRepository universityRepository;
    
    @Autowired
    DepartmentRepository departmentRepository;
    
    @Autowired
    MajorRepository majorRepository;
    
    @PostMapping("")
    public ResponseEntity<Response> postNewMajor(@PathVariable String univCode, @PathVariable String deptCode, @RequestBody Major requestBody) throws HttpException {
        if(requestBody.getCode() == null) {
            throw new BadRequestException("Request body에 정보가 부족합니다.\n필요한 정보 : code(String)");
        }
        
        University univ = University.findByUnivCode(universityRepository, univCode);
        int univId = univ.getId();
        
        DepartmentDB dept = DepartmentDB.findByUnivCodeAndDeptCode(universityRepository, departmentRepository, univCode, deptCode);
        int deptId = dept.getId();
        
        List<DepartmentDB> departmentsByUnivId = departmentRepository.findByUniversityId(univId);
        List<Major> majorsByUnivId = new ArrayList<Major>();
        for(int i = 0; i < departmentsByUnivId.size(); i++) {
            majorsByUnivId.addAll(majorRepository.findByDepartmentId(departmentsByUnivId.get(i).getId()));
        }
        
        for(int i = 0; i < majorsByUnivId.size(); i++) {
            if(requestBody.getCode().equals(majorsByUnivId.get(i).getCode())) {
                throw new MethodNotAllowedException("세부 학과 코드가 " + requestBody.getCode() + "인 학과가 " + univCode + "를 학교 코드로 갖는 학교에 이미 존재합니다.");
            }
        }
        
        requestBody.setDepartmentId(deptId);
        Major output = majorRepository.save(requestBody);
        
        URI location;
        try {
            location = new URI(Globals.serverUrl + "/univs/" + univCode + "/depts/" + deptCode + "/majors/" + requestBody.getCode());    
        } catch(URISyntaxException e) {
            e.printStackTrace();
            throw new InternalServerErrorException("안알랴줌");
        }
        
        return ResponseEntity.created(location).body(new Response(univ, dept.toDepartment(majorRepository), output.toMajorResponse()));
    }
    
    public class Response {
        University university;
        Department department;
        MajorResponse major;

        public Response(University university, Department department, MajorResponse major) {
            this.university = new University(university);
            this.department = new Department(department);
            this.major = new MajorResponse(major);
        }

        public University getUniversity() {
            if(university == null) {
                return null;
            }
            return new University(university);
        }

        public Department getDepartment() {
            if(department == null) {
                return null;
            }
            return new Department(department);
        }
        
        public MajorResponse getMajor() {
            if(major == null) {
                return null;
            }
            return new MajorResponse(major);
        }
    }
}