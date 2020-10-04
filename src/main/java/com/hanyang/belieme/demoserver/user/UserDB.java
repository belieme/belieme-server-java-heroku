package com.hanyang.belieme.demoserver.user;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.TimeZone;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import com.hanyang.belieme.demoserver.common.IntegerListConverter;
import com.hanyang.belieme.demoserver.department.DepartmentDB;
import com.hanyang.belieme.demoserver.department.DepartmentNestedToUser;
import com.hanyang.belieme.demoserver.department.DepartmentRepository;
import com.hanyang.belieme.demoserver.department.major.Major;
import com.hanyang.belieme.demoserver.department.major.MajorRepository;
import com.hanyang.belieme.demoserver.exception.NotFoundException;
import com.hanyang.belieme.demoserver.exception.WrongInDataBaseException;
import com.hanyang.belieme.demoserver.university.University;
import com.hanyang.belieme.demoserver.university.UniversityRepository;
import com.hanyang.belieme.demoserver.user.permission.PermissionDB;
import com.hanyang.belieme.demoserver.user.permission.PermissionRepository;

@Entity //TODO user에 permission 어케할까?
public class UserDB {
    @Id @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;
    
    private int universityId;
    
    @Convert(converter = IntegerListConverter.class)
    private List<Integer> majorIds;
    
    private String token;
    
    private long createTimeStamp;
    
    private long approvalTimeStamp;
    
    @Column(name = "student_id", nullable = false)
    private String studentId;

    private String name;
    
    private int entranceYear;
    
    public UserDB() {
        majorIds = new ArrayList<Integer>();
    }
    
    public int getId() {
        return id;
    }
    
    public int getUniversityId() {
        return universityId;
    }
    
    public String getToken() {
        return token;
    }
    
    public long getCreateTimeStamp() {
        return createTimeStamp;
    }
    
    public long getApprovalTimeStamp() {
        return approvalTimeStamp;
    }
    
    public List<Integer> getMajorIds() {
        return majorIds;
    }
    
    public String getStudentId() {
        return studentId;
    }

    public String getName() {
        return name;
    }

    public int getEntranceYear() {
        return entranceYear;
    }
    
    public void setCreateTimeStampNow() {
        createTimeStamp = System.currentTimeMillis()/1000;
    }
    
    public void setApprovalTimeStampNow() {
        approvalTimeStamp = System.currentTimeMillis()/1000;
    }
    
    public void setApprovalTimeStampZero() {
        approvalTimeStamp = 0;
    }
    
    public void setNewToken(UserRepository userRepository) {
        this.token = UUID.randomUUID().toString();
        // while(hasDuplicateToken(userRepository)) {
        //     this.token = UUID.randomUUID().toString();
        // }
    }
    
    public void resetToken() {
        this.token = null;
    }
    
    public void setUniversityId(int universityId) {
        this.universityId = universityId;
    }
    
    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }
    
    public void setName(String name) {
        this.name = name;
    }

    public void setEntranceYear(int entranceYear) {
        this.entranceYear = entranceYear;
    }
    
    public User toUser(DepartmentRepository departmentRepository, MajorRepository majorRepository, PermissionRepository permissionRepository) {
        User output = new User();
        output.setId(id);
        output.setUnivId(universityId);
        output.setStudentId(studentId);
        output.setName(name);
        output.setEntranceYear(entranceYear);
        output.setCreateTimeStamp(createTimeStamp);
        output.setApprovalTimeStamp(approvalTimeStamp);
        
        Iterable<Major> majorListByIdList = majorRepository.findAllById(majorIds);
        Iterator<Major> iter = majorListByIdList.iterator();
        
        ArrayList<String> majorCodes = new ArrayList<String>();
        ArrayList<DepartmentNestedToUser> departments = new ArrayList<DepartmentNestedToUser>();
        while(iter.hasNext()) {
            Major tmp = iter.next();
            majorCodes.add(tmp.getCode());
            
            if(departments.size() == 0) {
                Optional<DepartmentDB> tmpDepartmentOptional = departmentRepository.findById(tmp.getDepartmentId());
                if(tmpDepartmentOptional.isPresent()) {
                    departments.add(tmpDepartmentOptional.get().toDepartmentNestedToUser());
                } else {
                    // TODO DB에 없음 이라는 Department를 만들기
                }
                continue;
            }
            for(int i = 0; i < departments.size(); i++) {
                if(tmp.getDepartmentId() != departments.get(i).getId()) {
                    Optional<DepartmentDB> tmpDepartmentOptional = departmentRepository.findById(tmp.getDepartmentId());
                    if(tmpDepartmentOptional.isPresent()) {
                        departments.add(tmpDepartmentOptional.get().toDepartmentNestedToUser());
                    } else {
                        // TODO DB에 없음 이라는 Department를 만들기
                    }
                }
            }
        }
        
        output.setMajorCodes(majorCodes);
        output.setDepartments(departments);
        
        List<PermissionDB> permissions = permissionRepository.findByUserId(id);
        for(int i = 0; i < permissions.size(); i++) {
            output.addPermission(departmentRepository.findById(permissions.get(i).getDeptId()).get().getCode(), permissions.get(i).getPermission()); // TODO null pointer exception 잡아 줘야하는 것인가...
        }
        
        return output;
    }
    
    public UserWithUniversity toUserWithUniversity(UniversityRepository universityRepository, DepartmentRepository departmentRepository, MajorRepository majorRepository, PermissionRepository permissionRepository) {
        UserWithUniversity output = new UserWithUniversity();
        output.setId(id);
        output.setStudentId(studentId);
        output.setName(name);
        output.setEntranceYear(entranceYear);
        output.setCreateTimeStamp(createTimeStamp);
        output.setApprovalTimeStamp(approvalTimeStamp);
        
        Optional<University> universityOptional = universityRepository.findById(universityId);
        output.setUniversity(universityOptional.get());
        
        Iterable<Major> majorListByIdList = majorRepository.findAllById(majorIds);
        Iterator<Major> iter = majorListByIdList.iterator();
        
        ArrayList<String> majorCodes = new ArrayList<String>();
        ArrayList<DepartmentNestedToUser> departments = new ArrayList<DepartmentNestedToUser>();
        while(iter.hasNext()) {
            Major tmp = iter.next();
            majorCodes.add(tmp.getCode());
            
            if(departments.size() == 0) {
                Optional<DepartmentDB> tmpDepartmentOptional = departmentRepository.findById(tmp.getDepartmentId());
                if(tmpDepartmentOptional.isPresent()) {
                    departments.add(tmpDepartmentOptional.get().toDepartmentNestedToUser());
                } else {
                    // TODO DB에 없음 이라는 Department를 만들기
                }
                continue;
            }
            for(int i = 0; i < departments.size(); i++) {
                if(tmp.getDepartmentId() != departments.get(i).getId()) {
                    Optional<DepartmentDB> tmpDepartmentOptional = departmentRepository.findById(tmp.getDepartmentId());
                    if(tmpDepartmentOptional.isPresent()) {
                        departments.add(tmpDepartmentOptional.get().toDepartmentNestedToUser());
                    } else {
                        // TODO DB에 없음 이라는 Department를 만들기
                    }
                }
            }
        }
        
        output.setMajorCodes(majorCodes);
        output.setDepartments(departments);
        
        List<PermissionDB> permissions = permissionRepository.findByUserId(id);
        for(int i = 0; i < permissions.size(); i++) {
            output.addPermission(departmentRepository.findById(permissions.get(i).getDeptId()).get().getCode(), permissions.get(i).getPermission()); // TODO null pointer exception 잡아 줘야하는 것인가...
        }
        
        return output;
    }
    
    public UserWithToken toUserWithToken(UniversityRepository universityRepository, DepartmentRepository departmentRepository, MajorRepository majorRepository, PermissionRepository permissionRepository) {
        UserWithToken output = new UserWithToken();
        output.setId(id);
        output.setStudentId(studentId);
        output.setName(name);
        output.setEntranceYear(entranceYear);
        output.setToken(token);
        output.setCreateTimeStamp(createTimeStamp);
        output.setApprovalTimeStamp(approvalTimeStamp);
        
        Optional<University> universityOptional = universityRepository.findById(universityId);
        output.setUniversity(universityOptional.get());
        
        Iterable<Major> majorListByIdList = majorRepository.findAllById(majorIds);
        Iterator<Major> iter = majorListByIdList.iterator();
        
        ArrayList<String> majorCodes = new ArrayList<String>();
        ArrayList<DepartmentNestedToUser> departments = new ArrayList<DepartmentNestedToUser>();
        while(iter.hasNext()) {
            Major tmp = iter.next();
            majorCodes.add(tmp.getCode());
            
            if(departments.size() == 0) {
                Optional<DepartmentDB> tmpDepartmentOptional = departmentRepository.findById(tmp.getDepartmentId());
                if(tmpDepartmentOptional.isPresent()) {
                    departments.add(tmpDepartmentOptional.get().toDepartmentNestedToUser());
                } else {
                    // TODO DB에 없음 이라는 Department를 만들기
                }
                continue;
            }
            for(int i = 0; i < departments.size(); i++) {
                if(tmp.getDepartmentId() != departments.get(i).getId()) {
                    Optional<DepartmentDB> tmpDepartmentOptional = departmentRepository.findById(tmp.getDepartmentId());
                    if(tmpDepartmentOptional.isPresent()) {
                        departments.add(tmpDepartmentOptional.get().toDepartmentNestedToUser());
                    } else {
                        // TODO DB에 없음 이라는 Department를 만들기
                    }
                }
            }
        }
        
        output.setMajorCodes(majorCodes);
        output.setDepartments(departments);
        
        List<PermissionDB> permissions = permissionRepository.findByUserId(id);
        for(int i = 0; i < permissions.size(); i++) {
            output.addPermission(departmentRepository.findById(permissions.get(i).getDeptId()).get().getCode(), permissions.get(i).getPermission()); // TODO null pointer exception 잡아 줘야하는 것인가...
        }
        
        return output;
    }
    
    public UserNestedToEvent toUserNestedToEvent() {
        UserNestedToEvent output = new UserNestedToEvent();
        output.setId(id);
        output.setStudentId(studentId);
        output.setName(name);
        output.setEntranceYear(entranceYear);
        
        return output;
    }
    
    public boolean hasDuplicateToken(UserRepository userRepository) {
        Iterator<UserDB> allUserIter = userRepository.findAll().iterator();
        
        if(token == null) {
            return false;   
        }
        while(allUserIter.hasNext()) {
            if(token.equals(allUserIter.next().getToken())) {
                return true;
            }
        }
        return false;
    }
    
    public long tokenExpiredTime() {
        if(approvalTimeStamp != 0) {
            TimeZone timeZone = TimeZone.getTimeZone("Asia/Seoul");
            Calendar tmp = Calendar.getInstance();
            tmp.setTime(new Date(approvalTimeStamp*1000));
            tmp.setTimeZone(timeZone);
            int year = tmp.get(Calendar.YEAR);
            if(tmp.get(Calendar.MONTH) >= Calendar.MARCH && tmp.get(Calendar.MONTH) < Calendar.SEPTEMBER) {
                tmp.set(year, Calendar.SEPTEMBER, 1, 0, 0, 0);
            } else {
                tmp.set(year+1, Calendar.MARCH, 1, 0, 0, 0);
            }
            return tmp.getTimeInMillis()/1000;
            // return approvalTimeStamp + 60;
        }
        return 0;
    }
    
    public static UserDB findByUnivCodeAndStudentId(UniversityRepository universityRepository, UserRepository userRepository, String univCode, String studentId) throws NotFoundException, WrongInDataBaseException {
        int univId = University.findByUnivCode(universityRepository, univCode).getId();
        List<UserDB> targetList = userRepository.findByUniversityIdAndStudentId(univId, studentId);
        
        if(targetList.size() == 0) {
            throw new NotFoundException("학번이 " + studentId + "인 학생정보는 " + univCode + "를 학교 코드로 갖는 학교에서 찾을 수 없습니다.");
        } else if(targetList.size() != 1) {
            throw new WrongInDataBaseException("학번이 " + studentId + "인 학생정보가 " + univCode + "를 학교 코드로 갖는 학교에 2개 이상 존재합니다.");
        } else {
            return targetList.get(0);
        }
    }
    
    public static UserDB findByToken(UserRepository userRepository, String token) throws NotFoundException, WrongInDataBaseException {
        List<UserDB> targetList = userRepository.findByToken(token);
        
        if(targetList.size() == 0) {
            throw new NotFoundException("토큰이 " + token + "인 학생정보를 찾을 수 없습니다.");
        } else if(targetList.size() != 1) {
            throw new WrongInDataBaseException("토큰이 " + token + "인 학생정보가 2개 이상 존재합니다.");
        } else {
            return targetList.get(0);
        }
    }
}
