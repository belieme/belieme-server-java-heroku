package com.hanyang.belieme.demoserver.event;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import com.hanyang.belieme.demoserver.thing.*;
import com.hanyang.belieme.demoserver.university.UniversityRepository;
import com.hanyang.belieme.demoserver.user.User;
import com.hanyang.belieme.demoserver.user.UserDB;
import com.hanyang.belieme.demoserver.user.UserRepository;
import com.hanyang.belieme.demoserver.item.*;
import com.hanyang.belieme.demoserver.common.*;
import com.hanyang.belieme.demoserver.department.Department;
import com.hanyang.belieme.demoserver.department.DepartmentRepository;
import com.hanyang.belieme.demoserver.department.major.MajorRepository;
import com.hanyang.belieme.demoserver.exception.NotFoundException;
import com.hanyang.belieme.demoserver.exception.WrongInDataBaseException;

@RestController
@RequestMapping(path="/univs/{univCode}/depts/{deptCode}/events")
public class EventApiController {
    @Autowired
    private UniversityRepository universityRepository;
    
    @Autowired
    private DepartmentRepository departmentRepository;
    
    @Autowired
    private MajorRepository majorRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private ThingRepository thingRepository;

    @GetMapping("")
    public ResponseWrapper<List<Event>> getItems(@RequestHeader("user-token") String userToken, @PathVariable String univCode, @PathVariable String deptCode, @RequestParam(value = "userStudentId", required = false) String studentId) { //TODO value 바꾸기
        if(userToken == null) {
            return new ResponseWrapper<>(ResponseHeader.LACK_OF_REQUEST_HEADER_EXCEPTION, null);
        }
        
        int deptId;
        try {
            deptId = Department.findIdByUnivCodeAndDeptCode(universityRepository, departmentRepository, univCode, deptCode);
        } catch(NotFoundException e) {
            return new ResponseWrapper<>(ResponseHeader.NOT_FOUND_EXCEPTION, null);
        } catch(WrongInDataBaseException e) {
            return new ResponseWrapper<>(ResponseHeader.WRONG_IN_DATABASE_EXCEPTION, null);
        }
        
        int userId;
        try {
            userId = User.findIdByToken(userRepository, userToken);    
        } catch(NotFoundException e) {
            return new ResponseWrapper<>(ResponseHeader.EXPIRED_USER_TOKEN_EXCEPTION, null);
        } catch(WrongInDataBaseException e) {
            return new ResponseWrapper<>(ResponseHeader.WRONG_IN_DATABASE_EXCEPTION, null);
        }
        
        UserDB userDB = userRepository.findById(userId).get();
        User user;
        if(userDB == null) {
            return new ResponseWrapper<>(ResponseHeader.NOT_FOUND_EXCEPTION, null);
        } else {
            try {
                user = userDB.toUser(universityRepository, departmentRepository, majorRepository);    
            } catch(NotFoundException e) {
                return new ResponseWrapper<>(ResponseHeader.NOT_FOUND_EXCEPTION, null);
            }
        }
        
        boolean authorized = false;
        for(int i = 0; i < user.getDepartments().size(); i++) {
            if(deptId == user.getDepartments().get(i).getId()) {
                authorized = true;
            }
        }
        if(!authorized) {
            return new ResponseWrapper<>(ResponseHeader.USER_PERMISSION_DENIED_EXCEPTION, null);    
        }
        
        Iterable<EventDB> allEventList = eventRepository.findAll();
        Iterator<EventDB> iterator = allEventList.iterator();
        
        List<Event> output = new ArrayList<>();
        while(iterator.hasNext()) {
            EventDB eventDB = iterator.next();
            Event tmp;
            try {
                tmp = eventDB.toEvent(universityRepository, departmentRepository, majorRepository, userRepository, thingRepository, itemRepository, eventRepository);    
            } catch(NotFoundException e) {
                return new ResponseWrapper<>(ResponseHeader.NOT_FOUND_EXCEPTION, null);
            }
            
            if(tmp.getItem().getThing().getDepartment().getId() == deptId) { //TODO null pointer exception 발생 할 수도 있지 않을까?
                if(studentId == null || studentId.equals(tmp.getUser().getStudentId())) {
                    output.add(tmp);    
                }
            }
        }
        return new ResponseWrapper<>(ResponseHeader.OK, output);
    }

    @GetMapping("/{id}")
    public ResponseWrapper<Event> getItem(@RequestHeader("user-token") String userToken, @PathVariable String univCode, @PathVariable String deptCode, @PathVariable int id) {
        if(userToken == null) {
            return new ResponseWrapper<>(ResponseHeader.LACK_OF_REQUEST_HEADER_EXCEPTION, null);
        }
        
        int deptId;
        try {
            deptId = Department.findIdByUnivCodeAndDeptCode(universityRepository, departmentRepository, univCode, deptCode);
        } catch(NotFoundException e) {
            return new ResponseWrapper<>(ResponseHeader.NOT_FOUND_EXCEPTION, null);
        } catch(WrongInDataBaseException e) {
            return new ResponseWrapper<>(ResponseHeader.WRONG_IN_DATABASE_EXCEPTION, null);
        }
        
        int userId;
        try {
            userId = User.findIdByToken(userRepository, userToken);    
        } catch(NotFoundException e) {
            return new ResponseWrapper<>(ResponseHeader.EXPIRED_USER_TOKEN_EXCEPTION, null);
        } catch(WrongInDataBaseException e) {
            return new ResponseWrapper<>(ResponseHeader.WRONG_IN_DATABASE_EXCEPTION, null);
        }
        
        UserDB userDB = userRepository.findById(userId).get();
        User user;
        if(userDB == null) {
            return new ResponseWrapper<>(ResponseHeader.NOT_FOUND_EXCEPTION, null);
        } else {
            try {
                user = userDB.toUser(universityRepository, departmentRepository, majorRepository);    
            } catch(NotFoundException e) {
                return new ResponseWrapper<>(ResponseHeader.NOT_FOUND_EXCEPTION, null);
            }
        }
        
        boolean authorized = false;
        for(int i = 0; i < user.getDepartments().size(); i++) {
            if(deptId == user.getDepartments().get(i).getId()) {
                authorized = true;
            }
        }
        if(!authorized) {
            return new ResponseWrapper<>(ResponseHeader.USER_PERMISSION_DENIED_EXCEPTION, null);    
        }
        
        Optional<EventDB> eventOptional = eventRepository.findById(id);
        Event output;
        if(eventOptional.isPresent()) {
            try {
                output = eventOptional.get().toEvent(universityRepository, departmentRepository, majorRepository, userRepository, thingRepository, itemRepository, eventRepository);
            } catch(NotFoundException e) {
                return new ResponseWrapper<>(ResponseHeader.NOT_FOUND_EXCEPTION, null);
            }
            
            if(output.getItem().getThing().getDepartment().getId() == deptId) { //TODO null pointer exception 발생 할 수도 있지 않을까?
                 return new ResponseWrapper<>(ResponseHeader.OK, output);   
            }
            //TODO NotFoundException의 종류를 늘려야 하나? Exception바꿀까?
        }
        return new ResponseWrapper<>(ResponseHeader.NOT_FOUND_EXCEPTION, null);
    }

    
    //TODO 로그인 없이 관리자가 예약 신청 해주는 경우 추가하기
    @PostMapping("/reserve")
    public ResponseWrapper<Event> createRequestEvent(@RequestHeader("user-token") String userToken, @PathVariable String univCode, @PathVariable String deptCode, @RequestParam(value = "thingId", required = true) int thingId, @RequestParam(value = "itemNum", required = false) Integer itemNum) {
        if(userToken == null) {
            return new ResponseWrapper<>(ResponseHeader.LACK_OF_REQUEST_HEADER_EXCEPTION, null);
        }
        
        int deptId;
        try {
            deptId = Department.findIdByUnivCodeAndDeptCode(universityRepository, departmentRepository, univCode, deptCode);
        } catch(NotFoundException e) {
            return new ResponseWrapper<>(ResponseHeader.NOT_FOUND_EXCEPTION, null);
        } catch(WrongInDataBaseException e) {
            return new ResponseWrapper<>(ResponseHeader.WRONG_IN_DATABASE_EXCEPTION, null);
        }
        
        int userId;
        try {
            userId = User.findIdByToken(userRepository, userToken);    
        } catch(NotFoundException e) {
            return new ResponseWrapper<>(ResponseHeader.EXPIRED_USER_TOKEN_EXCEPTION, null);
        } catch(WrongInDataBaseException e) {
            return new ResponseWrapper<>(ResponseHeader.WRONG_IN_DATABASE_EXCEPTION, null);
        }
        
        UserDB userDB = userRepository.findById(userId).get();
        User user;
        if(userDB == null) {
            return new ResponseWrapper<>(ResponseHeader.NOT_FOUND_EXCEPTION, null);
        } else {
            try {
                user = userDB.toUser(universityRepository, departmentRepository, majorRepository);    
            } catch(NotFoundException e) {
                return new ResponseWrapper<>(ResponseHeader.NOT_FOUND_EXCEPTION, null);
            }
        }
        
        boolean authorized = false;
        for(int i = 0; i < user.getDepartments().size(); i++) {
            if(deptId == user.getDepartments().get(i).getId()) {
                authorized = true;
            }
        }
        if(!authorized) {
            return new ResponseWrapper<>(ResponseHeader.USER_PERMISSION_DENIED_EXCEPTION, null);    
        }
        
        List<EventDB> eventListByUserId = eventRepository.findByUserId(userId);
        int currentEventCount = 0;
        for(int i = 0; i < eventListByUserId.size(); i++) {
            Event tmp;
            
            try {
                tmp = eventListByUserId.get(i).toEvent(universityRepository, departmentRepository, majorRepository, userRepository, thingRepository, itemRepository, eventRepository);   
            } catch(NotFoundException e) {
                return new ResponseWrapper<>(ResponseHeader.NOT_FOUND_EXCEPTION, null);
            }
            
            if(tmp.getStatus().equals("RESERVED") || tmp.getStatus().equals("USING") || tmp.getStatus().equals("DELAYED") || tmp.getStatus().equals("LOST")) {
                currentEventCount++;
                if(tmp.getItem().getThing().getId() == thingId) { //TODO null pointer exception 발생 할 수도 있지 않을까?
                    return new ResponseWrapper<>(ResponseHeader.EVENT_FOR_SAME_THING_EXCEPTION, null);
                }
            }
        }
        if(currentEventCount >= 3) {
            return new ResponseWrapper<>(ResponseHeader.OVER_THREE_CURRENT_EVENT_EXCEPTION, null);
        }
        
        Item reservedItem = null;
        if(itemNum == null) {
            List<ItemDB> itemListByThingId = itemRepository.findByThingId(thingId);
            for(int i = 0; i < itemListByThingId.size(); i++) {
                try {
                    reservedItem = itemListByThingId.get(i).toItem(universityRepository, departmentRepository, majorRepository, userRepository, thingRepository, eventRepository);
                } catch(NotFoundException e) {
                    return new ResponseWrapper<>(ResponseHeader.NOT_FOUND_EXCEPTION, null);
                }
                if (reservedItem.getStatus().equals("USABLE")) {
                    break;
                }
                reservedItem = null;
            }
            if(reservedItem == null) {
                return new ResponseWrapper<>(ResponseHeader.ITEM_NOT_AVAILABLE_EXCEPTION, null);
            }
        } else {
            int itemId;
            try {
                itemId = Item.findIdByThingIdAndItemNum(itemRepository, thingId, itemNum);
            } catch(NotFoundException e) {
                return new ResponseWrapper<>(ResponseHeader.NOT_FOUND_EXCEPTION, null);
            } catch(WrongInDataBaseException e) {
                return new ResponseWrapper<>(ResponseHeader.WRONG_IN_DATABASE_EXCEPTION, null);
            }
            
            Optional<ItemDB> reservedItemOptional = itemRepository.findById(itemId);
            if(reservedItemOptional.isPresent()) {
                try {
                    reservedItem = reservedItemOptional.get().toItem(universityRepository, departmentRepository, majorRepository, userRepository, thingRepository, eventRepository);
                } catch(NotFoundException e) {
                    return new ResponseWrapper<>(ResponseHeader.NOT_FOUND_EXCEPTION, null);
                }
            } else {
                return new ResponseWrapper<>(ResponseHeader.NOT_FOUND_EXCEPTION, null);
            }
            
            if(!reservedItem.getStatus().equals("USABLE")) {
                return new ResponseWrapper<>(ResponseHeader.ITEM_NOT_AVAILABLE_EXCEPTION, null);
            }
        }

        EventDB newEventDB = new EventDB();
        
        newEventDB.setItemId(reservedItem.getId());
        newEventDB.setUserId(userId);
        newEventDB.setApproveManagerId(0);
        newEventDB.setReturnManagerId(0);
        newEventDB.setLostManagerId(0);
        newEventDB.setReserveTimeStampNow();
        newEventDB.setApproveTimeStampZero();
        newEventDB.setReturnTimeStampZero();
        newEventDB.setCancelTimeStampZero();
        newEventDB.setLostTimeStampZero();
            
        Event output;
        try {
            output = eventRepository.save(newEventDB).toEvent(universityRepository, departmentRepository, majorRepository, userRepository, thingRepository, itemRepository, eventRepository);   
        } catch(NotFoundException e) {
            return new ResponseWrapper<>(ResponseHeader.NOT_FOUND_EXCEPTION, null);
        }
        
        ItemDB updatedItemDB = new ItemDB();
        updatedItemDB.setId(reservedItem.getId());
        updatedItemDB.setNum(reservedItem.getNum());
        updatedItemDB.setThingId(reservedItem.getThing().getId());
        updatedItemDB.setLastEventId(output.getId());;
        
        itemRepository.save(updatedItemDB);
        
        return new ResponseWrapper<>(ResponseHeader.OK, output);
    }
    
    @PostMapping("/lost")
    public ResponseWrapper<Event> createLostEvent(@RequestHeader("user-token") String userToken, @PathVariable String univCode, @PathVariable String deptCode, @RequestParam(value = "thingId", required = true) int thingId, @RequestParam(value = "itemNum", required = true) int itemNum) {
        if(userToken == null) {
            return new ResponseWrapper<>(ResponseHeader.LACK_OF_REQUEST_HEADER_EXCEPTION, null);
        }
        
        int deptId;
        try {
            deptId = Department.findIdByUnivCodeAndDeptCode(universityRepository, departmentRepository, univCode, deptCode);
        } catch(NotFoundException e) {
            return new ResponseWrapper<>(ResponseHeader.NOT_FOUND_EXCEPTION, null);
        } catch(WrongInDataBaseException e) {
            return new ResponseWrapper<>(ResponseHeader.WRONG_IN_DATABASE_EXCEPTION, null);
        }
        
        int userId;
        try {
            userId = User.findIdByToken(userRepository, userToken);    
        } catch(NotFoundException e) {
            return new ResponseWrapper<>(ResponseHeader.EXPIRED_USER_TOKEN_EXCEPTION, null);
        } catch(WrongInDataBaseException e) {
            return new ResponseWrapper<>(ResponseHeader.WRONG_IN_DATABASE_EXCEPTION, null);
        }
        
        UserDB userDB = userRepository.findById(userId).get();
        User user;
        if(userDB == null) {
            return new ResponseWrapper<>(ResponseHeader.NOT_FOUND_EXCEPTION, null);
        } else {
            try {
                user = userDB.toUser(universityRepository, departmentRepository, majorRepository);    
            } catch(NotFoundException e) {
                return new ResponseWrapper<>(ResponseHeader.NOT_FOUND_EXCEPTION, null);
            }
        }
        
        boolean authorized = false;
        for(int i = 0; i < user.getDepartments().size(); i++) {
            if(deptId == user.getDepartments().get(i).getId()) {
                authorized = true;
            }
        }
        if(!authorized) {
            return new ResponseWrapper<>(ResponseHeader.USER_PERMISSION_DENIED_EXCEPTION, null);    
        }

        Optional<ThingDB> targetThingOptional = thingRepository.findById(thingId);
        if(!targetThingOptional.isPresent()) {
            return new ResponseWrapper<>(ResponseHeader.NOT_FOUND_EXCEPTION, null);
        } else if(targetThingOptional.get().getDepartmentId() != deptId) {
            return new ResponseWrapper<>(ResponseHeader.NOT_FOUND_EXCEPTION, null); //TODO Exception바꿀까?
        }
        
        Item lostItem;
        int itemId;
        try {
            itemId = Item.findIdByThingIdAndItemNum(itemRepository, thingId, itemNum);
        } catch(NotFoundException e) {
            return new ResponseWrapper<>(ResponseHeader.NOT_FOUND_EXCEPTION, null);
        } catch(WrongInDataBaseException e) {
            return new ResponseWrapper<>(ResponseHeader.WRONG_IN_DATABASE_EXCEPTION, null);
        }
            
        Optional<ItemDB> lostItemOptional = itemRepository.findById(itemId);
        if(lostItemOptional.isPresent()) {
            try {
                lostItem = lostItemOptional.get().toItem(universityRepository, departmentRepository, majorRepository, userRepository, thingRepository, eventRepository);
            } catch(NotFoundException e) {
                return new ResponseWrapper<>(ResponseHeader.NOT_FOUND_EXCEPTION, null);
            }
        } else {
            return new ResponseWrapper<>(ResponseHeader.NOT_FOUND_EXCEPTION, null);
        }   
        if(!lostItem.getStatus().equals("USABLE")) {
            return new ResponseWrapper<>(ResponseHeader.ITEM_NOT_AVAILABLE_EXCEPTION, null);
        }
        
        EventDB newEventDB = new EventDB();
        
        newEventDB.setItemId(lostItem.getId());
        newEventDB.setUserId(0);
        newEventDB.setApproveManagerId(0);
        newEventDB.setReturnManagerId(0);
        newEventDB.setLostManagerId(userId);
        newEventDB.setReserveTimeStampZero();
        newEventDB.setApproveTimeStampZero();
        newEventDB.setReturnTimeStampZero();
        newEventDB.setCancelTimeStampZero();
        newEventDB.setLostTimeStampNow();
        
        Event output;
        try {
            output = eventRepository.save(newEventDB).toEvent(universityRepository, departmentRepository, majorRepository, userRepository, thingRepository, itemRepository, eventRepository);   
        } catch(NotFoundException e) {
            return new ResponseWrapper<>(ResponseHeader.NOT_FOUND_EXCEPTION, null);
        }
            
        ItemDB updatedItemDB = new ItemDB();
        updatedItemDB.setId(lostItem.getId());
        updatedItemDB.setNum(lostItem.getNum());
        updatedItemDB.setThingId(lostItem.getThing().getId());
        updatedItemDB.setLastEventId(output.getId());;
        
        itemRepository.save(updatedItemDB);
        
        return new ResponseWrapper<>(ResponseHeader.OK, output);
    }

    @PatchMapping("/{id}/cancel")
    public ResponseWrapper<Event> cancelItem(@RequestHeader("user-token") String userToken, @PathVariable String univCode, @PathVariable String deptCode, @PathVariable int id) {
        if(userToken == null) {
            return new ResponseWrapper<>(ResponseHeader.LACK_OF_REQUEST_HEADER_EXCEPTION, null);
        }
        
        int deptId;
        try {
            deptId = Department.findIdByUnivCodeAndDeptCode(universityRepository, departmentRepository, univCode, deptCode);
        } catch(NotFoundException e) {
            return new ResponseWrapper<>(ResponseHeader.NOT_FOUND_EXCEPTION, null);
        } catch(WrongInDataBaseException e) {
            return new ResponseWrapper<>(ResponseHeader.WRONG_IN_DATABASE_EXCEPTION, null);
        }
        
        int userId;
        try {
            userId = User.findIdByToken(userRepository, userToken);    
        } catch(NotFoundException e) {
            return new ResponseWrapper<>(ResponseHeader.EXPIRED_USER_TOKEN_EXCEPTION, null);
        } catch(WrongInDataBaseException e) {
            return new ResponseWrapper<>(ResponseHeader.WRONG_IN_DATABASE_EXCEPTION, null);
        }
        
        UserDB userDB = userRepository.findById(userId).get();
        User user;
        if(userDB == null) {
            return new ResponseWrapper<>(ResponseHeader.NOT_FOUND_EXCEPTION, null);
        } else {
            try {
                user = userDB.toUser(universityRepository, departmentRepository, majorRepository);    
            } catch(NotFoundException e) {
                return new ResponseWrapper<>(ResponseHeader.NOT_FOUND_EXCEPTION, null);
            }
        }
        
        boolean authorized = false;
        for(int i = 0; i < user.getDepartments().size(); i++) {
            if(deptId == user.getDepartments().get(i).getId()) {
                authorized = true;
            }
        }
        if(!authorized) {
            return new ResponseWrapper<>(ResponseHeader.USER_PERMISSION_DENIED_EXCEPTION, null);    
        }
        
        Optional<EventDB> eventBeforeUpdateOptional = eventRepository.findById(id);
        if(eventBeforeUpdateOptional.isPresent()) {
            EventDB eventToUpdate = eventBeforeUpdateOptional.get();
            if(eventToUpdate.getUserId() != userId) { //TODO 관리자가 취소할 수 있게 바꾸기
                return new ResponseWrapper<>(ResponseHeader.USER_PERMISSION_DENIED_EXCEPTION, null);
            }
            
            Event eventBeforeUpdate;
            try {
                eventBeforeUpdate = eventToUpdate.toEvent(universityRepository, departmentRepository, majorRepository, userRepository, thingRepository, itemRepository, eventRepository);
            } catch(NotFoundException e) {
                return new ResponseWrapper<>(ResponseHeader.NOT_FOUND_EXCEPTION, null);
            }
            if(eventBeforeUpdate.getItem().getThing().getDepartment().getId() != deptId) { //TODO null pointer exception 발생 할 수도 있지 않을까?
                return new ResponseWrapper<>(ResponseHeader.NOT_FOUND_EXCEPTION, null); //TODO Exception바꿀까?
            }
            
            if(eventToUpdate.getStatus().equals("RESERVED")) {
                eventToUpdate.setCancelTimeStampNow();
                
                Event output; 
                try {
                    output = eventRepository.save(eventToUpdate).toEvent(universityRepository, departmentRepository, majorRepository, userRepository, thingRepository, itemRepository, eventRepository);
                } catch(NotFoundException e) {
                    return new ResponseWrapper<>(ResponseHeader.NOT_FOUND_EXCEPTION, null);
                }
                return new ResponseWrapper<>(ResponseHeader.OK, output);
            }
            else {
                return new ResponseWrapper<>(ResponseHeader.WRONG_EVENT_STATUS_EXCEPTION, null);
            }
        }
        else {
            return new ResponseWrapper<>(ResponseHeader.NOT_FOUND_EXCEPTION, null);
        }
    }

    @PatchMapping("/{id}/approve")
    public ResponseWrapper<Event> responseItem(@RequestHeader("user-token") String userToken, @PathVariable String univCode, @PathVariable String deptCode, @PathVariable int id) {
        if(userToken == null) {
            return new ResponseWrapper<>(ResponseHeader.LACK_OF_REQUEST_HEADER_EXCEPTION, null);
        }
        
        int deptId;
        try {
            deptId = Department.findIdByUnivCodeAndDeptCode(universityRepository, departmentRepository, univCode, deptCode);
        } catch(NotFoundException e) {
            return new ResponseWrapper<>(ResponseHeader.NOT_FOUND_EXCEPTION, null);
        } catch(WrongInDataBaseException e) {
            return new ResponseWrapper<>(ResponseHeader.WRONG_IN_DATABASE_EXCEPTION, null);
        }
        
        int userId;
        try {
            userId = User.findIdByToken(userRepository, userToken);    
        } catch(NotFoundException e) {
            return new ResponseWrapper<>(ResponseHeader.EXPIRED_USER_TOKEN_EXCEPTION, null);
        } catch(WrongInDataBaseException e) {
            return new ResponseWrapper<>(ResponseHeader.WRONG_IN_DATABASE_EXCEPTION, null);
        }
        
        UserDB userDB = userRepository.findById(userId).get();
        User user;
        if(userDB == null) {
            return new ResponseWrapper<>(ResponseHeader.NOT_FOUND_EXCEPTION, null);
        } else {
            try {
                user = userDB.toUser(universityRepository, departmentRepository, majorRepository);    
            } catch(NotFoundException e) {
                return new ResponseWrapper<>(ResponseHeader.NOT_FOUND_EXCEPTION, null);
            }
        }
        
        boolean authorized = false;
        for(int i = 0; i < user.getDepartments().size(); i++) {
            if(deptId == user.getDepartments().get(i).getId()) {
                authorized = true;
            }
        }
        if(!authorized) {
            return new ResponseWrapper<>(ResponseHeader.USER_PERMISSION_DENIED_EXCEPTION, null);    
        }
        
        // TODO permission 확인하기
        
        Optional<EventDB> eventBeforeUpdateOptional = eventRepository.findById(id);
        if(eventBeforeUpdateOptional.isPresent()) {
            EventDB eventToUpdate = eventBeforeUpdateOptional.get();
            
            Event eventBeforeUpdate;
            try {
                eventBeforeUpdate = eventToUpdate.toEvent(universityRepository, departmentRepository, majorRepository, userRepository, thingRepository, itemRepository, eventRepository);
            } catch(NotFoundException e) {
                return new ResponseWrapper<>(ResponseHeader.NOT_FOUND_EXCEPTION, null);
            }
            if(eventBeforeUpdate.getItem().getThing().getDepartment().getId() != deptId) { //TODO null pointer exception 발생 할 수도 있지 않을까?
                return new ResponseWrapper<>(ResponseHeader.NOT_FOUND_EXCEPTION, null); //TODO Exception바꿀까?
            }
            
            if(eventToUpdate.getStatus().equals("RESERVED")) {
                eventToUpdate.setApproveTimeStampNow();
                eventToUpdate.setApproveManagerId(userId);
                
                Event output;
                try {
                    output = eventRepository.save(eventToUpdate).toEvent(universityRepository, departmentRepository, majorRepository, userRepository, thingRepository, itemRepository, eventRepository);
                } catch(NotFoundException e) {
                    return new ResponseWrapper<>(ResponseHeader.NOT_FOUND_EXCEPTION, null);
                }
                return new ResponseWrapper<>(ResponseHeader.OK, output);
            }
            else {
                return new ResponseWrapper<>(ResponseHeader.WRONG_EVENT_STATUS_EXCEPTION, null);
            }
        }
        else {
            return new ResponseWrapper<>(ResponseHeader.NOT_FOUND_EXCEPTION, null);
        }
    }

    @PatchMapping("/{id}/return")
    public ResponseWrapper<Event> returnItem(@RequestHeader("user-token") String userToken, @PathVariable String univCode, @PathVariable String deptCode, @PathVariable int id) {
        if(userToken == null) {
            return new ResponseWrapper<>(ResponseHeader.LACK_OF_REQUEST_HEADER_EXCEPTION, null);
        }
        
        int deptId;
        try {
            deptId = Department.findIdByUnivCodeAndDeptCode(universityRepository, departmentRepository, univCode, deptCode);
        } catch(NotFoundException e) {
            return new ResponseWrapper<>(ResponseHeader.NOT_FOUND_EXCEPTION, null);
        } catch(WrongInDataBaseException e) {
            return new ResponseWrapper<>(ResponseHeader.WRONG_IN_DATABASE_EXCEPTION, null);
        }
        
        int userId;
        try {
            userId = User.findIdByToken(userRepository, userToken);    
        } catch(NotFoundException e) {
            return new ResponseWrapper<>(ResponseHeader.EXPIRED_USER_TOKEN_EXCEPTION, null);
        } catch(WrongInDataBaseException e) {
            return new ResponseWrapper<>(ResponseHeader.WRONG_IN_DATABASE_EXCEPTION, null);
        }
        
        UserDB userDB = userRepository.findById(userId).get();
        User user;
        if(userDB == null) {
            return new ResponseWrapper<>(ResponseHeader.NOT_FOUND_EXCEPTION, null);
        } else {
            try {
                user = userDB.toUser(universityRepository, departmentRepository, majorRepository);    
            } catch(NotFoundException e) {
                return new ResponseWrapper<>(ResponseHeader.NOT_FOUND_EXCEPTION, null);
            }
        }
        
        boolean authorized = false;
        for(int i = 0; i < user.getDepartments().size(); i++) {
            if(deptId == user.getDepartments().get(i).getId()) {
                authorized = true;
            }
        }
        if(!authorized) {
            return new ResponseWrapper<>(ResponseHeader.USER_PERMISSION_DENIED_EXCEPTION, null);    
        }
        
        // TODO permission 확인하기
        
        Optional<EventDB> eventBeforeUpdateOptional = eventRepository.findById(id);
        if(eventBeforeUpdateOptional.isPresent()) {
            EventDB eventToUpdate = eventBeforeUpdateOptional.get();
            
            Event eventBeforeUpdate;
            try {
                eventBeforeUpdate = eventToUpdate.toEvent(universityRepository, departmentRepository, majorRepository, userRepository, thingRepository, itemRepository, eventRepository);
            } catch(NotFoundException e) {
                return new ResponseWrapper<>(ResponseHeader.NOT_FOUND_EXCEPTION, null);
            }
            if(eventBeforeUpdate.getItem().getThing().getDepartment().getId() != deptId) { //TODO null pointer exception 발생 할 수도 있지 않을까?
                return new ResponseWrapper<>(ResponseHeader.NOT_FOUND_EXCEPTION, null); //TODO Exception바꿀까?
            }
            
            if(eventToUpdate.getStatus().equals("USING") || eventToUpdate.getStatus().equals("DELAYED")) {
                eventToUpdate.setReturnTimeStampNow();
                eventToUpdate.setReturnManagerId(userId);
                
                Event output;
                try {
                    output = eventRepository.save(eventToUpdate).toEvent(universityRepository, departmentRepository, majorRepository, userRepository, thingRepository, itemRepository, eventRepository);
                } catch(NotFoundException e) {
                    return new ResponseWrapper<>(ResponseHeader.NOT_FOUND_EXCEPTION, null);
                }
                return new ResponseWrapper<>(ResponseHeader.OK, output);
            }
            else {
                return new ResponseWrapper<>(ResponseHeader.WRONG_EVENT_STATUS_EXCEPTION, null);
            }
        }
        else {
            return new ResponseWrapper<>(ResponseHeader.NOT_FOUND_EXCEPTION, null);
        }
    }
    
    @PatchMapping("{id}/lost")
    public ResponseWrapper<Event> lostItem(@RequestHeader("user-token") String userToken, @PathVariable String univCode, @PathVariable String deptCode, @PathVariable int id) {
        if(userToken == null) {
            return new ResponseWrapper<>(ResponseHeader.LACK_OF_REQUEST_HEADER_EXCEPTION, null);
        }
        
        int deptId;
        try {
            deptId = Department.findIdByUnivCodeAndDeptCode(universityRepository, departmentRepository, univCode, deptCode);
        } catch(NotFoundException e) {
            return new ResponseWrapper<>(ResponseHeader.NOT_FOUND_EXCEPTION, null);
        } catch(WrongInDataBaseException e) {
            return new ResponseWrapper<>(ResponseHeader.WRONG_IN_DATABASE_EXCEPTION, null);
        }
        
        int userId;
        try {
            userId = User.findIdByToken(userRepository, userToken);    
        } catch(NotFoundException e) {
            return new ResponseWrapper<>(ResponseHeader.EXPIRED_USER_TOKEN_EXCEPTION, null);
        } catch(WrongInDataBaseException e) {
            return new ResponseWrapper<>(ResponseHeader.WRONG_IN_DATABASE_EXCEPTION, null);
        }
        
        UserDB userDB = userRepository.findById(userId).get();
        User user;
        if(userDB == null) {
            return new ResponseWrapper<>(ResponseHeader.NOT_FOUND_EXCEPTION, null);
        } else {
            try {
                user = userDB.toUser(universityRepository, departmentRepository, majorRepository);    
            } catch(NotFoundException e) {
                return new ResponseWrapper<>(ResponseHeader.NOT_FOUND_EXCEPTION, null);
            }
        }
        
        boolean authorized = false;
        for(int i = 0; i < user.getDepartments().size(); i++) {
            if(deptId == user.getDepartments().get(i).getId()) {
                authorized = true;
            }
        }
        if(!authorized) {
            return new ResponseWrapper<>(ResponseHeader.USER_PERMISSION_DENIED_EXCEPTION, null);    
        }
        
        // TODO permission 확인하기
        
        Optional<EventDB> eventBeforeUpdateOptional = eventRepository.findById(id);
        if(eventBeforeUpdateOptional.isPresent()) {
            EventDB eventToUpdate = eventBeforeUpdateOptional.get();
            
            Event eventBeforeUpdate;
            try {
                eventBeforeUpdate = eventToUpdate.toEvent(universityRepository, departmentRepository, majorRepository, userRepository, thingRepository, itemRepository, eventRepository);
            } catch(NotFoundException e) {
                return new ResponseWrapper<>(ResponseHeader.NOT_FOUND_EXCEPTION, null);
            }
            if(eventBeforeUpdate.getItem().getThing().getDepartment().getId() != deptId) { //TODO null pointer exception 발생 할 수도 있지 않을까?
                return new ResponseWrapper<>(ResponseHeader.NOT_FOUND_EXCEPTION, null); //TODO Exception바꿀까?
            }
            
            if(eventToUpdate.getStatus().equals("USING") || eventToUpdate.getStatus().equals("DELAYED")) {
                eventToUpdate.setLostTimeStampNow();
                eventToUpdate.setLostManagerId(userId);
                
                Event output;
                try {
                    output = eventRepository.save(eventToUpdate).toEvent(universityRepository, departmentRepository, majorRepository, userRepository, thingRepository, itemRepository, eventRepository);
                } catch(NotFoundException e) {
                    return new ResponseWrapper<>(ResponseHeader.NOT_FOUND_EXCEPTION, null);
                }
                return new ResponseWrapper<>(ResponseHeader.OK, output);
            }
            else {
                return new ResponseWrapper<>(ResponseHeader.WRONG_EVENT_STATUS_EXCEPTION, null);
            }
        }
        else {
            return new ResponseWrapper<>(ResponseHeader.NOT_FOUND_EXCEPTION, null);
        }
    }
    
    @PatchMapping("/{id}/found")
    public ResponseWrapper<Event> foundItem(@RequestHeader("user-token") String userToken, @PathVariable String univCode, @PathVariable String deptCode, @PathVariable int id) {
        if(userToken == null) {
            return new ResponseWrapper<>(ResponseHeader.LACK_OF_REQUEST_HEADER_EXCEPTION, null);
        }
        
        int deptId;
        try {
            deptId = Department.findIdByUnivCodeAndDeptCode(universityRepository, departmentRepository, univCode, deptCode);
        } catch(NotFoundException e) {
            return new ResponseWrapper<>(ResponseHeader.NOT_FOUND_EXCEPTION, null);
        } catch(WrongInDataBaseException e) {
            return new ResponseWrapper<>(ResponseHeader.WRONG_IN_DATABASE_EXCEPTION, null);
        }
        
        int userId;
        try {
            userId = User.findIdByToken(userRepository, userToken);    
        } catch(NotFoundException e) {
            return new ResponseWrapper<>(ResponseHeader.EXPIRED_USER_TOKEN_EXCEPTION, null);
        } catch(WrongInDataBaseException e) {
            return new ResponseWrapper<>(ResponseHeader.WRONG_IN_DATABASE_EXCEPTION, null);
        }
        
        UserDB userDB = userRepository.findById(userId).get();
        User user;
        if(userDB == null) {
            return new ResponseWrapper<>(ResponseHeader.NOT_FOUND_EXCEPTION, null);
        } else {
            try {
                user = userDB.toUser(universityRepository, departmentRepository, majorRepository);    
            } catch(NotFoundException e) {
                return new ResponseWrapper<>(ResponseHeader.NOT_FOUND_EXCEPTION, null);
            }
        }
        
        boolean authorized = false;
        for(int i = 0; i < user.getDepartments().size(); i++) {
            if(deptId == user.getDepartments().get(i).getId()) {
                authorized = true;
            }
        }
        if(!authorized) {
            return new ResponseWrapper<>(ResponseHeader.USER_PERMISSION_DENIED_EXCEPTION, null);    
        }
        
        // TODO permission 확인하기
        
        Optional<EventDB> eventBeforeUpdateOptional = eventRepository.findById(id);
        if(eventBeforeUpdateOptional.isPresent()) {
            EventDB eventToUpdate = eventBeforeUpdateOptional.get();
            
            Event eventBeforeUpdate;
            try {
                eventBeforeUpdate = eventToUpdate.toEvent(universityRepository, departmentRepository, majorRepository, userRepository, thingRepository, itemRepository, eventRepository);
            } catch(NotFoundException e) {
                return new ResponseWrapper<>(ResponseHeader.NOT_FOUND_EXCEPTION, null);
            }
            if(eventBeforeUpdate.getItem().getThing().getDepartment().getId() != deptId) { //TODO null pointer exception 발생 할 수도 있지 않을까?
                return new ResponseWrapper<>(ResponseHeader.NOT_FOUND_EXCEPTION, null); //TODO Exception바꿀까?
            }
            
            if(eventToUpdate.getStatus().equals("LOST")) {
                eventToUpdate.setReturnTimeStampNow();
                eventToUpdate.setReturnManagerId(userId);
                
                Event output;
                try {
                    output = eventRepository.save(eventToUpdate).toEvent(universityRepository, departmentRepository, majorRepository, userRepository, thingRepository, itemRepository, eventRepository);
                } catch(NotFoundException e) {
                    return new ResponseWrapper<>(ResponseHeader.NOT_FOUND_EXCEPTION, null);
                }
                return new ResponseWrapper<>(ResponseHeader.OK, output);
            }
            else {
                return new ResponseWrapper<>(ResponseHeader.WRONG_EVENT_STATUS_EXCEPTION, null);
            }
        }
        else {
            return new ResponseWrapper<>(ResponseHeader.NOT_FOUND_EXCEPTION, null);
        }
    }
}