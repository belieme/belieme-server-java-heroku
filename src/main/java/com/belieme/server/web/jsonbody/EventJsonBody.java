package com.belieme.server.web.jsonbody;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class EventJsonBody {
    private ThingJsonBodyNestedToEvent thing;
    private ItemJsonBodyNestedToEvent item;

    private int num;
    
    private UserJsonBodyNestedToEvent user;
    private UserJsonBodyNestedToEvent approveManager;
    private UserJsonBodyNestedToEvent returnManager;
    private UserJsonBodyNestedToEvent lostManager;
    
    private long reserveTimeStamp;
    private long approveTimeStamp;
    private long returnTimeStamp;
    private long cancelTimeStamp;
    private long lostTimeStamp;
    
    public EventJsonBody() {
    }
    
    public ItemJsonBodyNestedToEvent getItem() {
        return item;
    }
    
    public ThingJsonBodyNestedToEvent getThing() {
        return thing;
    }
    
    public int getNum() { 
        return num;
    }
    
    public UserJsonBodyNestedToEvent getuser() {
        return user;
    }
    
    public UserJsonBodyNestedToEvent getApproveManager() {
        return approveManager;
    }
    
    public UserJsonBodyNestedToEvent getReturnManager() {
        return returnManager;
    }
    
    public UserJsonBodyNestedToEvent getLostManager() {
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
    
    public void setItem(ItemJsonBodyNestedToEvent item) {
        this.item = item;
    }
    
    public void setThing(ThingJsonBodyNestedToEvent thing) {
        this.thing = thing;
    }
    
    public void setNum(int num) {
        this.num = num;
    }

    public void setUser(UserJsonBodyNestedToEvent user) {
        this.user = user;
    }
    
    public void setApproveManager(UserJsonBodyNestedToEvent approveManager) {
        this.approveManager = approveManager;
    }
    
    public void setReturnManager(UserJsonBodyNestedToEvent returnManager) {
        this.returnManager = returnManager;
    }
    
    public void setLostManager(UserJsonBodyNestedToEvent lostManager) {
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
