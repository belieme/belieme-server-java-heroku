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

import com.hanyang.belieme.demoserver.common.StringListConverter;
import com.hanyang.belieme.demoserver.department.Department;
import com.hanyang.belieme.demoserver.department.DepartmentDB;
import com.hanyang.belieme.demoserver.department.DepartmentRepository;
import com.hanyang.belieme.demoserver.department.major.Major;
import com.hanyang.belieme.demoserver.department.major.MajorRepository;
import com.hanyang.belieme.demoserver.exception.NotFoundException;
import com.hanyang.belieme.demoserver.university.University;
import com.hanyang.belieme.demoserver.university.UniversityRepository;

@Entity
public class UserDB {
    @Id @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;
    
    private int universityId;
    
    @Convert(converter = StringListConverter.class)
    private List<Integer> majorIds;
    
    private String token;
    
    private long createTimeStamp;
    
    private long approvalTimeStamp;
    
    @Column(name = "student_id", nullable = false)
    private String studentId;

    private String name;
    
    private int entranceYear;
    
    private String permission;
    
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
    
    public String getPermission() {
        return permission;
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
    
    public void permissionSetUser() {
        permission = "USER";
    }
    
    public void permissionSetAdmin() {
        permission = "ADMIN";
    }

    public void permissionSetMaster() {
        permission = "MASTER";
    }

    public void permissionSetDeveloper() {
        permission = "DEVELOPER";
    }
    
    public User toUser(UniversityRepository universityRepository, DepartmentRepository departmentRepository, MajorRepository majorRepository) throws NotFoundException {
        User output = new User();
        output.setId(id);
        output.setStudentId(studentId);
        output.setName(name);
        output.setEntranceYear(entranceYear);
        output.setToken(token);
        output.setCreateTimeStamp(createTimeStamp);
        output.setApprovalTimeStamp(approvalTimeStamp);
        output.setPermission(permission);
        
        Optional<University> universityOptional = universityRepository.findById(universityId);
        if(universityOptional.isPresent()) {
            output.setUniversity(universityOptional.get());
        } else {
            throw new NotFoundException();
        }
        
        Iterable<Major> majorListByIdList = majorRepository.findAllById(majorIds);
        Iterator<Major> iter = majorListByIdList.iterator();
        
        ArrayList<String> majorCodes = new ArrayList<String>();
        ArrayList<Department> departments = new ArrayList<Department>();
        while(iter.hasNext()) {
            Major tmp = iter.next();
            majorCodes.add(tmp.getMajorCode());
            
            for(int i = 0; i < departments.size(); i++) {
                if(tmp.getDepartmentId() != departments.get(i).getId()) {
                    Optional<DepartmentDB> tmpDepartmentOptional = departmentRepository.findById(tmp.getDepartmentId());
                    if(!tmpDepartmentOptional.isPresent()) {
                        throw new NotFoundException();
                    }
                    departments.add(tmpDepartmentOptional.get().toDepartment(universityRepository, majorRepository));
                }
            }
            
        }
        
        output.setMajorCodes(majorCodes);
        output.setDepartments(departments);
        
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
}
