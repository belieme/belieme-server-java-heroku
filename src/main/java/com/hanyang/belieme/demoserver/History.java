package com.hanyang.belieme.demoserver;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
public class History {
    @Id @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;

    private int typeId;
    private int itemNum;
    private int requesterId;
    private String requesterName;
    private int managerId;
    private String managerName;
    private long requestTimeStamp;
    private long responseTimeStamp;
    private long returnedTimeStamp;
    private String status;

    private String typeName;

    public History() {
    }

    public History(int typeId, int itemNum, int requesterId, String requesterName, int managerId, String managerName, long requestTimeStamp, long responseTimeStamp, long returnedTimeStamp, String status) {
        this.typeId = typeId;
        this.itemNum = itemNum;
        this.requesterId = requesterId;
        this.requesterName = requesterName;
        this.managerId = managerId;
        this.managerName = managerName;
        this.requestTimeStamp = requestTimeStamp;
        this.responseTimeStamp = responseTimeStamp;
        this.returnedTimeStamp = returnedTimeStamp;
        this.status = status;
    }

    public int getId() {
        return id;
    }

    public int getTypeId() {
        return typeId;
    }

    public void setTypeId(int typeId) {
        this.typeId = typeId;
    }

    public int getItemNum() {
        return itemNum;
    }

    public void setItemNum(int itemNum) {
        this.itemNum = itemNum;
    }

    public int getRequesterId() {
        return requesterId;
    }

    public void setRequesterId(int requesterId) {
        this.requesterId = requesterId;
    }

    public String getRequesterName() {
        return requesterName;
    }

    public void setRequesterName(String requesterName) {
        this.requesterName = requesterName;
    }

    public int getManagerId() {
        return managerId;
    }

    public void setManagerId(int managerId) {
        this.managerId = managerId;
    }

    public String getManagerName() {
        return managerName;
    }

    public void setManagerName(String managerName) {
        this.managerName = managerName;
    }

    public long getRequestTimeStamp() {
        return requestTimeStamp;
    }

    public void setRequestTimeStamp(long requestTimeStamp) {
        this.requestTimeStamp = requestTimeStamp;
    }

    public long getResponseTimeStamp() {
        return responseTimeStamp;
    }

    public void setResponseTimeStamp(long responseTimeStamp) {
        this.responseTimeStamp = responseTimeStamp;
    }

    public long getReturnedTimeStamp() {
        return returnedTimeStamp;
    }

    public void setReturnedTimeStamp(long returnedTimeStamp) {
        this.returnedTimeStamp = returnedTimeStamp;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getTypeName() {
        return typeName;
    }

    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }
}
