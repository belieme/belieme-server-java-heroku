package com.belieme.server.web;

import org.springframework.beans.factory.annotation.Autowired;

import com.belieme.server.web.jsonbody.JsonBodyProjector;

import com.belieme.server.domain.university.*;
import com.belieme.server.domain.department.*;
import com.belieme.server.domain.major.*;
import com.belieme.server.domain.user.*;
import com.belieme.server.domain.permission.*;
import com.belieme.server.domain.thing.*;
import com.belieme.server.domain.item.*;
import com.belieme.server.domain.event.*;

import com.belieme.server.data.*;
import com.belieme.server.data.university.*;
import com.belieme.server.data.department.*;
import com.belieme.server.data.major.*;
import com.belieme.server.data.user.*;
import com.belieme.server.data.permission.*;
import com.belieme.server.data.thing.*;
import com.belieme.server.data.item.*;
import com.belieme.server.data.event.*;

public class ApiController {
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
    
    protected ApiController() {
        setControllersAndJsonBodyProjector();
    }
}