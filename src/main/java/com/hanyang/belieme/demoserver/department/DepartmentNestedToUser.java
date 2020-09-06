package com.hanyang.belieme.demoserver.department;

public class DepartmentNestedToUser {
    private int id;
    
    private String departmentCode;

    private String departmentName;
    
    private boolean available;
    
    public DepartmentNestedToUser() {
    }
    
    public int getId() {
        return id;
    }
    
    public String getDepartmentCode() {
        return departmentCode;
    }
    
    public String getDepartmentName() {
        return departmentName;
    }
    
    public boolean getAvailble() {
        return available;
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
    
    public void setDepartmentCode(String departmentCode) {
        this.departmentCode = departmentCode;
    }
    
    public void setDepartmentName(String departmentName) {
        this.departmentName = departmentName;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }
}
