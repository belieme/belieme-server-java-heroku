package com.hanyang.belieme.demoserver.event;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import com.hanyang.belieme.demoserver.thing.*;
import com.hanyang.belieme.demoserver.university.University;
import com.hanyang.belieme.demoserver.university.UniversityRepository;
import com.hanyang.belieme.demoserver.user.User;
import com.hanyang.belieme.demoserver.user.UserDB;
import com.hanyang.belieme.demoserver.user.UserRepository;
import com.hanyang.belieme.demoserver.user.permission.PermissionRepository;
import com.hanyang.belieme.demoserver.item.*;
import com.hanyang.belieme.demoserver.common.*;
import com.hanyang.belieme.demoserver.department.DepartmentDB;
import com.hanyang.belieme.demoserver.department.DepartmentRepository;
import com.hanyang.belieme.demoserver.department.DepartmentResponse;
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
    private PermissionRepository permissionRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private ThingRepository thingRepository;

    @GetMapping("")
    public ResponseWrapper<ListResponse> getItems(@RequestHeader("user-token") String userToken, @PathVariable String univCode, @PathVariable String deptCode, @RequestParam(value = "studentId", required = false) String studentId) { //TODO value 바꾸기
        if(userToken == null) {
            return new ResponseWrapper<>(ResponseHeader.LACK_OF_REQUEST_HEADER_EXCEPTION, null);
        }
        
        University univ;
        try {
            univ = University.findByUnivCode(universityRepository, univCode);
        } catch(NotFoundException e) {
            return new ResponseWrapper<>(ResponseHeader.NOT_FOUND_EXCEPTION, null);
        } catch(WrongInDataBaseException e) {
            return new ResponseWrapper<>(ResponseHeader.WRONG_IN_DATABASE_EXCEPTION, null);
        }
        
        DepartmentResponse dept;
        try {
            dept = DepartmentDB.findByUnivCodeAndDeptCode(universityRepository, departmentRepository, univCode, deptCode).toDepartmentResponse(majorRepository);
        } catch(NotFoundException e) {
            return new ResponseWrapper<>(ResponseHeader.NOT_FOUND_EXCEPTION, null);
        } catch(WrongInDataBaseException e) {
            return new ResponseWrapper<>(ResponseHeader.WRONG_IN_DATABASE_EXCEPTION, null);
        }
        
        User user;
        try {
            user = UserDB.findByToken(userRepository, userToken).toUser(departmentRepository, majorRepository, permissionRepository);    
        } catch(NotFoundException e) {
            return new ResponseWrapper<>(ResponseHeader.EXPIRED_USER_TOKEN_EXCEPTION, null);
        } catch(WrongInDataBaseException e) {
            return new ResponseWrapper<>(ResponseHeader.WRONG_IN_DATABASE_EXCEPTION, null);
        }
        
        if(!(user.hasStaffPermission(deptCode) || (user.hasUserPermission(deptCode) && user.getStudentId().equals(studentId)))) {
            return new ResponseWrapper<>(ResponseHeader.USER_PERMISSION_DENIED_EXCEPTION, null);
        }
        
        Iterable<EventDB> allEventList = eventRepository.findAll();
        Iterator<EventDB> iterator = allEventList.iterator();
        
        List<Event> output = new ArrayList<>();
        while(iterator.hasNext()) {
            EventDB eventDB = iterator.next();
            Event tmp;
            try {
                tmp = eventDB.toEvent(userRepository, thingRepository, itemRepository, eventRepository);    
            } catch (NotFoundException e) {
                return new ResponseWrapper<>(ResponseHeader.NOT_FOUND_EXCEPTION, null);
            }            
            
            if(tmp.getThing().deptIdGetter() == dept.getId()) { //TODO null pointer exception 발생 할 수도 있지 않을까?
                if(studentId == null || (tmp.getUser() != null && studentId.equals(tmp.getUser().getStudentId()))) {
                    output.add(tmp);    
                }
            }
        }
        return new ResponseWrapper<>(ResponseHeader.OK, new ListResponse(univ, dept, output));
    }

    @GetMapping("/{id}")
    public ResponseWrapper<Response> getItem(@RequestHeader("user-token") String userToken, @PathVariable String univCode, @PathVariable String deptCode, @PathVariable int id) {
        if(userToken == null) {
            return new ResponseWrapper<>(ResponseHeader.LACK_OF_REQUEST_HEADER_EXCEPTION, null);
        }
        
        University univ;
        try {
            univ = University.findByUnivCode(universityRepository, univCode);
        } catch(NotFoundException e) {
            return new ResponseWrapper<>(ResponseHeader.NOT_FOUND_EXCEPTION, null);
        } catch(WrongInDataBaseException e) {
            return new ResponseWrapper<>(ResponseHeader.WRONG_IN_DATABASE_EXCEPTION, null);
        }
        
        DepartmentResponse dept;
        try {
            dept = DepartmentDB.findByUnivCodeAndDeptCode(universityRepository, departmentRepository, univCode, deptCode).toDepartmentResponse(majorRepository);
        } catch(NotFoundException e) {
            return new ResponseWrapper<>(ResponseHeader.NOT_FOUND_EXCEPTION, null);
        } catch(WrongInDataBaseException e) {
            return new ResponseWrapper<>(ResponseHeader.WRONG_IN_DATABASE_EXCEPTION, null);
        }
        
        User user;
        try {
            user = UserDB.findByToken(userRepository, userToken).toUser(departmentRepository, majorRepository, permissionRepository);    
        } catch(NotFoundException e) {
            return new ResponseWrapper<>(ResponseHeader.EXPIRED_USER_TOKEN_EXCEPTION, null);
        } catch(WrongInDataBaseException e) {
            return new ResponseWrapper<>(ResponseHeader.WRONG_IN_DATABASE_EXCEPTION, null);
        }
        
        Optional<EventDB> eventOptional = eventRepository.findById(id);
        Event output;
        if(eventOptional.isPresent()) {
            try {
                output = eventOptional.get().toEvent(userRepository, thingRepository, itemRepository, eventRepository);    
            } catch(NotFoundException e) {
                return new ResponseWrapper<>(ResponseHeader.NOT_FOUND_EXCEPTION, null);
            }
            
            if(output.getThing().deptIdGetter() == dept.getId()) { //TODO null pointer exception 발생 할 수도 있지 않을까?
                if(user.hasStaffPermission(deptCode) || (user.hasUserPermission(deptCode) && output.getUser() != null && output.getUser().getId() == user.getId())) {
                    return new ResponseWrapper<>(ResponseHeader.OK, new Response(univ, dept, output));       
                }
                else {
                    return new ResponseWrapper<>(ResponseHeader.USER_PERMISSION_DENIED_EXCEPTION, null);
                }
            }
            //TODO NotFoundException의 종류를 늘려야 하나? Exception바꿀까?
        }
        return new ResponseWrapper<>(ResponseHeader.NOT_FOUND_EXCEPTION, null);
    }

    
    //TODO 로그인 없이 관리자가 예약 신청 해주는 경우 추가하기
    @PostMapping("/reserve")
    public ResponseWrapper<Response> createRequestEvent(@RequestHeader("user-token") String userToken, @PathVariable String univCode, @PathVariable String deptCode, @RequestBody EventPostRequestBody requestBody) {
        if(userToken == null) {
            return new ResponseWrapper<>(ResponseHeader.LACK_OF_REQUEST_HEADER_EXCEPTION, null);
        }
        
        if(requestBody.getThingId() == null) {
            return new ResponseWrapper<>(ResponseHeader.LACK_OF_REQUEST_BODY_EXCEPTION, null);
        }
        
        University univ;
        try {
            univ = University.findByUnivCode(universityRepository, univCode);
        } catch(NotFoundException e) {
            return new ResponseWrapper<>(ResponseHeader.NOT_FOUND_EXCEPTION, null);
        } catch(WrongInDataBaseException e) {
            return new ResponseWrapper<>(ResponseHeader.WRONG_IN_DATABASE_EXCEPTION, null);
        }
        
        DepartmentResponse dept;
        try {
            dept = DepartmentDB.findByUnivCodeAndDeptCode(universityRepository, departmentRepository, univCode, deptCode).toDepartmentResponse(majorRepository);
        } catch(NotFoundException e) {
            return new ResponseWrapper<>(ResponseHeader.NOT_FOUND_EXCEPTION, null);
        } catch(WrongInDataBaseException e) {
            return new ResponseWrapper<>(ResponseHeader.WRONG_IN_DATABASE_EXCEPTION, null);
        }
        
        User user;
        try {
            user = UserDB.findByToken(userRepository, userToken).toUser(departmentRepository, majorRepository, permissionRepository);    
        } catch(NotFoundException e) {
            return new ResponseWrapper<>(ResponseHeader.EXPIRED_USER_TOKEN_EXCEPTION, null);
        } catch(WrongInDataBaseException e) {
            return new ResponseWrapper<>(ResponseHeader.WRONG_IN_DATABASE_EXCEPTION, null);
        }
        
        if(!user.hasUserPermission(deptCode)) {
            return new ResponseWrapper<>(ResponseHeader.USER_PERMISSION_DENIED_EXCEPTION, null);
        }
        
        List<EventDB> eventListByUserId = eventRepository.findByUserId(user.getId());
        int currentEventCount = 0;
        for(int i = 0; i < eventListByUserId.size(); i++) {
            Event tmp;
            try {
                tmp = eventListByUserId.get(i).toEvent(userRepository, thingRepository, itemRepository, eventRepository);  
            } catch(NotFoundException e) {
                return new ResponseWrapper<>(ResponseHeader.NOT_FOUND_EXCEPTION, null);
            }
            
            if(tmp.getStatus().equals("RESERVED") || tmp.getStatus().equals("USING") || tmp.getStatus().equals("DELAYED") || tmp.getStatus().equals("LOST")) {
                currentEventCount++;
                if(tmp.getThing().getId() == requestBody.getThingId()) { //TODO null pointer exception 발생 할 수도 있지 않을까?
                    return new ResponseWrapper<>(ResponseHeader.EVENT_FOR_SAME_THING_EXCEPTION, null);
                }
            }
        }
        if(currentEventCount >= 3) {
            return new ResponseWrapper<>(ResponseHeader.OVER_THREE_CURRENT_EVENT_EXCEPTION, null);
        }
        
        Item reservedItem = null;
        if(requestBody.getItemNum() == null) {
            List<ItemDB> itemListByThingId = itemRepository.findByThingId(requestBody.getThingId());
            for(int i = 0; i < itemListByThingId.size(); i++) {
                reservedItem = itemListByThingId.get(i).toItem(userRepository, eventRepository);
                if (reservedItem.getStatus().equals("USABLE")) {
                    break;
                }
                reservedItem = null;
            }
            if(reservedItem == null) {
                return new ResponseWrapper<>(ResponseHeader.ITEM_NOT_AVAILABLE_EXCEPTION, null);
            }
        } else {
            try {
                reservedItem = ItemDB.findByThingIdAndItemNum(itemRepository, requestBody.getThingId(), requestBody.getItemNum()).toItem(userRepository, eventRepository);
            } catch(NotFoundException e) {
                return new ResponseWrapper<>(ResponseHeader.NOT_FOUND_EXCEPTION, null);
            } catch(WrongInDataBaseException e) {
                return new ResponseWrapper<>(ResponseHeader.WRONG_IN_DATABASE_EXCEPTION, null);
            }
            
            if(!reservedItem.getStatus().equals("USABLE")) {
                return new ResponseWrapper<>(ResponseHeader.ITEM_NOT_AVAILABLE_EXCEPTION, null);
            }
        }

        EventDB newEventDB = new EventDB();
        
        newEventDB.setItemId(reservedItem.getId());
        newEventDB.setUserId(user.getId());
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
            output = eventRepository.save(newEventDB).toEvent(userRepository, thingRepository, itemRepository, eventRepository);  
        } catch(NotFoundException e) {
            return new ResponseWrapper<>(ResponseHeader.NOT_FOUND_EXCEPTION, null);
        }
        
        ItemDB updatedItemDB = new ItemDB();
        updatedItemDB.setId(reservedItem.getId());
        updatedItemDB.setNum(reservedItem.getNum());
        updatedItemDB.setThingId(reservedItem.thingIdGetter());
        updatedItemDB.setLastEventId(output.getId());;
        
        itemRepository.save(updatedItemDB);
        
        return new ResponseWrapper<>(ResponseHeader.OK, new Response(univ, dept, output));
    }
    
    @PostMapping("/lost")
    public ResponseWrapper<Response> createLostEvent(@RequestHeader("user-token") String userToken, @PathVariable String univCode, @PathVariable String deptCode, @RequestBody EventPostRequestBody requestBody) {
        if(userToken == null) {
            return new ResponseWrapper<>(ResponseHeader.LACK_OF_REQUEST_HEADER_EXCEPTION, null);
        }
        
        if(requestBody.getThingId() == null || requestBody.getItemNum() == null) {
            return new ResponseWrapper<>(ResponseHeader.LACK_OF_REQUEST_BODY_EXCEPTION, null);
        }
        
        University univ;
        try {
            univ = University.findByUnivCode(universityRepository, univCode);
        } catch(NotFoundException e) {
            return new ResponseWrapper<>(ResponseHeader.NOT_FOUND_EXCEPTION, null);
        } catch(WrongInDataBaseException e) {
            return new ResponseWrapper<>(ResponseHeader.WRONG_IN_DATABASE_EXCEPTION, null);
        }
        
        DepartmentResponse dept;
        try {
            dept = DepartmentDB.findByUnivCodeAndDeptCode(universityRepository, departmentRepository, univCode, deptCode).toDepartmentResponse(majorRepository);
        } catch(NotFoundException e) {
            return new ResponseWrapper<>(ResponseHeader.NOT_FOUND_EXCEPTION, null);
        } catch(WrongInDataBaseException e) {
            return new ResponseWrapper<>(ResponseHeader.WRONG_IN_DATABASE_EXCEPTION, null);
        }
        
        User user;
        try {
            user = UserDB.findByToken(userRepository, userToken).toUser(departmentRepository, majorRepository, permissionRepository);    
        } catch(NotFoundException e) {
            return new ResponseWrapper<>(ResponseHeader.EXPIRED_USER_TOKEN_EXCEPTION, null);
        } catch(WrongInDataBaseException e) {
            return new ResponseWrapper<>(ResponseHeader.WRONG_IN_DATABASE_EXCEPTION, null);
        }
        
        if(!user.hasStaffPermission(deptCode)) {
            return new ResponseWrapper<>(ResponseHeader.USER_PERMISSION_DENIED_EXCEPTION, null);
        }

        Optional<ThingDB> targetThingOptional = thingRepository.findById(requestBody.getThingId());
        if(!targetThingOptional.isPresent()) {
            return new ResponseWrapper<>(ResponseHeader.NOT_FOUND_EXCEPTION, null);
        } else if(targetThingOptional.get().getDepartmentId() != dept.getId()) {
            return new ResponseWrapper<>(ResponseHeader.NOT_FOUND_EXCEPTION, null); //TODO Exception바꿀까?
        }
        
        Item lostItem;
        try {
            lostItem = ItemDB.findByThingIdAndItemNum(itemRepository, requestBody.getThingId(), requestBody.getItemNum()).toItem(userRepository, eventRepository);
        } catch(NotFoundException e) {
            return new ResponseWrapper<>(ResponseHeader.NOT_FOUND_EXCEPTION, null);
        } catch(WrongInDataBaseException e) {
            return new ResponseWrapper<>(ResponseHeader.WRONG_IN_DATABASE_EXCEPTION, null);
        }
            
        if(!lostItem.getStatus().equals("USABLE")) {
            return new ResponseWrapper<>(ResponseHeader.ITEM_NOT_AVAILABLE_EXCEPTION, null);
        }
        
        EventDB newEventDB = new EventDB();
        
        newEventDB.setItemId(lostItem.getId());
        newEventDB.setUserId(0);
        newEventDB.setApproveManagerId(0);
        newEventDB.setReturnManagerId(0);
        newEventDB.setLostManagerId(user.getId());
        newEventDB.setReserveTimeStampZero();
        newEventDB.setApproveTimeStampZero();
        newEventDB.setReturnTimeStampZero();
        newEventDB.setCancelTimeStampZero();
        newEventDB.setLostTimeStampNow();
        
        Event output;
        try {
            output = eventRepository.save(newEventDB).toEvent(userRepository, thingRepository, itemRepository, eventRepository);
        } catch(NotFoundException e) {
            return new ResponseWrapper<>(ResponseHeader.NOT_FOUND_EXCEPTION, null);
        }
            
        ItemDB updatedItemDB = new ItemDB();
        updatedItemDB.setId(lostItem.getId());
        updatedItemDB.setNum(lostItem.getNum());
        updatedItemDB.setThingId(lostItem.thingIdGetter());
        updatedItemDB.setLastEventId(output.getId());;
        
        itemRepository.save(updatedItemDB);
        
        return new ResponseWrapper<>(ResponseHeader.OK, new Response(univ, dept, output));
    }

    @PatchMapping("/{id}/cancel") //TODO 논의점 cancel manager를 만들어야 하는가?
    public ResponseWrapper<Response> cancelItem(@RequestHeader("user-token") String userToken, @PathVariable String univCode, @PathVariable String deptCode, @PathVariable int id) {
        if(userToken == null) {
            return new ResponseWrapper<>(ResponseHeader.LACK_OF_REQUEST_HEADER_EXCEPTION, null);
        }
        
        University univ;
        try {
            univ = University.findByUnivCode(universityRepository, univCode);
        } catch(NotFoundException e) {
            return new ResponseWrapper<>(ResponseHeader.NOT_FOUND_EXCEPTION, null);
        } catch(WrongInDataBaseException e) {
            return new ResponseWrapper<>(ResponseHeader.WRONG_IN_DATABASE_EXCEPTION, null);
        }
        
        DepartmentResponse dept;
        try {
            dept = DepartmentDB.findByUnivCodeAndDeptCode(universityRepository, departmentRepository, univCode, deptCode).toDepartmentResponse(majorRepository);
        } catch(NotFoundException e) {
            return new ResponseWrapper<>(ResponseHeader.NOT_FOUND_EXCEPTION, null);
        } catch(WrongInDataBaseException e) {
            return new ResponseWrapper<>(ResponseHeader.WRONG_IN_DATABASE_EXCEPTION, null);
        }
        
        User user;
        try {
            user = UserDB.findByToken(userRepository, userToken).toUser(departmentRepository, majorRepository, permissionRepository);    
        } catch(NotFoundException e) {
            return new ResponseWrapper<>(ResponseHeader.EXPIRED_USER_TOKEN_EXCEPTION, null);
        } catch(WrongInDataBaseException e) {
            return new ResponseWrapper<>(ResponseHeader.WRONG_IN_DATABASE_EXCEPTION, null);
        }
        
        Optional<EventDB> eventBeforeUpdateOptional = eventRepository.findById(id);
        if(eventBeforeUpdateOptional.isPresent()) {
            EventDB eventToUpdate = eventBeforeUpdateOptional.get();
            if(!(user.hasStaffPermission(deptCode) || (user.hasUserPermission(deptCode) && eventToUpdate.getUserId() == user.getId()))) {
                return new ResponseWrapper<>(ResponseHeader.USER_PERMISSION_DENIED_EXCEPTION, null);
            }
            
            Event eventBeforeUpdate;
            try {
                eventBeforeUpdate = eventToUpdate.toEvent(userRepository, thingRepository, itemRepository, eventRepository);
            } catch(NotFoundException e) {
                return new ResponseWrapper<>(ResponseHeader.NOT_FOUND_EXCEPTION, null);
            }
            
            if(eventBeforeUpdate.getThing().deptIdGetter() != dept.getId()) { //TODO null pointer exception 발생 할 수도 있지 않을까?
                return new ResponseWrapper<>(ResponseHeader.NOT_FOUND_EXCEPTION, null); //TODO Exception바꿀까?
            }
            
            if(eventToUpdate.getStatus().equals("RESERVED")) {
                eventToUpdate.setCancelTimeStampNow();
                
                Event output;
                try {
                    output = eventRepository.save(eventToUpdate).toEvent(userRepository, thingRepository, itemRepository, eventRepository);
                } catch(NotFoundException e) {
                    return new ResponseWrapper<>(ResponseHeader.NOT_FOUND_EXCEPTION, null);
                }
    
                return new ResponseWrapper<>(ResponseHeader.OK, new Response(univ, dept, output));
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
    public ResponseWrapper<Response> responseItem(@RequestHeader("user-token") String userToken, @PathVariable String univCode, @PathVariable String deptCode, @PathVariable int id) {
        if(userToken == null) {
            return new ResponseWrapper<>(ResponseHeader.LACK_OF_REQUEST_HEADER_EXCEPTION, null);
        }
        
        University univ;
        try {
            univ = University.findByUnivCode(universityRepository, univCode);
        } catch(NotFoundException e) {
            return new ResponseWrapper<>(ResponseHeader.NOT_FOUND_EXCEPTION, null);
        } catch(WrongInDataBaseException e) {
            return new ResponseWrapper<>(ResponseHeader.WRONG_IN_DATABASE_EXCEPTION, null);
        }
        
        DepartmentResponse dept;
        try {
            dept = DepartmentDB.findByUnivCodeAndDeptCode(universityRepository, departmentRepository, univCode, deptCode).toDepartmentResponse(majorRepository);
        } catch(NotFoundException e) {
            return new ResponseWrapper<>(ResponseHeader.NOT_FOUND_EXCEPTION, null);
        } catch(WrongInDataBaseException e) {
            return new ResponseWrapper<>(ResponseHeader.WRONG_IN_DATABASE_EXCEPTION, null);
        }
        
        User user;
        try {
            user = UserDB.findByToken(userRepository, userToken).toUser(departmentRepository, majorRepository, permissionRepository);    
        } catch(NotFoundException e) {
            return new ResponseWrapper<>(ResponseHeader.EXPIRED_USER_TOKEN_EXCEPTION, null);
        } catch(WrongInDataBaseException e) {
            return new ResponseWrapper<>(ResponseHeader.WRONG_IN_DATABASE_EXCEPTION, null);
        }
        
        if(!user.hasStaffPermission(deptCode)) {
            return new ResponseWrapper<>(ResponseHeader.USER_PERMISSION_DENIED_EXCEPTION, null);
        }
        
        Optional<EventDB> eventBeforeUpdateOptional = eventRepository.findById(id);
        if(eventBeforeUpdateOptional.isPresent()) {
            EventDB eventToUpdate = eventBeforeUpdateOptional.get();
            
            Event eventBeforeUpdate;
            try {
                eventBeforeUpdate = eventToUpdate.toEvent(userRepository, thingRepository, itemRepository, eventRepository);
            } catch(NotFoundException e) {
                return new ResponseWrapper<>(ResponseHeader.NOT_FOUND_EXCEPTION, null);
            }
            
            if(eventBeforeUpdate.getThing().deptIdGetter() != dept.getId()) { //TODO null pointer exception 발생 할 수도 있지 않을까?
                return new ResponseWrapper<>(ResponseHeader.NOT_FOUND_EXCEPTION, null); //TODO Exception바꿀까?
            }
            
            if(eventToUpdate.getStatus().equals("RESERVED")) {
                eventToUpdate.setApproveTimeStampNow();
                eventToUpdate.setApproveManagerId(user.getId());
                
                Event output;
                try {
                    output = eventRepository.save(eventToUpdate).toEvent(userRepository, thingRepository, itemRepository, eventRepository);
                } catch(NotFoundException e) {
                    return new ResponseWrapper<>(ResponseHeader.NOT_FOUND_EXCEPTION, null);
                }
    
                return new ResponseWrapper<>(ResponseHeader.OK, new Response(univ, dept, output));
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
    public ResponseWrapper<Response> returnItem(@RequestHeader("user-token") String userToken, @PathVariable String univCode, @PathVariable String deptCode, @PathVariable int id) {
        if(userToken == null) {
            return new ResponseWrapper<>(ResponseHeader.LACK_OF_REQUEST_HEADER_EXCEPTION, null);
        }
        
        University univ;
        try {
            univ = University.findByUnivCode(universityRepository, univCode);
        } catch(NotFoundException e) {
            return new ResponseWrapper<>(ResponseHeader.NOT_FOUND_EXCEPTION, null);
        } catch(WrongInDataBaseException e) {
            return new ResponseWrapper<>(ResponseHeader.WRONG_IN_DATABASE_EXCEPTION, null);
        }
        
        DepartmentResponse dept;
        try {
            dept = DepartmentDB.findByUnivCodeAndDeptCode(universityRepository, departmentRepository, univCode, deptCode).toDepartmentResponse(majorRepository);
        } catch(NotFoundException e) {
            return new ResponseWrapper<>(ResponseHeader.NOT_FOUND_EXCEPTION, null);
        } catch(WrongInDataBaseException e) {
            return new ResponseWrapper<>(ResponseHeader.WRONG_IN_DATABASE_EXCEPTION, null);
        }
        
        User user;
        try {
            user = UserDB.findByToken(userRepository, userToken).toUser(departmentRepository, majorRepository, permissionRepository);    
        } catch(NotFoundException e) {
            return new ResponseWrapper<>(ResponseHeader.EXPIRED_USER_TOKEN_EXCEPTION, null);
        } catch(WrongInDataBaseException e) {
            return new ResponseWrapper<>(ResponseHeader.WRONG_IN_DATABASE_EXCEPTION, null);
        }
        
        if(!user.hasStaffPermission(deptCode)) {
            return new ResponseWrapper<>(ResponseHeader.USER_PERMISSION_DENIED_EXCEPTION, null);
        }
        
        Optional<EventDB> eventBeforeUpdateOptional = eventRepository.findById(id);
        if(eventBeforeUpdateOptional.isPresent()) {
            EventDB eventToUpdate = eventBeforeUpdateOptional.get();
            
            Event eventBeforeUpdate;
            try {
                eventBeforeUpdate = eventToUpdate.toEvent(userRepository, thingRepository, itemRepository, eventRepository);
            } catch(NotFoundException e) {
                return new ResponseWrapper<>(ResponseHeader.NOT_FOUND_EXCEPTION, null);
            }
            
            if(eventBeforeUpdate.getThing().deptIdGetter() != dept.getId()) { //TODO null pointer exception 발생 할 수도 있지 않을까?
                return new ResponseWrapper<>(ResponseHeader.NOT_FOUND_EXCEPTION, null); //TODO Exception바꿀까?
            }
            
            if(eventToUpdate.getStatus().equals("USING") || eventToUpdate.getStatus().equals("DELAYED")) {
                eventToUpdate.setReturnTimeStampNow();
                eventToUpdate.setReturnManagerId(user.getId());
                
                Event output;
                try {
                    output = eventRepository.save(eventToUpdate).toEvent(userRepository, thingRepository, itemRepository, eventRepository);
                } catch(NotFoundException e) {
                    return new ResponseWrapper<>(ResponseHeader.NOT_FOUND_EXCEPTION, null);
                }
    
                return new ResponseWrapper<>(ResponseHeader.OK, new Response(univ, dept, output));
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
    public ResponseWrapper<Response> lostItem(@RequestHeader("user-token") String userToken, @PathVariable String univCode, @PathVariable String deptCode, @PathVariable int id) {
        if(userToken == null) {
            return new ResponseWrapper<>(ResponseHeader.LACK_OF_REQUEST_HEADER_EXCEPTION, null);
        }
        
        University univ;
        try {
            univ = University.findByUnivCode(universityRepository, univCode);
        } catch(NotFoundException e) {
            return new ResponseWrapper<>(ResponseHeader.NOT_FOUND_EXCEPTION, null);
        } catch(WrongInDataBaseException e) {
            return new ResponseWrapper<>(ResponseHeader.WRONG_IN_DATABASE_EXCEPTION, null);
        }
        
        DepartmentResponse dept;
        try {
            dept = DepartmentDB.findByUnivCodeAndDeptCode(universityRepository, departmentRepository, univCode, deptCode).toDepartmentResponse(majorRepository);
        } catch(NotFoundException e) {
            return new ResponseWrapper<>(ResponseHeader.NOT_FOUND_EXCEPTION, null);
        } catch(WrongInDataBaseException e) {
            return new ResponseWrapper<>(ResponseHeader.WRONG_IN_DATABASE_EXCEPTION, null);
        }
        
        User user;
        try {
            user = UserDB.findByToken(userRepository, userToken).toUser(departmentRepository, majorRepository, permissionRepository);    
        } catch(NotFoundException e) {
            return new ResponseWrapper<>(ResponseHeader.EXPIRED_USER_TOKEN_EXCEPTION, null);
        } catch(WrongInDataBaseException e) {
            return new ResponseWrapper<>(ResponseHeader.WRONG_IN_DATABASE_EXCEPTION, null);
        }
        
        if(!user.hasStaffPermission(deptCode)) {
            return new ResponseWrapper<>(ResponseHeader.USER_PERMISSION_DENIED_EXCEPTION, null);
        }
        
        Optional<EventDB> eventBeforeUpdateOptional = eventRepository.findById(id);
        if(eventBeforeUpdateOptional.isPresent()) {
            EventDB eventToUpdate = eventBeforeUpdateOptional.get();
            
            Event eventBeforeUpdate;
            try {
                eventBeforeUpdate = eventToUpdate.toEvent(userRepository, thingRepository, itemRepository, eventRepository);
            } catch(NotFoundException e) {
                return new ResponseWrapper<>(ResponseHeader.NOT_FOUND_EXCEPTION, null);
            }
            
            if(eventBeforeUpdate.getThing().deptIdGetter() != dept.getId()) { //TODO null pointer exception 발생 할 수도 있지 않을까?
                return new ResponseWrapper<>(ResponseHeader.NOT_FOUND_EXCEPTION, null); //TODO Exception바꿀까?
            }
            
            if(eventToUpdate.getStatus().equals("USING") || eventToUpdate.getStatus().equals("DELAYED")) {
                eventToUpdate.setLostTimeStampNow();
                eventToUpdate.setLostManagerId(user.getId());
                
                Event output;
                try {
                    output = eventRepository.save(eventToUpdate).toEvent(userRepository, thingRepository, itemRepository, eventRepository);
                } catch(NotFoundException e) {
                    return new ResponseWrapper<>(ResponseHeader.NOT_FOUND_EXCEPTION, null);
                }
    
                return new ResponseWrapper<>(ResponseHeader.OK, new Response(univ, dept, output));
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
    public ResponseWrapper<Response> foundItem(@RequestHeader("user-token") String userToken, @PathVariable String univCode, @PathVariable String deptCode, @PathVariable int id) {
        if(userToken == null) {
            return new ResponseWrapper<>(ResponseHeader.LACK_OF_REQUEST_HEADER_EXCEPTION, null);
        }
        
        University univ;
        try {
            univ = University.findByUnivCode(universityRepository, univCode);
        } catch(NotFoundException e) {
            return new ResponseWrapper<>(ResponseHeader.NOT_FOUND_EXCEPTION, null);
        } catch(WrongInDataBaseException e) {
            return new ResponseWrapper<>(ResponseHeader.WRONG_IN_DATABASE_EXCEPTION, null);
        }
        
        DepartmentResponse dept;
        try {
            dept = DepartmentDB.findByUnivCodeAndDeptCode(universityRepository, departmentRepository, univCode, deptCode).toDepartmentResponse(majorRepository);
        } catch(NotFoundException e) {
            return new ResponseWrapper<>(ResponseHeader.NOT_FOUND_EXCEPTION, null);
        } catch(WrongInDataBaseException e) {
            return new ResponseWrapper<>(ResponseHeader.WRONG_IN_DATABASE_EXCEPTION, null);
        }
        
        User user;
        try {
            user = UserDB.findByToken(userRepository, userToken).toUser(departmentRepository, majorRepository, permissionRepository);    
        } catch(NotFoundException e) {
            return new ResponseWrapper<>(ResponseHeader.EXPIRED_USER_TOKEN_EXCEPTION, null);
        } catch(WrongInDataBaseException e) {
            return new ResponseWrapper<>(ResponseHeader.WRONG_IN_DATABASE_EXCEPTION, null);
        }
        
        if(!user.hasStaffPermission(deptCode)) {
            return new ResponseWrapper<>(ResponseHeader.USER_PERMISSION_DENIED_EXCEPTION, null);
        }
        
        Optional<EventDB> eventBeforeUpdateOptional = eventRepository.findById(id);
        if(eventBeforeUpdateOptional.isPresent()) {
            EventDB eventToUpdate = eventBeforeUpdateOptional.get();
            
            Event eventBeforeUpdate;
            try {
                eventBeforeUpdate = eventToUpdate.toEvent(userRepository, thingRepository, itemRepository, eventRepository);
            } catch(NotFoundException e) {
                return new ResponseWrapper<>(ResponseHeader.NOT_FOUND_EXCEPTION, null);
            }
            
            if(eventBeforeUpdate.getThing().deptIdGetter() != dept.getId()) { //TODO null pointer exception 발생 할 수도 있지 않을까?
                return new ResponseWrapper<>(ResponseHeader.NOT_FOUND_EXCEPTION, null); //TODO Exception바꿀까?
            }
            
            if(eventToUpdate.getStatus().equals("LOST")) {
                eventToUpdate.setReturnTimeStampNow();
                eventToUpdate.setReturnManagerId(user.getId());
                
                Event output;
                try {
                    output = eventRepository.save(eventToUpdate).toEvent(userRepository, thingRepository, itemRepository, eventRepository);
                } catch(NotFoundException e) {
                    return new ResponseWrapper<>(ResponseHeader.NOT_FOUND_EXCEPTION, null);
                }
    
                return new ResponseWrapper<>(ResponseHeader.OK, new Response(univ, dept, output));
            }
            else {
                return new ResponseWrapper<>(ResponseHeader.WRONG_EVENT_STATUS_EXCEPTION, null);
            }
        }
        else {
            return new ResponseWrapper<>(ResponseHeader.NOT_FOUND_EXCEPTION, null);
        }
    }
    
    public class Response {
        University university;
        DepartmentResponse department;
        Event event;

        public Response(University university, DepartmentResponse department, Event event) {
            this.university = new University(university);
            this.department = new DepartmentResponse(department);
            this.event = new Event(event);
        }

        public University getUniversity() {
            if(university == null) {
                return null;
            }
            return new University(university);
        }

        public DepartmentResponse getDepartment() {
            if(department == null) {
                return null;
            }
            return new DepartmentResponse(department);
        }
        
        public Event getEvent() {
            if(event == null) {
                return null;
            }
            return new Event(event);
        }
    }
    
    public class ListResponse {
        University university;
        DepartmentResponse department;
        List<Event> events;

        public ListResponse(University university, DepartmentResponse department, List<Event> events) {
            this.university = university;
            this.department = department;
            this.events = new ArrayList<>(events);
        }

        public University getUniversity() {
            if(university == null) {
                return null;
            }
            return new University(university);
        }
        
        public DepartmentResponse getDepartment() {
            if(department == null) {
                return null;
            }
            return new DepartmentResponse(department);
        }

        public List<Event> getEvents() {
            return new ArrayList<>(events);
        }
    }
}