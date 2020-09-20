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
import com.hanyang.belieme.demoserver.user.permission.PermissionRepository;
import com.hanyang.belieme.demoserver.item.*;
import com.hanyang.belieme.demoserver.common.*;
import com.hanyang.belieme.demoserver.department.Department;
import com.hanyang.belieme.demoserver.department.DepartmentRepository;
import com.hanyang.belieme.demoserver.department.major.MajorRepository;
import com.hanyang.belieme.demoserver.exception.NotFoundException;
import com.hanyang.belieme.demoserver.exception.WrongInDataBaseException;


@RestController
@RequestMapping(path="/beta/univs/{univCode}/depts/{deptCode}/events")
public class BetaEventApiController {
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
             user = userDB.toUser(universityRepository, departmentRepository, majorRepository, permissionRepository);    
        }
        
        if(!(user.hasStaffPermission(deptCode) || (user.hasUserPermission(deptCode) && user.getStudentId().equals(studentId)))) {
            return new ResponseWrapper<>(ResponseHeader.USER_PERMISSION_DENIED_EXCEPTION, null);
        }
        
        Iterable<EventDB> allEventList = eventRepository.findAll();
        Iterator<EventDB> iterator = allEventList.iterator();
        
        List<Event> output = new ArrayList<>();
        while(iterator.hasNext()) {
            EventDB eventDB = iterator.next();
            Event tmp = eventDB.toEvent(universityRepository, departmentRepository, majorRepository, userRepository, thingRepository, itemRepository, eventRepository);    
            
            if(tmp.getItem().getThing().getDepartment().getId() == deptId) {
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
             user = userDB.toUser(universityRepository, departmentRepository, majorRepository, permissionRepository);    
        }
               
        Optional<EventDB> eventOptional = eventRepository.findById(id);
        Event output;
        if(eventOptional.isPresent()) {
            output = eventOptional.get().toEvent(universityRepository, departmentRepository, majorRepository, userRepository, thingRepository, itemRepository, eventRepository);
            
            if(output.getItem().getThing().getDepartment().getId() == deptId) { //TODO null pointer exception 발생 할 수도 있지 않을까?
                if(user.hasStaffPermission(deptCode) || (user.hasUserPermission(deptCode) && output.getUser().getId() == userId)) {
                    return new ResponseWrapper<>(ResponseHeader.OK, output);       
                }
                else {
                    return new ResponseWrapper<>(ResponseHeader.USER_PERMISSION_DENIED_EXCEPTION, null);
                }   
            }
            //TODO NotFoundException의 종류를 늘려야 하나? Exception바꿀까?
        }
        return new ResponseWrapper<>(ResponseHeader.NOT_FOUND_EXCEPTION, null);
    }

@PostMapping("/reserve")
    public ResponseWrapper<PostMappingResponse> createRequestEvent(@RequestHeader("user-token") String userToken, @PathVariable String univCode, @PathVariable String deptCode, @RequestParam(value = "thingId", required = true) int thingId, @RequestParam(value = "itemNum", required = false) Integer itemNum) {
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
             user = userDB.toUser(universityRepository, departmentRepository, majorRepository, permissionRepository);    
        }
        
        if(!user.hasUserPermission(deptCode)) {
            return new ResponseWrapper<>(ResponseHeader.USER_PERMISSION_DENIED_EXCEPTION, null);
        }

        Optional<ThingDB> targetThingOptional = thingRepository.findById(thingId);
        if(!targetThingOptional.isPresent()) {
            return new ResponseWrapper<>(ResponseHeader.NOT_FOUND_EXCEPTION, null);
        } else if(targetThingOptional.get().getDepartmentId() != deptId) { //TODO null pointer exception 발생 할 수도 있지 않을까?
            return new ResponseWrapper<>(ResponseHeader.NOT_FOUND_EXCEPTION, null); //TODO Exception바꿀까?
        }
        
        List<EventDB> eventListByUserId = eventRepository.findByUserId(userId);
        int currentEventCount = 0;
        for(int i = 0; i < eventListByUserId.size(); i++) {
            Event tmp = eventListByUserId.get(i).toEvent(universityRepository, departmentRepository, majorRepository, userRepository, thingRepository, itemRepository, eventRepository);   

            
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
                reservedItem = itemListByThingId.get(i).toItem(universityRepository, departmentRepository, majorRepository, userRepository, thingRepository, eventRepository);

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
                reservedItem = reservedItemOptional.get().toItem(universityRepository, departmentRepository, majorRepository, userRepository, thingRepository, eventRepository);

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
            
        Event eventOutput = eventRepository.save(newEventDB).toEvent(universityRepository, departmentRepository, majorRepository, userRepository, thingRepository, itemRepository, eventRepository);
        
            
        ItemDB updatedItemDB = new ItemDB();
        updatedItemDB.setId(reservedItem.getId());
        updatedItemDB.setNum(reservedItem.getNum());
        updatedItemDB.setThingId(reservedItem.getThing().getId());
        updatedItemDB.setLastEventId(eventOutput.getId());
        
        itemRepository.save(updatedItemDB);
            
        ArrayList<Thing> thingListOutput = new ArrayList<>();
        Iterable<ThingDB> allThingDBList = thingRepository.findAll();
        Iterator<ThingDB> iterator = allThingDBList.iterator();
        while(iterator.hasNext()) {
            Thing tmp = iterator.next().toThing(universityRepository, departmentRepository, majorRepository, userRepository, thingRepository, itemRepository, eventRepository);

            if(tmp.getDepartment().getId() == deptId) {
                thingListOutput.add(tmp);   
            }
        }
        return new ResponseWrapper<>(ResponseHeader.OK, new PostMappingResponse(eventOutput, thingListOutput));
    }
    
    @PostMapping("/lost")
    public ResponseWrapper<PostMappingResponse> createLostEvent(@RequestHeader("user-token") String userToken, @PathVariable String univCode, @PathVariable String deptCode, @RequestParam(value = "thingId", required = true) int thingId, @RequestParam(value = "itemNum", required = true) int itemNum) {
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
             user = userDB.toUser(universityRepository, departmentRepository, majorRepository, permissionRepository);    
        }
        
        if(!user.hasStaffPermission(deptCode)) {
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
            lostItem = lostItemOptional.get().toItem(universityRepository, departmentRepository, majorRepository, userRepository, thingRepository, eventRepository);
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
        
        Event eventOutput = eventRepository.save(newEventDB).toEvent(universityRepository, departmentRepository, majorRepository, userRepository, thingRepository, itemRepository, eventRepository);    
    
            
        ItemDB updatedItemDB = new ItemDB();
        updatedItemDB.setId(lostItem.getId());
        updatedItemDB.setNum(lostItem.getNum());
        updatedItemDB.setThingId(lostItem.getThing().getId());
        updatedItemDB.setLastEventId(eventOutput.getId());;
        
        itemRepository.save(updatedItemDB);
        
        ArrayList<Thing> thingListOutput = new ArrayList<>();
        Iterable<ThingDB> allThingDBList = thingRepository.findAll();
        Iterator<ThingDB> iterator = allThingDBList.iterator();
        while(iterator.hasNext()) {
            Thing tmp = iterator.next().toThing(universityRepository, departmentRepository, majorRepository, userRepository, thingRepository, itemRepository, eventRepository);
            
            if(tmp.getDepartment().getId() == deptId) {
                thingListOutput.add(tmp);   
            }
        }
        return new ResponseWrapper<>(ResponseHeader.OK, new PostMappingResponse(eventOutput, thingListOutput));
    }

    @PatchMapping("/{id}/cancel") //TODO 논의점 cancel manager를 만들어야 하는가?
    public ResponseWrapper<List<Event>> cancelItem(@RequestHeader("user-token") String userToken, @PathVariable String univCode, @PathVariable String deptCode, @PathVariable int id) {
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
             user = userDB.toUser(universityRepository, departmentRepository, majorRepository, permissionRepository);    
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
            if(!(user.hasStaffPermission(deptCode) || (user.hasUserPermission(deptCode) && eventToUpdate.getUserId() == userId))) {
                return new ResponseWrapper<>(ResponseHeader.USER_PERMISSION_DENIED_EXCEPTION, null);
            }
            
            Event eventBeforeUpdate = eventToUpdate.toEvent(universityRepository, departmentRepository, majorRepository, userRepository, thingRepository, itemRepository, eventRepository);
            
            if(eventBeforeUpdate.getItem().getThing().getDepartment().getId() != deptId) { //TODO null pointer exception 발생 할 수도 있지 않을까?
                return new ResponseWrapper<>(ResponseHeader.NOT_FOUND_EXCEPTION, null); //TODO Exception바꿀까?
            }
            
            if(eventToUpdate.getStatus().equals("RESERVED")) {
                eventToUpdate.setCancelTimeStampNow();
                eventRepository.save(eventToUpdate);
                List<Event> output = new ArrayList<>();
                List<EventDB> eventDBList = eventRepository.findByUserId(eventToUpdate.getUserId());
                for(int i = 0; i < eventDBList.size(); i++) {
                    Event tmp = eventDBList.get(i).toEvent(universityRepository, departmentRepository, majorRepository, userRepository, thingRepository, itemRepository, eventRepository);
                    
                    if(tmp.getItem().getThing().getDepartment().getId() == deptId) {
                        output.add(tmp);
                    }
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
    public ResponseWrapper<Iterable<Event>> responseItem(@RequestHeader("user-token") String userToken, @PathVariable String univCode, @PathVariable String deptCode, @PathVariable int id) {
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
             user = userDB.toUser(universityRepository, departmentRepository, majorRepository, permissionRepository);    
        }
        
        if(!user.hasStaffPermission(deptCode)) {
            return new ResponseWrapper<>(ResponseHeader.USER_PERMISSION_DENIED_EXCEPTION, null);
        }
        
        Optional<EventDB> eventBeforeUpdateOptional = eventRepository.findById(id);
        if(eventBeforeUpdateOptional.isPresent()) {
            EventDB eventToUpdate = eventBeforeUpdateOptional.get();
            
            Event eventBeforeUpdate = eventToUpdate.toEvent(universityRepository, departmentRepository, majorRepository, userRepository, thingRepository, itemRepository, eventRepository);
            
            if(eventBeforeUpdate.getItem().getThing().getDepartment().getId() != deptId) {
                return new ResponseWrapper<>(ResponseHeader.NOT_FOUND_EXCEPTION, null); //TODO Exception바꿀까?
            }
            
            if(eventToUpdate.getStatus().equals("RESERVED")) {
                eventToUpdate.setApproveTimeStampNow();
                eventToUpdate.setApproveManagerId(userId);
                eventRepository.save(eventToUpdate);
                
                List<Event> output = new ArrayList<>();
                Iterable<EventDB> eventDBList = eventRepository.findAll();
                Iterator<EventDB> iterator = eventDBList.iterator();
                while (iterator.hasNext()) {
                    Event tmp = iterator.next().toEvent(universityRepository, departmentRepository, majorRepository, userRepository, thingRepository, itemRepository, eventRepository);
                    
                    
                    if(tmp.getItem().getThing().getDepartment().getId() == deptId) {
                        output.add(tmp);
                    }
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
    public ResponseWrapper<Iterable<Event>> returnItem(@RequestHeader("user-token") String userToken, @PathVariable String univCode, @PathVariable String deptCode, @PathVariable int id) {
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
             user = userDB.toUser(universityRepository, departmentRepository, majorRepository, permissionRepository);    
        }
        
        if(!user.hasStaffPermission(deptCode)) {
            return new ResponseWrapper<>(ResponseHeader.USER_PERMISSION_DENIED_EXCEPTION, null);
        }
        
        Optional<EventDB> eventBeforeUpdateOptional = eventRepository.findById(id);
        if(eventBeforeUpdateOptional.isPresent()) {
            EventDB eventToUpdate = eventBeforeUpdateOptional.get();
            
            Event eventBeforeUpdate = eventToUpdate.toEvent(universityRepository, departmentRepository, majorRepository, userRepository, thingRepository, itemRepository, eventRepository);
            
            if(eventBeforeUpdate.getItem().getThing().getDepartment().getId() != deptId) { //TODO null pointer exception 발생 할 수도 있지 않을까?
                return new ResponseWrapper<>(ResponseHeader.NOT_FOUND_EXCEPTION, null); //TODO Exception바꿀까?
            }
            
            if(eventToUpdate.getStatus().equals("USING") || eventToUpdate.getStatus().equals("DELAYED")) {
                eventToUpdate.setReturnTimeStampNow();
                eventToUpdate.setReturnManagerId(userId);
                eventRepository.save(eventToUpdate);
                
                List<Event> output = new ArrayList<>();
                Iterable<EventDB> eventDBList = eventRepository.findAll();
                Iterator<EventDB> iterator = eventDBList.iterator();
                while (iterator.hasNext()) {
                    Event tmp = iterator.next().toEvent(universityRepository, departmentRepository, majorRepository, userRepository, thingRepository, itemRepository, eventRepository);
                    
                    if(tmp.getItem().getThing().getDepartment().getId() == deptId) {
                        output.add(tmp);
                    }
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
    public ResponseWrapper<Iterable<Event>> lostItem(@RequestHeader("user-token") String userToken, @PathVariable String univCode, @PathVariable String deptCode, @PathVariable int id) {
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
             user = userDB.toUser(universityRepository, departmentRepository, majorRepository, permissionRepository);    
        }
        
        if(!user.hasStaffPermission(deptCode)) {
            return new ResponseWrapper<>(ResponseHeader.USER_PERMISSION_DENIED_EXCEPTION, null);
        }
        
        Optional<EventDB> eventBeforeUpdateOptional = eventRepository.findById(id);
        if(eventBeforeUpdateOptional.isPresent()) {
            EventDB eventToUpdate = eventBeforeUpdateOptional.get();
            
            Event eventBeforeUpdate = eventToUpdate.toEvent(universityRepository, departmentRepository, majorRepository, userRepository, thingRepository, itemRepository, eventRepository);
            
            if(eventBeforeUpdate.getItem().getThing().getDepartment().getId() != deptId) { //TODO null pointer exception 발생 할 수도 있지 않을까?
                return new ResponseWrapper<>(ResponseHeader.NOT_FOUND_EXCEPTION, null); //TODO Exception바꿀까?
            }
            
            if(eventToUpdate.getStatus().equals("USING") || eventToUpdate.getStatus().equals("DELAYED")) {
                eventToUpdate.setLostTimeStampNow();
                eventToUpdate.setLostManagerId(userId);
                eventRepository.save(eventToUpdate);
                
                List<Event> output = new ArrayList<>();
                Iterable<EventDB> eventDBList = eventRepository.findAll();
                Iterator<EventDB> iterator = eventDBList.iterator();
                while (iterator.hasNext()) {
                    Event tmp = iterator.next().toEvent(universityRepository, departmentRepository, majorRepository, userRepository, thingRepository, itemRepository, eventRepository);
                    
                    if(tmp.getItem().getThing().getDepartment().getId() == deptId) {
                        output.add(tmp);
                    }
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
    public ResponseWrapper<Iterable<Event>> foundItem(@RequestHeader("user-token") String userToken, @PathVariable String univCode, @PathVariable String deptCode, @PathVariable int id, @RequestBody EventRequestBody requestBody) {
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
             user = userDB.toUser(universityRepository, departmentRepository, majorRepository, permissionRepository);    
        }
        
        if(!user.hasStaffPermission(deptCode)) {
            return new ResponseWrapper<>(ResponseHeader.USER_PERMISSION_DENIED_EXCEPTION, null);
        }
        
        Optional<EventDB> eventBeforeUpdateOptional = eventRepository.findById(id);
        if(eventBeforeUpdateOptional.isPresent()) {
            EventDB eventToUpdate = eventBeforeUpdateOptional.get();
            
            Event eventBeforeUpdate = eventToUpdate.toEvent(universityRepository, departmentRepository, majorRepository, userRepository, thingRepository, itemRepository, eventRepository);
            
            if(eventBeforeUpdate.getItem().getThing().getDepartment().getId() != deptId) { //TODO null pointer exception 발생 할 수도 있지 않을까?
                return new ResponseWrapper<>(ResponseHeader.NOT_FOUND_EXCEPTION, null); //TODO Exception바꿀까?
            }
            
            if(eventToUpdate.getStatus().equals("LOST")) {
                eventToUpdate.setReturnTimeStampNow();
                eventToUpdate.setReturnManagerId(userId);
                eventRepository.save(eventToUpdate);
                
                List<Event> output = new ArrayList<>();
                Iterable<EventDB> eventDBList = eventRepository.findAll();
                Iterator<EventDB> iterator = eventDBList.iterator();
                while (iterator.hasNext()) {
                    Event tmp = iterator.next().toEvent(universityRepository, departmentRepository, majorRepository, userRepository, thingRepository, itemRepository, eventRepository);
                    
                    if(tmp.getItem().getThing().getDepartment().getId() == deptId) {
                        output.add(tmp);
                    }
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

    public class PostMappingResponse {
        Event event;
        ArrayList<Thing> thingList;

        public PostMappingResponse(Event event, ArrayList<Thing> thingList) {
            this.event = event;
            this.thingList = thingList;
        }

        public Event getEvent() {
            return event;
        }

        public ArrayList<Thing> getThingList() {
            return thingList;
        }
    }
}