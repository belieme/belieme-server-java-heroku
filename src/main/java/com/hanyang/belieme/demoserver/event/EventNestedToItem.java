package com.hanyang.belieme.demoserver.event;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import com.hanyang.belieme.demoserver.user.UserNestedToEvent;

public class EventNestedToItem {
    private int id;

    private UserNestedToEvent user;
    private UserNestedToEvent approveManager;
    private UserNestedToEvent returnManager;
    private UserNestedToEvent lostManager;
    
    private long reserveTimeStamp;
    private long approveTimeStamp;
    private long returnTimeStamp;
    private long cancelTimeStamp;
    private long lostTimeStamp;
    
    public EventNestedToItem() {
    }
    
    public EventNestedToItem(EventNestedToItem oth) {
        id = oth.id;
        
        user = null;
        approveManager = null;
        returnManager = null;
        lostManager = null;
        
        if(oth.user != null) {
            user = new UserNestedToEvent(oth.user);    
        }
        if(oth.approveManager != null) {
            approveManager = new UserNestedToEvent(oth.approveManager);    
        }
        if(oth.returnManager != null) {
            returnManager = new UserNestedToEvent(oth.returnManager);   
        }
        if(oth.lostManager != null) {
            lostManager = new UserNestedToEvent(oth.lostManager);    
        }
        
        reserveTimeStamp = oth.reserveTimeStamp;
        approveTimeStamp = oth.approveTimeStamp;
        returnTimeStamp = oth.returnTimeStamp;
        cancelTimeStamp = oth.cancelTimeStamp;
        lostTimeStamp = oth.lostTimeStamp;
    }

    public int getId() {
        return id;
    }
    
    public UserNestedToEvent getuser() {
        if(user == null) {
            return null;
        }
        return new UserNestedToEvent(user);
    }
    
    public UserNestedToEvent getApproveManager() {
        if(approveManager == null) {
            return null;
        }
        return new UserNestedToEvent(approveManager);
    }
    
    public UserNestedToEvent getReturnManager() {
        if(returnManager == null) {
            return null;
        }
        return new UserNestedToEvent(returnManager);
    }
    
    public UserNestedToEvent getLostManager() {
        if(lostManager == null) {
            return null;
        }
        return new UserNestedToEvent(lostManager);
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
        Date date =  new Date(approveTimeStamp);
        SimpleDateFormat sdf;
        sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
        sdf.setTimeZone(TimeZone.getTimeZone("Asia/Seoul"));
        return sdf.format(date);
    }

    public String getReturnTimeStamp() {
        if(returnTimeStamp == 0) {
            return null;
        }
        Date date = new Date(returnTimeStamp);
        SimpleDateFormat sdf;
        sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
        sdf.setTimeZone(TimeZone.getTimeZone("Asia/Seoul"));
        return sdf.format(date);
    }

    public String getCancelTimeStamp() {
        if(cancelTimeStamp == 0) {
            return null;
        }
        Date date = new Date(cancelTimeStamp);
        SimpleDateFormat sdf;
        sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
        sdf.setTimeZone(TimeZone.getTimeZone("Asia/Seoul"));
        return sdf.format(date);
    }
    
    public String getLostTimeStamp() {
        if(lostTimeStamp == 0) {
            return null;
        }
        Date date = new Date(lostTimeStamp);
        SimpleDateFormat sdf;
        sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
        sdf.setTimeZone(TimeZone.getTimeZone("Asia/Seoul"));
        return sdf.format(date);
    }

    public String getStatus() {
        //TODO ERROR인 조건들 추가하기 ex)item이 널이거나 그런경우?
        if(reserveTimeStamp != 0) {
            if(returnTimeStamp != 0) {
                if(lostTimeStamp != 0) {
                    return "FOUNDANDRETURNED";
                }
                return "RETURNED";
            }
            else if(cancelTimeStamp != 0) {
                return "EXPIRED";
            }
            else if(approveTimeStamp != 0) {
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
                return "RESERVED";
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
    
    public void setUser(UserNestedToEvent user) {
        this.user = user;
    }
    
    public void setApproveManager(UserNestedToEvent approveManager) {
        this.approveManager = approveManager;
    }
    
    public void setReturnManager(UserNestedToEvent returnManager) {
        this.returnManager = returnManager;
    }
    
    public void setLostManager(UserNestedToEvent lostManager) {
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
    
    public long expiredTime() {
        return reserveTimeStamp + 15*60;
    }

    public long dueTime() {
        TimeZone timeZone = TimeZone.getTimeZone("Asia/Seoul");
        Calendar tmp = Calendar.getInstance();
        tmp.setTime(new Date(approveTimeStamp*1000));
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