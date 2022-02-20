package com.belieme.server.web.common;

import java.util.List;

import com.belieme.server.domain.university.*;
import com.belieme.server.domain.department.*;
import com.belieme.server.domain.history.*;
import com.belieme.server.domain.major.*;
import com.belieme.server.domain.user.*;
import com.belieme.server.domain.permission.*;
import com.belieme.server.domain.thing.*;
import com.belieme.server.domain.item.*;

import com.belieme.server.domain.exception.*;
import com.belieme.server.web.exception.*;

public class DataAdapter { // TODO 이름 바꾸기...(8)
    private UniversityDao univDao;
    private DepartmentDao deptDao;
    private MajorDao majorDao;
    private UserDao userDao;
    private PermissionDao permissionDao;
    private ThingDao thingDao;
    private ItemDao itemDao;
    private HistoryDao historyDao;
    
    public DataAdapter(UniversityDao univDao, DepartmentDao deptDao, MajorDao majorDao, UserDao userDao, PermissionDao permissionDao, ThingDao thingDao, ItemDao itemDao, HistoryDao historyDao) {
        this.univDao = univDao;
        this.deptDao = deptDao;
        this.majorDao = majorDao;
        this.userDao = userDao;
        this.permissionDao = permissionDao;
        this.thingDao = thingDao;
        this.itemDao = itemDao;
        this.historyDao = historyDao;
    }
    
    public List<UniversityDto> findAllUnivs() {
        return univDao.findAllUnivs();
    }
    
    public UniversityDto findUnivByCode(String code) throws NotFoundException, InternalServerErrorException {
        try {
            return univDao.findByCode(code);    
        } catch (NotFoundOnServerException e) {
			throw new NotFoundException(e);
		}catch(InternalDataBaseException e1) {
            throw new InternalServerErrorException(e1);
        }
    }
    
    public UniversityDto saveUniv(UniversityDto univ) throws ConflictException {
        try {
			return univDao.save(univ);
		} catch (CodeDuplicationException e) {
			throw new ConflictException(e);
		}
    }
    
    public UniversityDto updateUniv(String code, UniversityDto univ) throws NotFoundException, InternalServerErrorException, ConflictException {
		try {
			return univDao.update(code, univ);
		} catch (NotFoundOnServerException e) {
			throw new NotFoundException(e);
		} catch (InternalDataBaseException e) {
			throw new InternalServerErrorException(e);
		} catch (CodeDuplicationException e) {
			throw new ConflictException(e);
		} 
    }
    
    public List<DepartmentDto> findAllDeptsByUnivCode(String univCode) throws InternalServerErrorException {
    	try {
			return deptDao.findAllByUnivCode(univCode);
		} catch (InternalDataBaseException e) {
			throw new InternalServerErrorException(e);
		}
    }
    
    public DepartmentDto findDeptByUnivCodeAndDeptCode(String univCode, String deptCode) throws NotFoundException, InternalServerErrorException {
    	try {
			return deptDao.findByUnivCodeAndDeptCode(univCode, deptCode);
		} catch (NotFoundOnServerException e) {
			throw new NotFoundException(e);
		} catch (InternalDataBaseException e) {
			throw new InternalServerErrorException(e);
		}
    }
    
    public DepartmentDto saveDept(DepartmentDto dept) throws InternalServerErrorException, MethodNotAllowedException, ConflictException {
    	try {
			return deptDao.save(dept);
		}  catch (InternalDataBaseException e) {
			throw new InternalServerErrorException(e);
		} catch (BreakDataBaseRulesException e) {
			throw new MethodNotAllowedException(e);
		} catch (CodeDuplicationException e) {
			throw new ConflictException(e);
		}
    }
    
    public DepartmentDto updateDept(String univCode, String deptCode, DepartmentDto dept) throws NotFoundException, InternalServerErrorException, MethodNotAllowedException, ConflictException {
    	try {
			return deptDao.update(univCode, deptCode, dept);
		} catch (NotFoundOnServerException e) {
			throw new NotFoundException(e);
		} catch (InternalDataBaseException e) {
			throw new InternalServerErrorException(e);
		} catch (BreakDataBaseRulesException e) {
			throw new MethodNotAllowedException(e);
		} catch (CodeDuplicationException e) {
			throw new ConflictException(e);
		}
    }
    
    public List<MajorDto> findAllMajorsByUnivCode(String univCode) throws InternalServerErrorException {
    	try {
			return majorDao.findAllByUnivCode(univCode);
		} catch (InternalDataBaseException e) {
			throw new InternalServerErrorException(e);
		}
    }
    
    public List<MajorDto> findAllMajorsByUnivCodeAndDeptCode(String univCode, String deptCode) throws InternalServerErrorException {
    	try {
			return majorDao.findAllByUnivCodeAndDeptCode(univCode, deptCode);
		} catch (InternalDataBaseException e) {
			throw new InternalServerErrorException(e);
		}
    }
    
    public MajorDto findMajorByUnivCodeAndMajorCode(String univCode, String majorCode) throws NotFoundException, InternalServerErrorException {
    	try {
			return majorDao.findByUnivCodeAndMajorCode(univCode, majorCode);
		} catch (NotFoundOnServerException e) {
			throw new NotFoundException(e);
		}catch (InternalDataBaseException e) {
			throw new InternalServerErrorException(e);
		}
    }
    
    public MajorDto saveMajor(MajorDto major) throws InternalServerErrorException, MethodNotAllowedException, ConflictException {
    	try {
			return majorDao.save(major);
		} catch (InternalDataBaseException e) {
			throw new InternalServerErrorException(e);
		} catch (BreakDataBaseRulesException e) {
			throw new MethodNotAllowedException(e);
		} catch (CodeDuplicationException e) {
			throw new ConflictException(e);
		}
    }
    
    public MajorDto updateMajor(String univCode, String majorCode, MajorDto major) throws NotFoundException, InternalServerErrorException, MethodNotAllowedException, ConflictException {
    	try {
			return majorDao.update(univCode, majorCode, major);
		} catch (NotFoundOnServerException e) {
			throw new NotFoundException(e);
		} catch (InternalDataBaseException e) {
			throw new InternalServerErrorException(e);
		} catch (BreakDataBaseRulesException e) {
			throw new MethodNotAllowedException(e);
		} catch (CodeDuplicationException e) {
			throw new ConflictException(e);
		}
    }
    
    public List<UserDto> findAllUsersByUnivCodeAndDeptCode(String univCode, String deptCode) throws InternalServerErrorException {
    	try {
			return userDao.findAllByUnivCodeAndDeptCode(univCode, deptCode);
		} catch (InternalDataBaseException e) {
			throw new InternalServerErrorException(e);
		}
    }
    
    public UserDto findUserByUnivCodeAndStudentId(String univCode, String studentId) throws NotFoundException, InternalServerErrorException {
    	try {
			return userDao.findByUnivCodeAndStudentId(univCode, studentId);
		} catch (NotFoundOnServerException e) {
			throw new NotFoundException(e);
		} catch (InternalDataBaseException e) {
			throw new InternalServerErrorException(e);
		}
    }
    
    public UserDto findUserByToken(String token) throws InternalServerErrorException, UnauthorizedException {
    	if(token == null) {
			throw new UnauthorizedException("인증이 진행되지 않았습니다. user-token을 header로 전달해 주시길 바랍니다.");//TODO Exception message체계에 변화가 필요함...(1)
		}
		try {
			return userDao.findByToken(token);
		} catch (NotFoundOnServerException e) {
			throw new UnauthorizedException(e);
		} catch (InternalDataBaseException e) {
			throw new InternalServerErrorException(e);
		} catch (TokenExpiredException e) {
			throw new UnauthorizedException(e);
		}
    }
    
    public UserDto saveUser(UserDto user) throws InternalServerErrorException, MethodNotAllowedException, ConflictException {
    	try {
			return userDao.save(user);
		} catch (InternalDataBaseException e) {
			throw new InternalServerErrorException(e);
		} catch (BreakDataBaseRulesException e) {
			throw new MethodNotAllowedException(e);
		} catch (CodeDuplicationException e) {
			throw new ConflictException(e);
		}
    }
    
    public UserDto updateUser(String univCode, String studentId, UserDto user) throws NotFoundException, InternalServerErrorException, MethodNotAllowedException, ConflictException {
    	try {
			return userDao.update(univCode, studentId, user);
		} catch (NotFoundOnServerException e) {
			throw new NotFoundException(e);
		} catch (InternalDataBaseException e) {
			throw new InternalServerErrorException(e);
		} catch (BreakDataBaseRulesException e) {
			throw new MethodNotAllowedException(e);
		} catch (CodeDuplicationException e) {
			throw new ConflictException(e);
		}
    }
    
    public List<PermissionDto> findAllPermissionsByUnivCodeAndStudentId(String univCode, String studentId) throws InternalServerErrorException {
    	try {
			return permissionDao.findAllByUnivCodeAndStudentId(univCode, studentId);
		} catch (InternalDataBaseException e) {
			throw new InternalServerErrorException(e);
		}
    }
    
    public PermissionDto findPermissionByUnivCodeAndStudentIdAndDeptCode(String univCode, String studentId, String deptCode) throws NotFoundException, InternalServerErrorException {
    	try {
			return permissionDao.findByUnivCodeAndStudentIdAndDeptCode(univCode, studentId, deptCode);
		} catch (NotFoundOnServerException e) {
			throw new NotFoundException(e);
		} catch (InternalDataBaseException e) {
			throw new InternalServerErrorException(e);
		}
    }
    
    public PermissionDto savePermission(PermissionDto permission) throws InternalServerErrorException, MethodNotAllowedException, ConflictException {
    	try {
			return permissionDao.save(permission);
		} catch (InternalDataBaseException e) {
			throw new InternalServerErrorException(e);
		} catch (BreakDataBaseRulesException e) {
			throw new MethodNotAllowedException(e);
		} catch (CodeDuplicationException e) {
			throw new ConflictException(e);
		}
    }
    
    public PermissionDto updatePermission(String univCode, String studentId, String deptCode, PermissionDto permission) throws NotFoundException, InternalServerErrorException, MethodNotAllowedException, ConflictException {
    	try {
			return permissionDao.update(univCode, studentId, deptCode, permission);
		} catch (NotFoundOnServerException e) {
			throw new NotFoundException(e);
		} catch (InternalDataBaseException e) {
			throw new InternalServerErrorException(e);
		} catch (BreakDataBaseRulesException e) {
			throw new MethodNotAllowedException(e);
		} catch (CodeDuplicationException e) {
			throw new ConflictException(e);
		}
    }
    
    public List<ThingDto> findAllThingsByUnivCodeAndDeptCode(String univCode, String deptCode) throws InternalServerErrorException {
    	try {
			return thingDao.findAllByUnivCodeAndDeptCode(univCode, deptCode);
		} catch (InternalDataBaseException e) {
			throw new InternalServerErrorException(e);
		}
    }
    
    public ThingDto findThingByUnivCodeAndDeptCodeAndThingCode(String univCode, String deptCode, String thingCode) throws NotFoundException, InternalServerErrorException {
    	try {
			return thingDao.findByUnivCodeAndDeptCodeAndThingCode(univCode, deptCode, thingCode);
		} catch (NotFoundOnServerException e) {
			throw new NotFoundException(e);
		} catch (InternalDataBaseException e) {
			throw new InternalServerErrorException(e);
		}
    }
    
    public ThingDto saveThing(ThingDto thing) throws InternalServerErrorException, MethodNotAllowedException, ConflictException {
    	try {
			return thingDao.save(thing);
		} catch (InternalDataBaseException e) {
			throw new InternalServerErrorException(e);
		} catch (BreakDataBaseRulesException e) {
			throw new MethodNotAllowedException(e);
		} catch (CodeDuplicationException e) {
			throw new ConflictException(e);
		}
    }
    
    public ThingDto updateThing(String univCode, String deptCode, String code, ThingDto user) throws NotFoundException, InternalServerErrorException, MethodNotAllowedException, ConflictException {
    	try {
			return thingDao.update(univCode, deptCode, code, user);
		} catch (NotFoundOnServerException e) {
			throw new NotFoundException(e);
		} catch (InternalDataBaseException e) {
			throw new InternalServerErrorException(e);
		} catch (BreakDataBaseRulesException e) {
			throw new MethodNotAllowedException(e);
		} catch (CodeDuplicationException e) {
			throw new ConflictException(e);
		}
    }
    
    public List<ItemDto> findAllItemsByUnivCodeAndDeptCodeAndThingCode(String univCode, String deptCode, String thingCode) throws InternalServerErrorException {
    	try {
			return itemDao.findAllByUnivCodeAndDeptCodeAndThingCode(univCode, deptCode, thingCode);
		} catch (InternalDataBaseException e) {
			throw new InternalServerErrorException(e);
		}
    }
    public ItemDto findItemByUnivCodeAndDeptCodeAndThingCodeAndItemNum(String univCode, String deptCode, String thingCode, int itemNum) throws NotFoundException, InternalServerErrorException {
    	try {
			return itemDao.findByUnivCodeAndDeptCodeAndThingCodeAndItemNum(univCode, deptCode, thingCode, itemNum);
		} catch (NotFoundOnServerException e) {
			throw new NotFoundException(e);
		} catch (InternalDataBaseException e) {
			throw new InternalServerErrorException(e);
		}
    }
    public ItemDto saveItem(ItemDto item) throws InternalServerErrorException, MethodNotAllowedException, ConflictException {
    	try {
			return itemDao.save(item);
		} catch (InternalDataBaseException e) {
			throw new InternalServerErrorException(e);
		} catch (BreakDataBaseRulesException e) {
			throw new MethodNotAllowedException(e);
		} catch (CodeDuplicationException e) {
			throw new ConflictException(e);
		}
    }
    public ItemDto updateItem(String univCode, String deptCode, String code, int num, ItemDto item) throws NotFoundException, InternalServerErrorException, MethodNotAllowedException, ConflictException {
    	try {
			return itemDao.update(univCode, deptCode, code, num, item);
		} catch (NotFoundOnServerException e) {
			throw new NotFoundException(e);
		} catch (InternalDataBaseException e) {
			throw new InternalServerErrorException(e);
		} catch (BreakDataBaseRulesException e) {
			throw new MethodNotAllowedException(e);
		} catch (CodeDuplicationException e) {
			throw new ConflictException(e);
		}
    }
    
    public List<HistoryDto> findAllHistoriesByUnivCodeAndDeptCode(String univCode, String deptCode) throws InternalServerErrorException {
    	try {
			return historyDao.findAllByUnivCodeAndDeptCode(univCode, deptCode);
		} catch (InternalDataBaseException e) {
			throw new InternalServerErrorException(e);
		}
    }
    
    public List<HistoryDto> findAllHistoriesByUnivCodeAndDeptCodeAndUserId(String univCode, String deptCode, String userId) throws InternalServerErrorException {
    	try {
			return historyDao.findAllByUnivCodeAndDeptCodeAndUserId(univCode, deptCode, userId);
		} catch (InternalDataBaseException e) {
			throw new InternalServerErrorException(e);
		}
    }
    
    public List<HistoryDto> findAllHistoriesByUnivCodeAndDeptCodeAndThingCodeAndItemNum(String univCode, String deptCode, String thingCode, int itemNum) throws InternalServerErrorException {
    	try {
			return historyDao.findAllByUnivCodeAndDeptCodeAndThingCodeAndItemNum(univCode, deptCode, thingCode, itemNum);
		} catch (InternalDataBaseException e) {
			throw new InternalServerErrorException(e);
		}
    }
    
    public HistoryDto findHistoryByUnivCodeAndDeptCodeAndThingCodeAndItemNumAndHistoryNum(String univCode, String deptCode, String thingCode, int itemNum, int historyNum) throws NotFoundException, InternalServerErrorException {
    	try {
			return historyDao.findByUnivCodeAndDeptCodeAndThingCodeAndItemNumAndHistoryNum(univCode, deptCode, thingCode, itemNum, historyNum);
		} catch (NotFoundOnServerException e) {
			throw new NotFoundException(e);
		} catch (InternalDataBaseException e) {
			throw new InternalServerErrorException(e);
		}
    }
    
    public HistoryDto saveHistory(HistoryDto history) throws InternalServerErrorException, MethodNotAllowedException, ConflictException {
    	try {
			return historyDao.save(history);
		} catch (InternalDataBaseException e) {
			throw new InternalServerErrorException(e);
		} catch (BreakDataBaseRulesException e) {
			throw new MethodNotAllowedException(e);
		} catch (CodeDuplicationException e) {
			throw new ConflictException(e);
		}
    }
    public HistoryDto updateHistory(String univCode, String deptCode, String thingCode, int itemNum, int historyNum, HistoryDto history) throws NotFoundException, InternalServerErrorException, MethodNotAllowedException, ConflictException {
    	try {
    		return historyDao.update(univCode, deptCode, thingCode, itemNum, historyNum, history);
    	} catch(NotFoundOnServerException e) {
    		throw new NotFoundException(e);
    	} catch (InternalDataBaseException e) {
			throw new InternalServerErrorException(e);
		} catch (BreakDataBaseRulesException e) {
			throw new MethodNotAllowedException(e);
		} catch (CodeDuplicationException e) {
			throw new ConflictException(e);
		}
    }
}