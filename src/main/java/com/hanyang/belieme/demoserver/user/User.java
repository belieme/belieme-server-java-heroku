package com.hanyang.belieme.demoserver.user;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hanyang.belieme.demoserver.department.DepartmentNestedToUser;
import com.hanyang.belieme.demoserver.exception.NotFoundException;
import com.hanyang.belieme.demoserver.exception.WrongInDataBaseException;
import com.hanyang.belieme.demoserver.university.University;
import com.hanyang.belieme.demoserver.university.UniversityRepository;

public class User {
    private int id;
    
    private University university;
    
    private List<String> majorCodes;
    
    private List<DepartmentNestedToUser> departments;
    
    private String studentId;

    private String name;
    
    private int entranceYear;
    
    private long createTimeStamp;
    
    private long approvalTimeStamp;
    
    private Map<String, String> permissions;
    
    public User() {
        permissions = new HashMap<>();
    }
    
    public int getId() {
        return id;
    }
    
    public University getUniversity() {
        return university;
    }
    
    public List<String> getMajorCodes() {
        return new ArrayList<String>(majorCodes);
    }
    
    public List<DepartmentNestedToUser> getDepartments() {
        return new ArrayList<DepartmentNestedToUser>(departments);
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
    
    public long getCreateTimeStamp() {
        return createTimeStamp;
    }
    
    public long getApprovalTimeStamp() {
        return approvalTimeStamp;
    }

    public String getPermissions() {
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.writeValueAsString(permissions);     
        } catch(JsonProcessingException e) {
            e.printStackTrace();
            return null;
        }
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
    public void setUniversity(University university) {
        this.university = new University(university);
    }
    
    public void setMajorCodes(List<String> majorCodes) {
        this.majorCodes = new ArrayList<String>(majorCodes);
    }
    
    public void setDepartments(List<DepartmentNestedToUser> departments) {
        this.departments = new ArrayList<DepartmentNestedToUser>(departments);
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
    
    public void setCreateTimeStamp(long createTimeStamp) {
        this.createTimeStamp = createTimeStamp;
    }
    
    public void setApprovalTimeStamp(long approvalTimeStamp) {
        this.approvalTimeStamp = approvalTimeStamp;
    }
    
    public void addPermission(String deptCode, String permission) {
        permissions.put(deptCode, permission);
    }
    
    public static int findIdByUnivCodeAndStudentId(UniversityRepository universityRepository, UserRepository userRepository, String univCode, String studentId) throws NotFoundException, WrongInDataBaseException {
        int univId = University.findIdByUnivCode(universityRepository, univCode);
        List<UserDB> targetList = userRepository.findByUniversityIdAndStudentId(univId, studentId);
        
        if(targetList.size() == 0) {
            throw new NotFoundException();
        } else if(targetList.size() != 1) {
            throw new WrongInDataBaseException();
        } else {
            return targetList.get(0).getId();
        }
    }
    
    public static int findIdByToken(UserRepository userRepository, String token) throws NotFoundException, WrongInDataBaseException {
        List<UserDB> targetList = userRepository.findByToken(token);
        
        if(targetList.size() == 0) {
            throw new NotFoundException();
        } else if(targetList.size() != 1) {
            throw new WrongInDataBaseException();
        } else {
            return targetList.get(0).getId();
        }
    }
}
