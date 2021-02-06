package com.belieme.server.web;

import java.net.URI;
import java.util.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.belieme.server.data.RepositoryManager;
import com.belieme.server.data.department.*;
import com.belieme.server.data.event.*;
import com.belieme.server.data.item.*;
import com.belieme.server.data.major.*;
import com.belieme.server.data.permission.*;
import com.belieme.server.data.thing.*;
import com.belieme.server.data.university.*;
import com.belieme.server.data.user.*;

import com.belieme.server.domain.department.DepartmentDao;
import com.belieme.server.domain.event.EventDao;
import com.belieme.server.domain.exception.*;
import com.belieme.server.domain.item.ItemDao;
import com.belieme.server.domain.major.MajorDao;
import com.belieme.server.domain.permission.PermissionDao;
import com.belieme.server.domain.thing.ThingDao;
import com.belieme.server.domain.university.*;
import com.belieme.server.domain.user.UserDao;
import com.belieme.server.web.common.*;
import com.belieme.server.web.exception.*;
import com.belieme.server.web.jsonbody.*;


@RestController
@RequestMapping("/univs")
public class UniversityApiController {
    @Autowired
    protected UniversityRepository univRepo;
    
    @Autowired
    protected DepartmentRepository deptRepo;
    
    @Autowired
    protected MajorRepository majorRepo;
    
    @Autowired
    protected UserRepository userRepo;
    
    @Autowired
    protected PermissionRepository permissionRepo;
    
    @Autowired
    protected ThingRepository thingRepo;
    
    @Autowired
    protected ItemRepository itemRepo;
    
    @Autowired
    protected EventRepository eventRepo;
    
    protected UniversityDao univDao = null;
    protected DepartmentDao deptDao = null;
    protected MajorDao majorDao = null;
    protected UserDao userDao = null;
    protected PermissionDao permissionDao = null;
    protected ThingDao thingDao = null;
    protected ItemDao itemDao = null;
    protected EventDao eventDao = null;
    
    protected JsonBodyProjector jsonBodyProjector;
    
    private void setControllers() {
        RepositoryManager repoManager = new RepositoryManager(univRepo, deptRepo, majorRepo, userRepo, permissionRepo, thingRepo, itemRepo, eventRepo);
        if(univDao == null) {
            this.univDao = new UniversityDaoImpl(repoManager);
        }
        if(deptDao == null) {
            this.deptDao = new DepartmentDaoImpl(repoManager);
        }
        if(majorDao == null) {
            this.majorDao = new MajorDaoImpl(repoManager);
        }
        if(userDao == null) {
            this.userDao = new UserDaoImpl(repoManager);
        }
        if(permissionDao == null) {
            this.permissionDao = new PermissionDaoImpl(repoManager);
        }
        if(thingDao == null) {
            this.thingDao = new ThingDaoImpl(repoManager);
        }
        if(itemDao == null) {
            this.itemDao = new ItemDaoImpl(repoManager);
        }
        if(eventDao == null) {
            this.eventDao = new EventDaoImpl(repoManager);
        }
    }
    
    private void setControllersAndJsonBodyProjector() {
        setControllers();
        jsonBodyProjector = new JsonBodyProjector(univDao, deptDao, majorDao, userDao, permissionDao, thingDao, itemDao, eventDao);
    }
    
    public UniversityApiController() {
        setControllersAndJsonBodyProjector();
    }
    
    private UniversityDto toUniversityDto(UniversityJsonBody univJsonBody) {
        UniversityDto output = new UniversityDto();
        output.setCode(univJsonBody.getCode());
        output.setName(univJsonBody.getName());
        output.setApiUrl(univJsonBody.getApiUrl());
        
        return output;
    }
    
    @GetMapping("") 
    public ResponseEntity<ListResponseBody> getUniv() throws ServerDomainException {
        List<UniversityJsonBody> output = getAllUniversitiesAndCastToJsonBody();
        return ResponseEntity.ok(new ListResponseBody(output));
    }
    
    private List<UniversityJsonBody> getAllUniversitiesAndCastToJsonBody() throws ServerDomainException {
        ArrayList<UniversityJsonBody> output = new ArrayList<>();
        List<UniversityDto> univDtoList = univDao.findAllUnivs();
        
        for(int i = 0; i < univDtoList.size(); i++) {
            output.add(jsonBodyProjector.toUniversityJsonBody(univDtoList.get(i)));
        }
        return output;
    }
    
    @GetMapping("/{univCode}") 
    public ResponseEntity<ResponseBody> getUnivByUnivCode(@PathVariable String univCode) throws ServerDomainException {
        UniversityJsonBody output = jsonBodyProjector.toUniversityJsonBody(univDao.findByCode(univCode));
        return ResponseEntity.ok(new ResponseBody(output));
    }
    
    @PostMapping("")
    public ResponseEntity<ResponseBody> postNewUniverity(@RequestBody UniversityJsonBody requestBody) throws HttpException, ServerDomainException {
        if(requestBody.getName() == null || requestBody.getCode() == null) {
            throw new BadRequestException("Request body에 정보가 부족합니다.\n필요한 정보 : name(String), code(String), apiUrl(String)(Optional)");
        }
        
        univDao.save(toUniversityDto(requestBody));
        URI location = Globals.getLocation("/univ/" + requestBody.getCode());
        return ResponseEntity.created(location).body(new ResponseBody(requestBody));
    }
    
    @PatchMapping("/{univCode}")
    public ResponseEntity<ResponseBody> updateUniverity(@PathVariable String univCode, @RequestBody UniversityJsonBody requestBody) throws HttpException, ServerDomainException {
        if(requestBody.getName() == null && requestBody.getCode() == null && requestBody.getApiUrl() == null) {
            throw new BadRequestException("Request body에 정보가 부족합니다.\n필요한 정보 : name(String), code(String), apiUrl(String) 중 최소 하나");
        }
        
        UniversityJsonBody target = jsonBodyProjector.toUniversityJsonBody(univDao.findByCode(univCode));
        if(requestBody.getName() == null) {
            requestBody.setName(target.getName());
        }
        if(requestBody.getCode() == null) {
            requestBody.setCode(target.getCode());
        }
        if(requestBody.getApiUrl() == null) {
            requestBody.setApiUrl(target.getApiUrl());
        }
        
        univDao.update(univCode, toUniversityDto(requestBody));
        return ResponseEntity.ok(new ResponseBody(requestBody)); 
    }
    
    public class ResponseBody {
        private UniversityJsonBody univ;
        
        public ResponseBody(UniversityJsonBody univ) {
            this.univ = univ;
        }

        public UniversityJsonBody getUniv() {
            return univ;
        }
    }
    
    public class ListResponseBody {
        List<UniversityJsonBody> univs;

        public ListResponseBody(List<UniversityJsonBody> univs) {
            this.univs = univs;
        }

        public List<UniversityJsonBody> getUniversity() {
            return univs;
        }
    }
}