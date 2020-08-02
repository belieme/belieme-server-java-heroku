package com.hanyang.belieme.demoserver.event;

import javax.persistence.*;
import java.util.Calendar;
import java.util.Date;
import java.util.Optional;
import java.util.TimeZone;

import com.hanyang.belieme.demoserver.thing.*;
import com.hanyang.belieme.demoserver.item.*;


@Entity
public class History {
    @Id @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;

    private int typeId;
    private int itemNum;
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
    
    @Transient
    private ItemNestedToHistory item;

    public History() {
    }
    
    public int typeIdGetter() {
        return typeId;
    }

    public int itemNumGetter() {
        return itemNum;
    }

    public int getId() {
        return id;
    }
    
    public ItemNestedToHistory getItem() {
        return item;
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
        return returnTimeStamp;
    }

    public long getCancelTimeStamp() {
        return cancelTimeStamp;
    }

    public void setTypeId(int typeId) {
        this.typeId = typeId;
    }

    public void setItemNum(int itemNum) {
        this.itemNum = itemNum;
    }
    
    public void setItem(int num, ItemTypeDB itemType) {
        this.item = new ItemNestedToHistory(num, itemType);
    }

    public void setRequesterId(int requesterId) {
        this.requesterId = requesterId;
    }

    public void setRequesterName(String requesterName) {
        this.requesterName = requesterName;
    }

    public void setResponseManagerId(int responseManagerId) {
        this.responseManagerId = responseManagerId;
    }

    public void setResponseManagerName(String responseManagerName) {
        this.responseManagerName = responseManagerName;
    }

    public void setReturnManagerId(int returnManagerId) {
        this.returnManagerId = returnManagerId;
    }

    public void setReturnManagerName(String returnManagerName) {
        this.returnManagerName = returnManagerName;
    }

    public void setRequestTimeStampZero() {
        this.requestTimeStamp = 0;
    }

    public void setResponseTimeStampZero() {
        this.responseTimeStamp = 0;
    }

    public void setReturnTimeStampZero() {
        this.returnTimeStamp = 0;
    }

    public void setCancelTimeStampZero() {
        this.cancelTimeStamp = 0;
    }

    public void setRequestTimeStampNow() {
        this.requestTimeStamp = System.currentTimeMillis()/1000;
    }

    public void setResponseTimeStampNow() {
        this.responseTimeStamp = System.currentTimeMillis()/1000;
    }

    public void setReturnTimeStampNow() {
        this.returnTimeStamp = System.currentTimeMillis()/1000;
    }

    public void setCancelTimeStampNow() {
        this.cancelTimeStamp = System.currentTimeMillis()/1000;
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

    public void addInfo(ItemTypeRepository itemTypeRepository) {
        Optional<ItemTypeDB> itemTypeDB = itemTypeRepository.findById(typeId);
        setItem(itemNum, itemTypeDB.get());
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
