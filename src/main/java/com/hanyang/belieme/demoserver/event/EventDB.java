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
import com.hanyang.belieme.demoserver.thing.ThingRepository;
import com.hanyang.belieme.demoserver.university.UniversityRepository;
import com.hanyang.belieme.demoserver.user.UserDB;
import com.hanyang.belieme.demoserver.user.UserRepository;


@Entity
public class EventDB {
    @Id @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;    
    
    private int itemId;
    private int requesterId;
    private int responseManagerId;
    private int returnManagerId;
    private int lostManagerId;
    
    private long requestTimeStamp;
    private long responseTimeStamp;
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

    public int getRequesterId() {
        return requesterId;
    }

    public int getResponseManagerId() {
        return responseManagerId;
    }


    public int getReturnManagerId() {
        return returnManagerId;
    }

    
    public int getLostManagerId() {
        return lostManagerId;
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

    public void setItemId(int itemId) {
        this.itemId = itemId;
    }

    public void setRequesterId(int requesterId) {
        this.requesterId = requesterId;
    }

    public void setResponseManagerId(int responseManagerId) {
        this.responseManagerId = responseManagerId;
    }

    public void setReturnManagerId(int returnManagerId) {
        this.returnManagerId = returnManagerId;
    }
    
    public void setLostManagerId(int lostManagerId) {
        this.lostManagerId = lostManagerId;
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
    
    public void setLostTimeStampZero() {
        this.lostTimeStamp = 0;
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
    
    public void setLostTimeStampNow() {
        this.lostTimeStamp = System.currentTimeMillis()/1000;
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
    
    public Event toEvent(UniversityRepository universityRepository, DepartmentRepository departmentRepository, MajorRepository majorRepository, UserRepository userRepository, ThingRepository thingRepository, ItemRepository itemRepository, EventRepository eventRepository) throws NotFoundException {
        Event output = new Event();
        ItemNestedToEvent item;
        
        Optional<ItemDB> itemOptional = itemRepository.findById(itemId);
        if(itemOptional.isPresent()) {
            item = itemOptional.get().toItemNestedToEvent(universityRepository, departmentRepository, majorRepository, thingRepository, eventRepository);
        } else {
            item = null;
        }
        
        output.setRequester(null);
        output.setResponseManager(null);
        output.setReturnManager(null);
        output.setLostManager(null);
        if(requesterId != 0) {
            Optional<UserDB> tmpOptional = userRepository.findById(requesterId);
            if(tmpOptional.isPresent()) {
                output.setRequester(tmpOptional.get().toUserNestedToEvent());
            } else {
                throw new NotFoundException();
            }
        }        
        if(responseManagerId != 0) {
            Optional<UserDB> tmpOptional = userRepository.findById(responseManagerId);
            if(tmpOptional.isPresent()) {
                output.setResponseManager(tmpOptional.get().toUserNestedToEvent());
            } else {
                throw new NotFoundException();
            }
        }        
        if(returnManagerId != 0) {
            Optional<UserDB> tmpOptional = userRepository.findById(returnManagerId);
            if(tmpOptional.isPresent()) {
                output.setReturnManager(tmpOptional.get().toUserNestedToEvent());
            } else {
                throw new NotFoundException();
            }
        }        
        if(lostManagerId != 0) {
            Optional<UserDB> tmpOptional = userRepository.findById(lostManagerId);
            if(tmpOptional.isPresent()) {
                output.setLostManager(tmpOptional.get().toUserNestedToEvent());
            } else {
                throw new NotFoundException();
            }
        }
        output.setId(id);
        output.setItem(item);
        output.setRequestTimeStamp(requestTimeStamp);
        output.setResponseTimeStamp(responseTimeStamp);
        output.setReturnTimeStamp(returnTimeStamp);
        output.setCancelTimeStamp(cancelTimeStamp);
        output.setLostTimeStamp(lostTimeStamp);
        
        return output;
    }

    public EventNestedToItem toEventNestedToItem(UserRepository userRepository) throws NotFoundException {
        EventNestedToItem output = new EventNestedToItem();
        
        output.setRequester(null);
        output.setResponseManager(null);
        output.setReturnManager(null);
        output.setLostManager(null);
        if(requesterId != 0) {
            Optional<UserDB> tmpOptional = userRepository.findById(requesterId);
            if(tmpOptional.isPresent()) {
                output.setRequester(tmpOptional.get().toUserNestedToEvent());
            } else {
                throw new NotFoundException();
            }
        }        
        if(responseManagerId != 0) {
            Optional<UserDB> tmpOptional = userRepository.findById(responseManagerId);
            if(tmpOptional.isPresent()) {
                output.setResponseManager(tmpOptional.get().toUserNestedToEvent());
            } else {
                throw new NotFoundException();
            }
        }        
        if(returnManagerId != 0) {
            Optional<UserDB> tmpOptional = userRepository.findById(returnManagerId);
            if(tmpOptional.isPresent()) {
                output.setReturnManager(tmpOptional.get().toUserNestedToEvent());
            } else {
                throw new NotFoundException();
            }
        }        
        if(lostManagerId != 0) {
            Optional<UserDB> tmpOptional = userRepository.findById(lostManagerId);
            if(tmpOptional.isPresent()) {
                output.setLostManager(tmpOptional.get().toUserNestedToEvent());
            } else {
                throw new NotFoundException();
            }
        }
        
        output.setId(id);
        output.setRequestTimeStamp(requestTimeStamp);
        output.setResponseTimeStamp(responseTimeStamp);
        output.setReturnTimeStamp(returnTimeStamp);
        output.setCancelTimeStamp(cancelTimeStamp);
        output.setLostTimeStamp(lostTimeStamp);
        
        return output;
    }
}
