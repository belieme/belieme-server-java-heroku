package com.hanyang.belieme.demoserver;

import javax.persistence.*;
import java.util.Calendar;
import java.util.Date;

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
    private long canceledTimeStamp;

    @Transient
    private String typeName;

    @Transient
    private String typeEmoji;

    public History() {
    }

    public History(int typeId, int itemNum, int requesterId, String requesterName, int managerId, String managerName, long requestTimeStamp, long responseTimeStamp, long returnedTimeStamp, long canceledTimeStamp) {
        this.typeId = typeId;
        this.itemNum = itemNum;
        this.requesterId = requesterId;
        this.requesterName = requesterName;
        this.managerId = managerId;
        this.managerName = managerName;
        this.requestTimeStamp = requestTimeStamp;
        this.responseTimeStamp = responseTimeStamp;
        this.returnedTimeStamp = returnedTimeStamp;
        this.canceledTimeStamp = canceledTimeStamp;
    }

    public int getId() {
        return id;
    }

    public int getTypeId() {
        return typeId;
    }

    public int getItemNum() {
        return itemNum;
    }

    public int getRequesterId() {
        return requesterId;
    }

    public String getRequesterName() {
        return requesterName;
    }

    public int getManagerId() {
        return managerId;
    }

    public String getManagerName() {
        return managerName;
    }

    public long getRequestTimeStamp() {
        return requestTimeStamp;
    }

    public long getResponseTimeStamp() {
        return responseTimeStamp;
    }

    public long getReturnedTimeStamp() {
        return returnedTimeStamp;
    }

    public long getCanceledTimeStamp() {
        return canceledTimeStamp;
    }

    public String getTypeName() {
        return typeName;
    }

    public String getTypeEmoji() {
        return typeEmoji;
    }

    public void setTypeId(int typeId) {
        this.typeId = typeId;
    }

    public void setItemNum(int itemNum) {
        this.itemNum = itemNum;
    }

    public void setRequesterId(int requesterId) {
        this.requesterId = requesterId;
    }

    public void setRequesterName(String requesterName) {
        this.requesterName = requesterName;
    }

    public void setManagerId(int managerId) {
        this.managerId = managerId;
    }

    public void setManagerName(String managerName) {
        this.managerName = managerName;
    }

    public void setRequestTimeStampZero() {
        this.requestTimeStamp = 0;
    }

    public void setResponseTimeStampZero() {
        this.responseTimeStamp = 0;
    }

    public void setReturnedTimeStampZero() {
        this.returnedTimeStamp = 0;
    }

    public void setCanceledTimeStampZero() {
        this.canceledTimeStamp = 0;
    }

    public void setRequestTimeStampNow() {
        this.requestTimeStamp = System.currentTimeMillis()/1000;
    }

    public void setResponseTimeStampNow() {
        this.responseTimeStamp = System.currentTimeMillis()/1000;
    }

    public void setReturnedTimeStampNow() {
        this.returnedTimeStamp = System.currentTimeMillis()/1000;
    }

    public void setCanceledTimeStampNow() {
        this.canceledTimeStamp = System.currentTimeMillis()/1000;
    }

    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }

    public void setTypeEmoji(String typeEmoji) {
        this.typeEmoji = typeEmoji;
    }

    public String getStatus() {
        if(requestTimeStamp != 0) {
            if(returnedTimeStamp != 0) {
                return "RETURNED";
            }
            else if(canceledTimeStamp != 0) {
                return "EXPIRED";
            }
            else if(responseTimeStamp != 0) {
                if(dueTime() > System.currentTimeMillis()/1000) {
                    return "USING";
                }
                else {
                    return "DELAYED";
                }
            }
            else if(expiredTime() > System.currentTimeMillis()/1000) {
                return "REQUESTED";
            }
            else {
                return "EXPIRED";
            }
        }
        else {
            return "ERROR";
        }
    }

    public long expiredTime() {
        return requestTimeStamp + 60;
    }

    public long dueTime() {
        Calendar tmp = Calendar.getInstance();
        tmp.setTime(new Date(responseTimeStamp));
        tmp.add(Calendar.DATE, 7);
        if(tmp.get(Calendar.HOUR_OF_DAY) > 17 ) {
            tmp.add(Calendar.DATE, 1);
        }
        tmp.set(Calendar.HOUR_OF_DAY, 17);
        tmp.set(Calendar.MINUTE, 59);
        tmp.set(Calendar.SECOND, 59);
        if(tmp.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY) {
            tmp.add(Calendar.DATE, 2);
        }
        else if(tmp.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) {
            tmp.add(Calendar.DATE, 1);
        }
        return tmp.getTime().getTime()/1000;
    }
}
