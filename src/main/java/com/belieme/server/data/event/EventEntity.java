package com.belieme.server.data.event;

import javax.persistence.*;

import java.util.*;

@Entity
public class EventEntity {
    @Id @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;    
    
    private int itemId;
    private int num;
    
    private int userId;
    private int approveManagerId;
    private int returnManagerId;
    private int lostManagerId;
    
    private long reserveTimeStamp;
    private long approveTimeStamp;
    private long returnTimeStamp;
    private long cancelTimeStamp;
    private long lostTimeStamp;
    
    public EventEntity() {
    }
    
    public int getId() {
        return id;
    }
    
    public int getItemId() {
        return itemId;
    }
    
    public int getNum() {
        return num;
    }

    public int getUserId() {
        return userId;
    }

    public int getApproveManagerId() {
        return approveManagerId;
    }

    public int getReturnManagerId() {
        return returnManagerId;
    }

    public int getLostManagerId() {
        return lostManagerId;
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

    public void setItemId(int itemId) {
        this.itemId = itemId;
    }
    
    public void setNum(int num) {
        this.num = num;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public void setApproveManagerId(int approveManagerId) {
        this.approveManagerId = approveManagerId;
    }

    public void setReturnManagerId(int returnManagerId) {
        this.returnManagerId = returnManagerId;
    }
    
    public void setLostManagerId(int lostManagerId) {
        this.lostManagerId = lostManagerId;
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
