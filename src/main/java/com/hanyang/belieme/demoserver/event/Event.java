package com.hanyang.belieme.demoserver.event;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import com.hanyang.belieme.demoserver.item.*;

public class Event {
    private int id;
    
    private ItemNestedToEvent item;
    
    private int requesterId;
    private String requesterName;
    private int responseManagerId;
    private String responseManagerName;
    private int returnManagerId;
    private String returnManagerName;
    private int lostManagerId;
    private String lostManagerName;
    
    private long requestTimeStamp;
    private long responseTimeStamp;
    private long returnTimeStamp;
    private long cancelTimeStamp;
    private long lostTimeStamp;
    
    

    public Event() {
    }
    
    public int getId() {
        return id;
    }
    
    public ItemNestedToEvent getItem() {
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
    
    public int getLostManagerId() {
        return lostManagerId;
    }

    public String getLostManagerName() {
        return lostManagerName;
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
    
    public long getLostTimeStamp() {
        return lostTimeStamp;
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
    public void setItem(ItemNestedToEvent item) {
        this.item = item;
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
    
    public void setLostManagerId(int lostManagerId) {
        this.lostManagerId = lostManagerId;
    }

    public void setLostManagerName(String lostManagerName) {
        this.lostManagerName = lostManagerName;
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

    // public void addInfo(ThingRepository thingRepository, ItemRepository itemRepository, EventRepository eventRepository) {
    //     Optional<Item> itemOptional = itemRepository.findById(itemId);
    //     if(itemOptional.isPresent()) {
    //         itemOptional.get().addInfo(thingRepository, eventRepository);
    //         setItem(itemOptional.get());
    //     } else {
    //         setItem(null);
    //     }        
    // }

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

    public EventNestedToItem toEventNestedToItem() {
        EventNestedToItem output = new EventNestedToItem();
        output.setId(getId());
        output.setRequesterId(requesterId);
        output.setRequesterName(requesterName);
        output.setResponseManagerId(responseManagerId);
        output.setResponseManagerName(responseManagerName);
        output.setReturnManagerId(returnManagerId);
        output.setReturnManagerName(returnManagerName);
        output.setLostManagerId(lostManagerId);
        output.setLostManagerName(lostManagerName);
        output.setRequestTimeStamp(requestTimeStamp);
        output.setResponseTimeStamp(responseTimeStamp);
        output.setReturnTimeStamp(returnTimeStamp);
        output.setCancelTimeStamp(cancelTimeStamp);
        output.setLostTimeStamp(lostTimeStamp);
        
        return output;
    }
}