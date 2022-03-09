package com.belieme.server.web.jsonbody;

public class HistoryJsonBodyNestedToItem {
    private int num;
    private UserJsonBodyNestedToHistory user;
    private UserJsonBodyNestedToHistory approveManager;
    private UserJsonBodyNestedToHistory returnManager;
    private UserJsonBodyNestedToHistory lostManager;
    
    private long reserveTimeStamp;
    private long approveTimeStamp;
    private long returnTimeStamp;
    private long cancelTimeStamp;
    private long lostTimeStamp;

    private String status;
    
    public HistoryJsonBodyNestedToItem() {
    }
    
    public int getNum() {
        return num;
    }
    
    public UserJsonBodyNestedToHistory getUser() {
        return user;
    }
    
    public UserJsonBodyNestedToHistory getApproveManager() {
        return approveManager;
    }
    
    public UserJsonBodyNestedToHistory getReturnManager() {
        return returnManager;
    }
    
    public UserJsonBodyNestedToHistory getLostManager() {
        return lostManager;
    }

    public long getReserveTimeStamp() {
        return reserveTimeStamp;
    }

    public long getApproveTimeStamp() {
        return approveTimeStamp;
    }

    public long getReturnTimeStamp() {
        return returnTimeStamp;
    }

    public long getCancelTimeStamp() {
        return cancelTimeStamp;
    }

    public long getLostTimeStamp() {
        return lostTimeStamp;
    }

    public String getStatus() {
        return status;
    }

    public void setNum(int num) {
        this.num = num;
    }
    
    public void setUser(UserJsonBodyNestedToHistory user) {
        this.user = user;
    }
    
    public void setApproveManager(UserJsonBodyNestedToHistory approveManager) {
        this.approveManager = approveManager;
    }
    
    public void setReturnManager(UserJsonBodyNestedToHistory returnManager) {
        this.returnManager = returnManager;
    }
    
    public void setLostManager(UserJsonBodyNestedToHistory lostManager) {
        this.lostManager = lostManager;
    }

    public void setReserveTimeStamp(long reserveTimeStamp) {
        this.reserveTimeStamp = reserveTimeStamp;
    }
    
    public void setApproveTimeStamp(long approveTimeStamp) {
        this.approveTimeStamp = approveTimeStamp;
    }
    
    public void setReturnTimeStamp(long returnTimeStamp) {
        this.returnTimeStamp = returnTimeStamp;
    }
    
    public void setCancelTimeStamp(long cancelTimeStamp) {
        this.cancelTimeStamp = cancelTimeStamp;
    }
    
    public void setLostTimeStamp(long lostTimeStamp) {
        this.lostTimeStamp = lostTimeStamp;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}