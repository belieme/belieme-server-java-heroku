package com.hanyang.belieme.demoserver.department;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import com.hanyang.belieme.demoserver.common.Globals;
import com.hanyang.belieme.demoserver.department.major.MajorRepository;
import com.hanyang.belieme.demoserver.exception.BadRequestException;
import com.hanyang.belieme.demoserver.exception.HttpException;
import com.hanyang.belieme.demoserver.exception.InternalServerErrorException;
import com.hanyang.belieme.demoserver.exception.MethodNotAllowedException;
import com.hanyang.belieme.demoserver.exception.NotFoundException;
import com.hanyang.belieme.demoserver.university.University;
import com.hanyang.belieme.demoserver.university.UniversityRepository;

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
@RequestMapping("/univs/{univCode}/depts")
public class DepartmentApiController {
    @Autowired
    private UniversityRepository universityRepository;
    
    @Autowired
    private DepartmentRepository departmentRepository;
    
    @Autowired
    private MajorRepository majorRepository;
    
    @GetMapping("")
    public ResponseEntity<ListResponse> getDepartments(@PathVariable String univCode) throws HttpException {
        University univ = University.findByUnivCode(universityRepository, univCode);
        int univId = univ.getId();
        
        List<Department> output = new ArrayList<>();
        Iterator<DepartmentDB> iterator = departmentRepository.findByUniversityId(univId).iterator();
        while(iterator.hasNext()) {
            output.add(iterator.next().toDepartment(majorRepository));
        }
        return ResponseEntity.ok(new ListResponse(univ, output));
    }
    
    @GetMapping("/{deptCode}")
    public ResponseEntity<Response> getDepartment(@PathVariable String univCode, @PathVariable String deptCode) throws HttpException {
        University univ = University.findByUnivCode(universityRepository, univCode);
            
        DepartmentDB dept = DepartmentDB.findByUnivCodeAndDeptCode(universityRepository, departmentRepository, univCode, deptCode);
        int deptId = dept.getId();
        Optional<DepartmentDB> departmentOptional = departmentRepository.findById(deptId);
        if(departmentOptional.isPresent()) {
            return ResponseEntity.ok(new Response(univ, departmentOptional.get().toDepartment(majorRepository)));
        } else {
            throw new NotFoundException("학과 코드가 " + deptCode + "인 학과는 " + univCode + "를 학교 코드로 갖는 학교에서 찾을 수 없습니다.");
        }
    }
    
    @PostMapping("")
    public ResponseEntity<Response> postNewDepartment(@PathVariable String univCode, @RequestBody Department requestBody) throws HttpException {
        if(requestBody.getCode() == null || requestBody.getName() == null) {
            throw new BadRequestException("Request body에 정보가 부족합니다.\n필요한 정보 : code(String), name(String)");
        }
        
        University univ = University.findByUnivCode(universityRepository, univCode);
        int univId = univ.getId();
        
        List<DepartmentDB> departmentListByUnivId = departmentRepository.findByUniversityId(univId);
        for(int i = 0; i < departmentListByUnivId.size(); i++) {
            if(departmentListByUnivId.get(i).getCode().equals(requestBody.getCode())) {
                throw new MethodNotAllowedException("학과 코드가 " + requestBody.getCode() + "인 학과가 " + univCode + "를 학교 코드로 갖는 학교에 이미 존재합니다.");
            }
        }
        
        DepartmentDB newDepartmentDB = requestBody.toDepartmentDB();
        newDepartmentDB.setUniversityId(univId);
        newDepartmentDB.able();// TODO default는 활성화? 비활성화?
        Department output = departmentRepository.save(newDepartmentDB).toDepartment(majorRepository);
        
        URI location;
        try {
            location = new URI(Globals.serverUrl + "/univs/" + univCode + "/depts/" + requestBody.getCode());    
        } catch(URISyntaxException e) {
            e.printStackTrace();
            throw new InternalServerErrorException("안알랴줌");
        }
        
        return ResponseEntity.created(location).body(new Response(univ,output));
    }
    
    @PatchMapping("/{deptCode}")
    public ResponseEntity<Response> updateDepartment(@PathVariable String univCode, @PathVariable String deptCode, @RequestBody Department requestBody) throws HttpException {
        if(requestBody.getName() == null && requestBody.getCode() == null) {
            throw new BadRequestException("Request body에 정보가 부족합니다.\n필요한 정보 : code(String), name(String) 중 최소 하나");
        }
        
        DepartmentDB dept = DepartmentDB.findByUnivCodeAndDeptCode(universityRepository, departmentRepository, univCode, deptCode);    
        int deptId = dept.getId();
        
        University univ = University.findByUnivCode(universityRepository, univCode);
        int univId = univ.getId();
        
        Optional<DepartmentDB> targetOptional = departmentRepository.findById(deptId);
        if(targetOptional.isPresent()) {
            DepartmentDB target = targetOptional.get();
            if(requestBody.getCode() != null && !requestBody.getCode().equals(deptCode)) {
                List<DepartmentDB> departmentListByUnivId = departmentRepository.findByUniversityId(univId);
                for(int i = 0; i < departmentListByUnivId.size(); i++) {
                    if(requestBody.getCode().equals(departmentListByUnivId.get(i).getCode())) {
                        throw new MethodNotAllowedException("학과 코드가 " + requestBody.getCode() + "인 학과가 " + univCode + "를 학교 코드로 갖는 학교에 이미 존재합니다.");
                    }
                }
                target.setCode(requestBody.getCode());
            } 
            if(requestBody.getName() != null && !requestBody.getName().equals(target.getName())) {
               target.setName(requestBody.getName());
            }
            Department output = departmentRepository.save(target).toDepartment(majorRepository);
            return ResponseEntity.ok().body(new Response(univ, output));
        } else {
            throw new NotFoundException("학과 코드가 " + deptCode + "인 학과는 " + univCode + "를 학교 코드로 갖는 학교에서 찾을 수 없습니다.");
        }
    }
    
    //TODO 활성화/비활성화 patch
    
    public class Response {
        University university;
        Department department;

        public Response(University university, Department department) {
            this.university = new University(university);
            this.department = new Department(department);
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
    }
    
    
    public class ListResponse {
        University university;
        ArrayList<Department> departments;

        public ListResponse(University university, List<Department> departments) {
            this.university = university;
            this.departments = new ArrayList<>(departments);
        }

        public University getUniversity() {
            if(university == null) {
                return null;
            }
            return new University(university);
        }

        public List<Department> getDepartments() {
            return new ArrayList<>(departments);
        }
    }
}