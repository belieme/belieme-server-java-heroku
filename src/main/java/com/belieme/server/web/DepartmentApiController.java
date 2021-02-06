package com.belieme.server.web;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;


import com.belieme.server.domain.exception.*;
import com.belieme.server.domain.department.*;

import com.belieme.server.web.common.Globals;
import com.belieme.server.web.exception.*;
import com.belieme.server.web.jsonbody.*;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

// TODO advice에 serverDomainException처리도 만들기
// TODO DepartmentJsonBody public 변수에서 private 변수로 바꾸기

@RestController
@RequestMapping("/univs/{univCode}/depts")
public class DepartmentApiController extends ApiController {
    public DepartmentApiController() {
        super();
    }
    
    @GetMapping("")
    public ResponseEntity<ListResponse> getDepartments(@PathVariable String univCode) throws ServerDomainException {
        UniversityJsonBody univOutput = getUniversityByCodeAndCastToJsonBody(univCode);
        
        List<DepartmentJsonBody> deptOutput = getAllDepartmentsByUnivCodeAndCastToJsonBody(univCode);
        return ResponseEntity.ok(new ListResponse(univOutput, deptOutput));
    }
    
    private UniversityJsonBody getUniversityByCodeAndCastToJsonBody(String code) throws ServerDomainException {
        return jsonBodyProjector.toUniversityJsonBody(univDao.findByCode(code));
    }
    
    private List<DepartmentJsonBody> getAllDepartmentsByUnivCodeAndCastToJsonBody(String univCode) throws ServerDomainException {
        ArrayList<DepartmentJsonBody> output = new ArrayList<>();
        List<DepartmentDto> deptDtoList;
        deptDtoList = deptDao.findAllByUnivCode(univCode);    
        
        for(int i = 0; i < deptDtoList.size(); i++) {
            output.add(jsonBodyProjector.toDepartmentJsonBody(deptDtoList.get(i)));
        }
        return output;
    }
    
    
    @GetMapping("/{deptCode}")
    public ResponseEntity<Response> getDepartment(@PathVariable String univCode, @PathVariable String deptCode) throws ServerDomainException {
        UniversityJsonBody univOutput = getUniversityByCodeAndCastToJsonBody(univCode);
        
        DepartmentJsonBody deptOutput = getDepartmentByUnivCodeAndDeptCodeAndCastToJsonBody(univCode, deptCode);
        return ResponseEntity.ok(new Response(univOutput, deptOutput));
    }
    
    private DepartmentJsonBody getDepartmentByUnivCodeAndDeptCodeAndCastToJsonBody(String univCode, String deptCode) throws ServerDomainException {
        return jsonBodyProjector.toDepartmentJsonBody(deptDao.findByUnivCodeAndDeptCode(univCode, deptCode));    
    }
    
    @PostMapping("")
    public ResponseEntity<Response> postNewDepartment(@PathVariable String univCode, @RequestBody DepartmentJsonBody requestBody) throws HttpException, ServerDomainException {
        if(requestBody.code == null || requestBody.name == null) {
            throw new BadRequestException("Request body에 정보가 부족합니다.\n필요한 정보 : code(String), name(String)");
        }
        UniversityJsonBody univOutput = getUniversityByCodeAndCastToJsonBody(univCode);
        
        DepartmentDto savedDept = deptDao.save(toDepartmentDto(univCode, requestBody));
        URI location = Globals.getLocation("/univ/" + requestBody.code);
        
        return ResponseEntity.created(location).body(new Response(univOutput,jsonBodyProjector.toDepartmentJsonBody(savedDept)));
    }
    
    private DepartmentDto toDepartmentDto(String univCode, DepartmentJsonBody jsonBody) {
        DepartmentDto output = new DepartmentDto();
        output.setCode(jsonBody.code);
        output.setName(jsonBody.name);
        output.setUnivCode(univCode);
        output.setAvailable(jsonBody.available);
     
        return output;
    }
    
    @PatchMapping("/{deptCode}")
    public ResponseEntity<Response> updateDepartment(@PathVariable String univCode, @PathVariable String deptCode, @RequestBody DepartmentJsonBody requestBody) throws HttpException, ServerDomainException {
        if(requestBody.name == null && requestBody.code == null) {
            throw new BadRequestException("Request body에 정보가 부족합니다.\n필요한 정보 : code(String), name(String) 중 최소 하나");
        }
        
        UniversityJsonBody univOutput = getUniversityByCodeAndCastToJsonBody(univCode);
        
        
        DepartmentJsonBody target = getDepartmentByUnivCodeAndDeptCodeAndCastToJsonBody(univCode, deptCode);
        if(requestBody.name == null) {
            requestBody.name = target.name;
        }
        if(requestBody.code == null) {
            requestBody.code = target.code;
        }
        
        DepartmentDto savedDept = deptDao.save(toDepartmentDto(univCode, requestBody));
        return ResponseEntity.ok().body(new Response(univOutput, jsonBodyProjector.toDepartmentJsonBody(savedDept)));
    }
    
    //TODO 활성화/비활성화 patch
    
    public class Response {
        UniversityJsonBody university;
        DepartmentJsonBody department;

        public Response(UniversityJsonBody university, DepartmentJsonBody department) {
            this.university = university;
            this.department = department;
        }

        public UniversityJsonBody getUniversity() {
            if(university == null) {
                return null;
            }
            return university;
        }

        public DepartmentJsonBody getDepartment() {
            if(department == null) {
                return null;
            }
            return department;
        }
    }
    
    
    public class ListResponse {
        UniversityJsonBody university;
        ArrayList<DepartmentJsonBody> departments;

        public ListResponse(UniversityJsonBody university, List<DepartmentJsonBody> departments) {
            this.university = university;
            this.departments = new ArrayList<>(departments);
        }

        public UniversityJsonBody getUniversity() {
            if(university == null) {
                return null;
            }
            return university;
        }

        public List<DepartmentJsonBody> getDepartments() {
            return departments;
        }
    }
}