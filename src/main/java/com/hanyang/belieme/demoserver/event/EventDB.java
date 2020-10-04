package com.hanyang.belieme.demoserver.event;

import javax.persistence.*;

import java.util.Calendar;
import java.util.Date;
import java.util.Optional;
import java.util.TimeZone;

import com.hanyang.belieme.demoserver.department.DepartmentRepository;
import com.hanyang.belieme.demoserver.department.major.MajorRepository;
import com.hanyang.belieme.demoserver.exception.NotFoundException;
import com.hanyang.belieme.demoserver.item.*;
import com.hanyang.belieme.demoserver.thing.ThingDB;
import com.hanyang.belieme.demoserver.thing.ThingNestedToEvent;
import com.hanyang.belieme.demoserver.thing.ThingRepository;
import com.hanyang.belieme.demoserver.university.UniversityRepository;
import com.hanyang.belieme.demoserver.user.UserDB;
import com.hanyang.belieme.demoserver.user.UserRepository;

@Entity
public class EventDB {
    @Id @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;    
    
    private int itemId;
    private int userId;
    private int approveManagerId;
    private int returnManagerId;
    private int lostManagerId;
    
    private long reserveTimeStamp;
    private long approveTimeStamp;
    private long returnTimeStamp;
    private long cancelTimeStamp;
    private long lostTimeStamp;
    
    public EventDB() {
    }
    
    public int getId() {
        return id;
    }
    
    public int getItemId() {
        return itemId;
    }

    public int getUserId() {
        return userId;
    }

    public int getApproveManagerId() {
        return approveManagerId;
    }


    public int getReturnManagerId() {
        return returnManagerId;
    }

    
    public int getLostManagerId() {
        return lostManagerId;
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

    public void setItemId(int itemId) {
        this.itemId = itemId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public void setApproveManagerId(int approveManagerId) {
        this.approveManagerId = approveManagerId;
    }

    public void setReturnManagerId(int returnManagerId) {
        this.returnManagerId = returnManagerId;
    }
    
    public void setLostManagerId(int lostManagerId) {
        this.lostManagerId = lostManagerId;
    }

    public void setReserveTimeStampZero() {
        this.reserveTimeStamp = 0;
    }

    public void setApproveTimeStampZero() {
        this.approveTimeStamp = 0;
    }

    public void setReturnTimeStampZero() {
        this.returnTimeStamp = 0;
    }

    public void setCancelTimeStampZero() {
        this.cancelTimeStamp = 0;
    }
    
    public void setLostTimeStampZero() {
        this.lostTimeStamp = 0;
    }

    public void setReserveTimeStampNow() {
        this.reserveTimeStamp = System.currentTimeMillis()/1000;
    }

    public void setApproveTimeStampNow() {
        this.approveTimeStamp = System.currentTimeMillis()/1000;
    }

    public void setReturnTimeStampNow() {
        this.returnTimeStamp = System.currentTimeMillis()/1000;
    }

    public void setCancelTimeStampNow() {
        this.cancelTimeStamp = System.currentTimeMillis()/1000;
    }
    
    public void setLostTimeStampNow() {
        this.lostTimeStamp = System.currentTimeMillis()/1000;
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
    
    public Event toEvent(UserRepository userRepository, ThingRepository thingRepository, ItemRepository itemRepository, EventRepository eventRepository) throws NotFoundException { //TODO deptId도 비교해야 할 것인가?
        Event output = new Event();
        
        int thingId;
        ItemNestedToEvent item;
        Optional<ItemDB> itemOptional = itemRepository.findById(itemId);
        if(itemOptional.isPresent()) {
            thingId = itemOptional.get().getThingId();
            item = itemOptional.get().toItemNestedToEvent(eventRepository);
        } else {
            throw new NotFoundException(itemId + "를 id로 갖는 물건이 존재하지 않습니다.");
        }
        
        ThingNestedToEvent thing;
        Optional<ThingDB> thingDBOptional = thingRepository.findById(thingId);
        if(thingDBOptional.isPresent()) {
            thing = thingDBOptional.get().toThingNestedToEvent();
        } else {
            throw new NotFoundException(thingId + "를 id로 갖는 물품이 존재하지 않습니다.");
        }
        
        
        output.setUser(null);
        output.setApproveManager(null);
        output.setReturnManager(null);
        output.setLostManager(null);
        if(userId != 0) {
            Optional<UserDB> tmpOptional = userRepository.findById(userId);
            if(tmpOptional.isPresent()) {
                output.setUser(tmpOptional.get().toUserNestedToEvent());
            } else {
                output.setUser(null); // TODO DB에 없음 이라는 User를 만들기
            }
        }        
        if(approveManagerId != 0) {    
            Optional<UserDB> tmpOptional = userRepository.findById(approveManagerId);
            if(tmpOptional.isPresent()) {
                output.setApproveManager(tmpOptional.get().toUserNestedToEvent());
            } else {
                output.setApproveManager(null); // TODO DB에 없음 이라는 User를 만들기
            }
        }        
        if(returnManagerId != 0) {
            Optional<UserDB> tmpOptional = userRepository.findById(returnManagerId);
            if(tmpOptional.isPresent()) {
                output.setReturnManager(tmpOptional.get().toUserNestedToEvent());
            } else {
                output.setReturnManager(null); // TODO DB에 없음 이라는 User를 만들기
            }
        }        
        if(lostManagerId != 0) {
            Optional<UserDB> tmpOptional = userRepository.findById(lostManagerId);
            if(tmpOptional.isPresent()) {
                output.setLostManager(tmpOptional.get().toUserNestedToEvent());
            } else {
                output.setLostManager(null); // TODO DB에 없음 이라는 User를 만들기
            }
        }
        output.setId(id);
        output.setItem(item);
        output.setThing(thing);
        output.setReserveTimeStamp(reserveTimeStamp);
        output.setApproveTimeStamp(approveTimeStamp);
        output.setReturnTimeStamp(returnTimeStamp);
        output.setCancelTimeStamp(cancelTimeStamp);
        output.setLostTimeStamp(lostTimeStamp);
        
        return output;
    }

    public EventNestedToItem toEventNestedToItem(UserRepository userRepository) {
        EventNestedToItem output = new EventNestedToItem();
        
        output.setUser(null);
        output.setApproveManager(null);
        output.setReturnManager(null);
        output.setLostManager(null);
        if(userId != 0) {
            Optional<UserDB> tmpOptional = userRepository.findById(userId);
            if(tmpOptional.isPresent()) {
                output.setUser(tmpOptional.get().toUserNestedToEvent());
            } else {
                output.setUser(null); // TODO DB에 없음 이라는 User를 만들기
            }
        }        
        if(approveManagerId != 0) {
            Optional<UserDB> tmpOptional = userRepository.findById(approveManagerId);
            if(tmpOptional.isPresent()) {
                output.setApproveManager(tmpOptional.get().toUserNestedToEvent());
            } else {
                output.setApproveManager(null); // TODO DB에 없음 이라는 User를 만들기
            }
        }        
        if(returnManagerId != 0) {
            Optional<UserDB> tmpOptional = userRepository.findById(returnManagerId);
            if(tmpOptional.isPresent()) {
                output.setReturnManager(tmpOptional.get().toUserNestedToEvent());
            } else {
                output.setReturnManager(null); // TODO DB에 없음 이라는 User를 만들기
            }
        }        
        if(lostManagerId != 0) {
            Optional<UserDB> tmpOptional = userRepository.findById(lostManagerId);
            if(tmpOptional.isPresent()) {
                output.setLostManager(tmpOptional.get().toUserNestedToEvent());
            } else {
                output.setLostManager(null); // TODO DB에 없음 이라는 User를 만들기
            }
        }
        
        output.setId(id);
        output.setReserveTimeStamp(reserveTimeStamp);
        output.setApproveTimeStamp(approveTimeStamp);
        output.setReturnTimeStamp(returnTimeStamp);
        output.setCancelTimeStamp(cancelTimeStamp);
        output.setLostTimeStamp(lostTimeStamp);
        
        return output;
    }
}
