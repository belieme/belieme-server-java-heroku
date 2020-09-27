package com.hanyang.belieme.demoserver.event;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import com.hanyang.belieme.demoserver.item.*;
import com.hanyang.belieme.demoserver.thing.ThingNestedToEvent;
import com.hanyang.belieme.demoserver.user.UserNestedToEvent;

public class Event {
    private int id;
    
    private ThingNestedToEvent thing;
    private ItemNestedToEvent item;
    
    private UserNestedToEvent user;
    private UserNestedToEvent approveManager;
    private UserNestedToEvent returnManager;
    private UserNestedToEvent lostManager;
    
    private long reserveTimeStamp;
    private long approveTimeStamp;
    private long returnTimeStamp;
    private long cancelTimeStamp;
    private long lostTimeStamp;
    
    

    public Event() {
    }
    
    public Event(Event oth) {
        this.id = oth.id;
        this.thing = new ThingNestedToEvent(oth.thing);
        this.item = new ItemNestedToEvent(oth.item);
        this.user = new UserNestedToEvent(oth.user);
        this.approveManager = new UserNestedToEvent(oth.approveManager);
        this.returnManager = new UserNestedToEvent(oth.returnManager);
        this.lostManager = new UserNestedToEvent(oth.lostManager);
        
        this.reserveTimeStamp = oth.reserveTimeStamp;
        this.approveTimeStamp = oth.approveTimeStamp;
        this.returnTimeStamp = oth.returnTimeStamp;
        this.cancelTimeStamp = oth.cancelTimeStamp;
        this.lostTimeStamp = oth.lostTimeStamp;
    }
    
    public int getId() {
        return id;
    }
    
    public ItemNestedToEvent getItem() {
        if(item == null) {
            return null;
        }
        return new ItemNestedToEvent(item);
    }
    
    public ThingNestedToEvent getThing() {
        if(item == null) {
            return null;
        }
        return new ThingNestedToEvent(thing);
    }
    
    public UserNestedToEvent getUser() {
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
    
    public void setId(int id) {
        this.id = id;
    }
    
    public void setItem(ItemNestedToEvent item) {
        this.item = new ItemNestedToEvent(item);
    }
    
    public void setThing(ThingNestedToEvent thing) {
        this.thing = new ThingNestedToEvent(thing);
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
