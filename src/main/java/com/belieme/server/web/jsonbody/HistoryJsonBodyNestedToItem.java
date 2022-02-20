package com.belieme.server.web.jsonbody;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

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
    
    public HistoryJsonBodyNestedToItem() {
    }
    
    public int getNum() {
        return num;
    }
    
    public UserJsonBodyNestedToHistory getuser() {
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

    public String getReserveTimeStamp() {
        if(reserveTimeStamp == 0) {
            return null;
        }
        Date date = new Date(reserveTimeStamp*1000);
        SimpleDateFormat sdf;
        sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
        sdf.setTimeZone(TimeZone.getTimeZone("Asia/Seoul"));
        return sdf.format(date);
    }

    public String getApproveTimeStamp() {
        if(approveTimeStamp == 0) {
            return null;
        }
        Date date =  new Date(approveTimeStamp*1000);
        SimpleDateFormat sdf;
        sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
        sdf.setTimeZone(TimeZone.getTimeZone("Asia/Seoul"));
        return sdf.format(date);
    }

    public String getReturnTimeStamp() {
        if(returnTimeStamp == 0) {
            return null;
        }
        Date date = new Date(returnTimeStamp*1000);
        SimpleDateFormat sdf;
        sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
        sdf.setTimeZone(TimeZone.getTimeZone("Asia/Seoul"));
        return sdf.format(date);
    }

    public String getCancelTimeStamp() {
        if(cancelTimeStamp == 0) {
            return null;
        }
        Date date = new Date(cancelTimeStamp*1000);
        SimpleDateFormat sdf;
        sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
        sdf.setTimeZone(TimeZone.getTimeZone("Asia/Seoul"));
        return sdf.format(date);
    }
    
    public String getLostTimeStamp() {
        if(lostTimeStamp == 0) {
            return null;
        }
        Date date = new Date(lostTimeStamp*1000);
        SimpleDateFormat sdf;
        sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
        sdf.setTimeZone(TimeZone.getTimeZone("Asia/Seoul"));
        return sdf.format(date);
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
}