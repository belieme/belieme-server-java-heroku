package com.belieme.server.domain.event;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class EventDto {
    private String univCode;
    private String deptCode;
    private String thingCode;
    private int itemNum;
    private int num;
    
    private String userStudentId;
    private String approveManagerStudentId;
    private String returnManagerStudentId;
    private String lostManagerStudentId;
    
    private long reserveTimeStamp;
    private long approveTimeStamp;
    private long returnTimeStamp;
    private long cancelTimeStamp;
    private long lostTimeStamp;
    
    public EventDto() {
    }
    
    public String getUnivCode() {
        return univCode;
    }
    
    public String getDeptCode() {
        return deptCode;
    }
    
    public String getThingCode() {
        return thingCode;
    }
    
    public int getItemNum() {
        return itemNum;
    }
    
    public int getNum() {
        return num;
    }
    
    public String getUserStudentId() {
        return userStudentId;
    }
    
    public String getApproveManagerStudentId() {
        return approveManagerStudentId;
    }
    
    public String getReturnManagerStudentId() {
        return returnManagerStudentId;
    }
    
    public String getLostManagerStudentId() {
        return lostManagerStudentId;
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

    public String getReserveTimeStampString() {
        if(reserveTimeStamp == 0) {
            return null;
        }
        Date date = new Date(reserveTimeStamp*1000);
        SimpleDateFormat sdf;
        sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
        sdf.setTimeZone(TimeZone.getTimeZone("Asia/Seoul"));
        return sdf.format(date);
    }

    public String getApproveTimeStampString() {
        if(approveTimeStamp == 0) {
            return null;
        }
        Date date =  new Date(approveTimeStamp*1000);
        SimpleDateFormat sdf;
        sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
        sdf.setTimeZone(TimeZone.getTimeZone("Asia/Seoul"));
        return sdf.format(date);
    }

    public String getReturnTimeStampString() {
        if(returnTimeStamp == 0) {
            return null;
        }
        Date date = new Date(returnTimeStamp*1000);
        SimpleDateFormat sdf;
        sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
        sdf.setTimeZone(TimeZone.getTimeZone("Asia/Seoul"));
        return sdf.format(date);
    }

    public String getCancelTimeStampString() {
        if(cancelTimeStamp == 0) {
            return null;
        }
        Date date = new Date(cancelTimeStamp*1000);
        SimpleDateFormat sdf;
        sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
        sdf.setTimeZone(TimeZone.getTimeZone("Asia/Seoul"));
        return sdf.format(date);
    }
    
    public String getLostTimeStampString() {
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
    
    public void setUnivCode(String univCode) {
        this.univCode = univCode;
    }
    
    public void setDeptCode(String deptCode) {
        this.deptCode = deptCode;
    }
    
    public void setThingCode(String thingCode) {
        this.thingCode = thingCode;
    }
    
    public void setItemNum(int itemNum) {
        this.itemNum = itemNum;
    }

    public void setUserStudentId(String userStudentId) {
        this.userStudentId = userStudentId;
    }
    
    public void setApproveManagerStudentId(String approveManagerStudentId) {
        this.approveManagerStudentId = approveManagerStudentId;
    }
    
    public void setReturnManagerStudentId(String returnManagerStudentId) {
        this.returnManagerStudentId = returnManagerStudentId;
    }
    
    public void setLostManagerStudentId(String lostManagerStudentId) {
        this.lostManagerStudentId = lostManagerStudentId;
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
