package com.hanyang.belieme.demoserver.event;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class HistoryNestedToItem {
    private int id;

    private int requesterId;
    private String requesterName;
    private int responseManagerId;
    private String responseManagerName;
    private int returnManagerId;
    private String returnManagerName;
    private long requestTimeStamp;
    private long responseTimeStamp;
    private long returnTimeStamp;
    private long cancelTimeStamp;
    
    public HistoryNestedToItem(History history) {
        if(history != null) {
            id = history.getId();
            requesterId = history.getRequesterId();
            requesterName = new String(history.getRequesterName());
            responseManagerId = history.getResponseManagerId();
            responseManagerName = new String(history.getResponseManagerName());
            returnManagerId = history.getReturnManagerId();
            returnManagerName = new String(history.getReturnManagerName());
            requestTimeStamp = history.getRequestTimeStamp();
            responseTimeStamp = history.getResponseTimeStamp();
            returnTimeStamp = history.getReturnTimeStamp();
            cancelTimeStamp = history.getCancelTimeStamp();
        } else {
            id = -1;
            requesterId = -1;
            requesterName = "";
            responseManagerId = -1;
            responseManagerName = "";
            returnManagerId = -1;
            returnManagerName = "";
            requestTimeStamp = 0;
            responseTimeStamp = 0;
            returnTimeStamp = 0;
            cancelTimeStamp = 0;
        }
    }
    
    public HistoryNestedToItem(HistoryNestedToItem historyNestedToItem) {
        id = historyNestedToItem.id;
        requesterId = historyNestedToItem.requesterId;
        requesterName = new String(historyNestedToItem.requesterName);
        responseManagerId = historyNestedToItem.responseManagerId;
        responseManagerName = new String(historyNestedToItem.responseManagerName);
        returnManagerId = historyNestedToItem.returnManagerId;
        returnManagerName = new String(historyNestedToItem.returnManagerName);
        requestTimeStamp = historyNestedToItem.requestTimeStamp;
        responseTimeStamp = historyNestedToItem.responseTimeStamp;
        returnTimeStamp = historyNestedToItem.returnTimeStamp;
        cancelTimeStamp = historyNestedToItem.cancelTimeStamp;
        
    }

    public int getId() {
        return id;
    }
    
    public int getRequesterId() {
        return requesterId;
    }
    
    public String getRequesterName() {
        return requesterName;
    }

    public int getResponseManagerId() {
        return responseManagerId;
    }

    public String getResponseManagerName() {
        return responseManagerName;
    }

    public int getReturnManagerId() {
        return returnManagerId;
    }

    public String getReturnManagerName() {
        return returnManagerName;
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

    public String getStatus() {
        if(requestTimeStamp != 0) {
            if(returnTimeStamp != 0) {
                return "RETURNED";
            }
            else if(cancelTimeStamp != 0) {
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