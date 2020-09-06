package com.hanyang.belieme.demoserver.event;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import com.hanyang.belieme.demoserver.user.UserNestedToEvent;

public class EventNestedToItem {
    private int id;

    private UserNestedToEvent requester;
    private UserNestedToEvent responseManager;
    private UserNestedToEvent returnManager;
    private UserNestedToEvent lostManager;
    
    private long requestTimeStamp;
    private long responseTimeStamp;
    private long returnTimeStamp;
    private long cancelTimeStamp;
    private long lostTimeStamp;
    
    public EventNestedToItem() {
    }
    
    public EventNestedToItem(EventNestedToItem oth) {
        id = oth.id;
        requester = new UserNestedToEvent(oth.requester);
        responseManager = new UserNestedToEvent(oth.responseManager);
        returnManager = new UserNestedToEvent(oth.returnManager);
        lostManager = new UserNestedToEvent(oth.lostManager);
        
        requestTimeStamp = oth.requestTimeStamp;
        responseTimeStamp = oth.responseTimeStamp;
        returnTimeStamp = oth.returnTimeStamp;
        cancelTimeStamp = oth.cancelTimeStamp;
        lostTimeStamp = oth.lostTimeStamp;
    }

    public int getId() {
        return id;
    }
    
    public UserNestedToEvent getRequester() {
        return new UserNestedToEvent(requester);
    }
    
    public UserNestedToEvent getResponseManager() {
        return new UserNestedToEvent(responseManager);
    }
    
    public UserNestedToEvent getReturnManager() {
        return new UserNestedToEvent(returnManager);
    }
    
    public UserNestedToEvent getLostManager() {
        return new UserNestedToEvent(lostManager);
    }

    public long getRequestTimeStamp() {
        return requestTimeStamp;
    }

    public long getResponseTimeStamp() {
        return responseTimeStamp;
    }

    public long getReturnTimeStamp() {
        return requestTimeStamp;
    }

    public long getCancelTimeStamp() {
        return cancelTimeStamp;
    }
    
    public long getLostTimeStamp() {
        return lostTimeStamp;
    }

    public String getStatus() {
        //TODO ERROR인 조건들 추가하기 ex)item이 널이거나 그런경우?
        if(requestTimeStamp != 0) {
            if(returnTimeStamp != 0) {
                if(lostTimeStamp != 0) {
                    return "FOUNDANDRETURNED";
                }
                return "RETURNED";
            }
            else if(cancelTimeStamp != 0) {
                return "EXPIRED";
            }
            else if(responseTimeStamp != 0) {
                if(lostTimeStamp != 0) {
                    return "LOST";
                }
                else if(dueTime() > System.currentTimeMillis()/1000) {
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
            if(lostTimeStamp != 0) {
                if(returnTimeStamp != 0) {
                    return "FOUND";
                } else {
                    return "LOST";
                }
            }
            return "ERROR";
        }
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
    public void setRequester(UserNestedToEvent requester) {
        this.requester = requester;
    }
    
    public void setResponseManager(UserNestedToEvent responseManager) {
        this.responseManager = responseManager;
    }
    
    public void setReturnManager(UserNestedToEvent returnManager) {
        this.returnManager = returnManager;
    }
    
    public void setLostManager(UserNestedToEvent lostManager) {
        this.lostManager = lostManager;
    }

    public void setRequestTimeStamp(long requestTimeStamp) {
        this.requestTimeStamp = requestTimeStamp;
    }
    
    public void setResponseTimeStamp(long responseTimeStamp) {
        this.responseTimeStamp = responseTimeStamp;
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
    
    public long expiredTime() {
        return requestTimeStamp + 15*60;
    }

    public long dueTime() {
        TimeZone timeZone = TimeZone.getTimeZone("Asia/Seoul");
        Calendar tmp = Calendar.getInstance();
        tmp.setTime(new Date(responseTimeStamp*1000));
        tmp.setTimeZone(timeZone);
        tmp.add(Calendar.DATE, 7);
        if(tmp.get(Calendar.HOUR_OF_DAY) > 18 ) {
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