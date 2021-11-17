package com.belieme.server.web.common;

import java.util.List;

import com.belieme.server.domain.university.*;
// import com.belieme.server.domain.department.*;
// import com.belieme.server.domain.event.*;
// import com.belieme.server.domain.major.*;
// import com.belieme.server.domain.user.*;
// import com.belieme.server.domain.permission.*;
// import com.belieme.server.domain.thing.*;
// import com.belieme.server.domain.item.*;
import com.belieme.server.data.exception.NotFoundOnDataBaseException;
import com.belieme.server.domain.exception.*;
import com.belieme.server.web.exception.*;

public class DataAdapter {
    private UniversityDao univDao;
    // private DepartmentDao deptDao;
    // private MajorDao majorDao;
    // private UserDao userDao;
    // private PermissionDao permissionDao;
    // private ThingDao thingDao;
    // private ItemDao itemDao;
    // private EventDao eventDao;
    
    public List<UniversityDto> findAllUnivs() {
        return univDao.findAllUnivs();
    }
    
    public UniversityDto findUnivByCode(String code) throws InternalServerErrorException, GoneException {
        try {
            return univDao.findByCode(code);    
        } catch(InternalDataBaseException e1) {
            throw new InternalServerErrorException(e1);
        } catch(NotFoundOnServerException e2) {
            throw new GoneException(e2);
        }
    }
    
    public UniversityDto saveUniv(UniversityDto univ) {
        return univDao.save(univ);
    }
    
    public UniversityDto updateUniv(String code, UniversityDto univ) {
        return univDao.update(code, univ);
    }
}