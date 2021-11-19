package com.belieme.server.web.controller;

import com.belieme.server.web.common.*;

import com.belieme.server.domain.university.*;
import com.belieme.server.domain.department.*;
import com.belieme.server.domain.major.*;
import com.belieme.server.domain.user.*;
import com.belieme.server.domain.permission.*;
import com.belieme.server.domain.thing.*;
import com.belieme.server.domain.item.*;
import com.belieme.server.domain.event.*;

import com.belieme.server.data.common.*;
import com.belieme.server.data.university.*;
import com.belieme.server.data.department.*;
import com.belieme.server.data.major.*;
import com.belieme.server.data.user.*;
import com.belieme.server.data.permission.*;
import com.belieme.server.data.thing.*;
import com.belieme.server.data.item.*;
import com.belieme.server.data.event.*;

public class ApiController {
    private UniversityDao univDao = null;
    private DepartmentDao deptDao = null;
    private MajorDao majorDao = null;
    private UserDao userDao = null;
    private PermissionDao permissionDao = null;
    private ThingDao thingDao = null;
    private ItemDao itemDao = null;
    private EventDao eventDao = null;
    
    protected DataAdapter dataAdapter;
    protected JsonBodyProjector jsonBodyProjector;
    
    private void setControllers(UniversityRepository univRepo, DepartmentRepository deptRepo, MajorRepository majorRepo, UserRepository userRepo, PermissionRepository permissionRepo, ThingRepository thingRepo, ItemRepository itemRepo, EventRepository eventRepo) {
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
        
        this.dataAdapter = new DataAdapter(univDao, deptDao, majorDao, userDao, permissionDao, thingDao, itemDao, eventDao);
        this.jsonBodyProjector = new JsonBodyProjector(dataAdapter);
    }
    
    protected ApiController(UniversityRepository univRepo, DepartmentRepository deptRepo, MajorRepository majorRepo, UserRepository userRepo, PermissionRepository permissionRepo, ThingRepository thingRepo, ItemRepository itemRepo, EventRepository eventRepo) {
        setControllers(univRepo, deptRepo, majorRepo, userRepo, permissionRepo, thingRepo, itemRepo, eventRepo);
    }
}