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
import com.hanyang.belieme.demoserver.user.UserRepository;
import com.hanyang.belieme.demoserver.item.*;
import com.hanyang.belieme.demoserver.common.*;
import com.hanyang.belieme.demoserver.department.Department;
import com.hanyang.belieme.demoserver.department.DepartmentRepository;
import com.hanyang.belieme.demoserver.department.major.MajorRepository;
import com.hanyang.belieme.demoserver.exception.NotFoundException;
import com.hanyang.belieme.demoserver.exception.WrongInDataBaseException;


@RestController
@RequestMapping(path="/universities/{univCode}/departments/{departmentCode}/events")
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
    public ResponseWrapper<List<Event>> getItems(@PathVariable String univCode, @PathVariable String departmentCode, @RequestParam(value = "requesterStudentId", required = false) String studentId) {
        int departmentId;
        try {
            departmentId = Department.findIdByUniversityCodeAndDepartmentCode(universityRepository, departmentRepository, univCode, departmentCode);
        } catch(NotFoundException e) {
            return new ResponseWrapper<>(ResponseHeader.NOT_FOUND_EXCEPTION, null);
        } catch(WrongInDataBaseException e) {
            return new ResponseWrapper<>(ResponseHeader.WRONG_IN_DATABASE_EXCEPTION, null);
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
            
            if(tmp.getItem().getThing().getDepartment().getId() == departmentId) {
                if(studentId == null || studentId.equals(tmp.getRequester().getStudentId())) {
                    output.add(tmp);    
                }
            }
        }
        return new ResponseWrapper<>(ResponseHeader.OK, output);
    }

    @GetMapping("/{id}")
    public ResponseWrapper<Event> getItem(@PathVariable String univCode, @PathVariable String departmentCode, @PathVariable int id) {
        int departmentId;
        try {
            departmentId = Department.findIdByUniversityCodeAndDepartmentCode(universityRepository, departmentRepository, univCode, departmentCode);
        } catch(NotFoundException e) {
            return new ResponseWrapper<>(ResponseHeader.NOT_FOUND_EXCEPTION, null);
        } catch(WrongInDataBaseException e) {
            return new ResponseWrapper<>(ResponseHeader.WRONG_IN_DATABASE_EXCEPTION, null);
        }
        
        Optional<EventDB> eventOptional = eventRepository.findById(id);
        Event output;
        if(eventOptional.isPresent()) {
            try {
                output = eventOptional.get().toEvent(universityRepository, departmentRepository, majorRepository, userRepository, thingRepository, itemRepository, eventRepository);
            } catch(NotFoundException e) {
                return new ResponseWrapper<>(ResponseHeader.NOT_FOUND_EXCEPTION, null);
            }
            
            if(output.getItem().getThing().getDepartment().getId() == departmentId) {
                 return new ResponseWrapper<>(ResponseHeader.OK, output);   
            }
            //TODO NotFoundException의 종류를 늘려야 하나? Exception바꿀까?
        } 
        return new ResponseWrapper<>(ResponseHeader.NOT_FOUND_EXCEPTION, null);
    }

    @PostMapping("/request")
    public ResponseWrapper<Event> createRequestEvent(@PathVariable String univCode, @PathVariable String departmentCode, @RequestParam(value = "thingId", required = true) int thingId, @RequestParam(value = "itemNum", required = false) Integer itemNum, @RequestBody EventRequestBody requestBody) {
        if(requestBody.getRequesterStudentId() == null) { 
            return new ResponseWrapper<>(ResponseHeader.LACK_OF_REQUEST_BODY_EXCEPTION, null);
        }
        
        int departmentId;
        try {
            departmentId = Department.findIdByUniversityCodeAndDepartmentCode(universityRepository, departmentRepository, univCode, departmentCode);
        } catch(NotFoundException e) {
            return new ResponseWrapper<>(ResponseHeader.NOT_FOUND_EXCEPTION, null);
        } catch(WrongInDataBaseException e) {
            return new ResponseWrapper<>(ResponseHeader.WRONG_IN_DATABASE_EXCEPTION, null);
        }

        Optional<ThingDB> targetThingOptional = thingRepository.findById(thingId);
        if(!targetThingOptional.isPresent()) {
            return new ResponseWrapper<>(ResponseHeader.NOT_FOUND_EXCEPTION, null);
        } else if(targetThingOptional.get().getDepartmentId() != departmentId) {
            return new ResponseWrapper<>(ResponseHeader.NOT_FOUND_EXCEPTION, null); //TODO Exception바꿀까?
        }
        
        int requesterId;
        try {
            requesterId = User.findIdByUniversityCodeAndStudentId(universityRepository, userRepository, univCode, requestBody.getRequesterStudentId());
        } catch(NotFoundException e) {
            return new ResponseWrapper<>(ResponseHeader.NOT_FOUND_EXCEPTION, null);
        } catch(WrongInDataBaseException e) {
            return new ResponseWrapper<>(ResponseHeader.WRONG_IN_DATABASE_EXCEPTION, null);
        }
        
        List<EventDB> eventListByRequesterId = eventRepository.findByRequesterId(requesterId);
        int currentEventCount = 0;
        for(int i = 0; i < eventListByRequesterId.size(); i++) {
            Event tmp;
            
            try {
                tmp = eventListByRequesterId.get(i).toEvent(universityRepository, departmentRepository, majorRepository, userRepository, thingRepository, itemRepository, eventRepository);   
            } catch(NotFoundException e) {
                return new ResponseWrapper<>(ResponseHeader.NOT_FOUND_EXCEPTION, null);
            }
            
            if(tmp.getStatus().equals("REQUESTED") || tmp.getStatus().equals("USING") || tmp.getStatus().equals("DELAYED") || tmp.getStatus().equals("LOST")) {
                currentEventCount++;
                if(tmp.getItem().getThing().getId() == thingId) {
                    return new ResponseWrapper<>(ResponseHeader.EVENT_FOR_SAME_THING_EXCEPTION, null);
                }
            }
        }
        if(currentEventCount >= 3) {
            return new ResponseWrapper<>(ResponseHeader.OVER_THREE_CURRENT_EVENT_EXCEPTION, null);
        }
        
        Item requestedItem = null;
        if(itemNum == null) {
            List<ItemDB> itemListByThingId = itemRepository.findByThingId(thingId);
            for(int i = 0; i < itemListByThingId.size(); i++) {
                try {
                    requestedItem = itemListByThingId.get(i).toItem(universityRepository, departmentRepository, majorRepository, userRepository, thingRepository, eventRepository);
                } catch(NotFoundException e) {
                    return new ResponseWrapper<>(ResponseHeader.NOT_FOUND_EXCEPTION, null);
                }
                if (requestedItem.getStatus().equals("USABLE")) {
                    break;
                }
                requestedItem = null;
            }
            if(requestedItem == null) {
                return new ResponseWrapper<>(ResponseHeader.ITEM_NOT_AVAILABLE_EXCEPTION, null);
            }
        } else {
            int itemId;
            try {
                itemId = Item.findItemIdByThingIdAndItemNum(itemRepository, thingId, itemNum);
            } catch(NotFoundException e) {
                return new ResponseWrapper<>(ResponseHeader.NOT_FOUND_EXCEPTION, null);
            } catch(WrongInDataBaseException e) {
                return new ResponseWrapper<>(ResponseHeader.WRONG_IN_DATABASE_EXCEPTION, null);
            }
            
            Optional<ItemDB> requestItemOptional = itemRepository.findById(itemId);
            if(requestItemOptional.isPresent()) {
                try {
                    requestedItem = requestItemOptional.get().toItem(universityRepository, departmentRepository, majorRepository, userRepository, thingRepository, eventRepository);
                } catch(NotFoundException e) {
                    return new ResponseWrapper<>(ResponseHeader.NOT_FOUND_EXCEPTION, null);
                }
            } else {
                return new ResponseWrapper<>(ResponseHeader.NOT_FOUND_EXCEPTION, null);
            }
            
            if(!requestedItem.getStatus().equals("USABLE")) {
                return new ResponseWrapper<>(ResponseHeader.ITEM_NOT_AVAILABLE_EXCEPTION, null);
            }
        }

        EventDB newEventDB = new EventDB();
        
        newEventDB.setItemId(requestedItem.getId());
        newEventDB.setRequesterId(requesterId);
        newEventDB.setResponseManagerId(0);
        newEventDB.setReturnManagerId(0);
        newEventDB.setLostManagerId(0);
        newEventDB.setRequestTimeStampNow();
        newEventDB.setResponseTimeStampZero();
        newEventDB.setReturnTimeStampZero();
        newEventDB.setCancelTimeStampZero();
        newEventDB.setLostTimeStampZero();
            
        Event output;
        try {
            output = newEventDB.toEvent(universityRepository, departmentRepository, majorRepository, userRepository, thingRepository, itemRepository, eventRepository);   
        } catch(NotFoundException e) {
            return new ResponseWrapper<>(ResponseHeader.NOT_FOUND_EXCEPTION, null);
        }
        
        ItemDB updatedItemDB = new ItemDB();
        updatedItemDB.setId(requestedItem.getId());
        updatedItemDB.setNum(requestedItem.getNum());
        updatedItemDB.setThingId(requestedItem.getThing().getId());
        updatedItemDB.setLastEventId(output.getId());;
        
        itemRepository.save(updatedItemDB);
        
        return new ResponseWrapper<>(ResponseHeader.OK, output);
    }
    
    @PostMapping("/lost")
    public ResponseWrapper<Event> createLostEvent(@PathVariable String univCode, @PathVariable String departmentCode, @RequestParam(value = "thingId", required = true) int thingId, @RequestParam(value = "itemNum", required = true) int itemNum, @RequestBody EventRequestBody requestBody) {
        if(requestBody.getLostManagerStudentId() == null) {
            return new ResponseWrapper<>(ResponseHeader.LACK_OF_REQUEST_BODY_EXCEPTION, null);
        }
        
        int departmentId;
        try {
            departmentId = Department.findIdByUniversityCodeAndDepartmentCode(universityRepository, departmentRepository, univCode, departmentCode);
        } catch(NotFoundException e) {
            return new ResponseWrapper<>(ResponseHeader.NOT_FOUND_EXCEPTION, null);
        } catch(WrongInDataBaseException e) {
            return new ResponseWrapper<>(ResponseHeader.WRONG_IN_DATABASE_EXCEPTION, null);
        }

        Optional<ThingDB> targetThingOptional = thingRepository.findById(thingId);
        if(!targetThingOptional.isPresent()) {
            return new ResponseWrapper<>(ResponseHeader.NOT_FOUND_EXCEPTION, null);
        } else if(targetThingOptional.get().getDepartmentId() != departmentId) {
            return new ResponseWrapper<>(ResponseHeader.NOT_FOUND_EXCEPTION, null); //TODO Exception바꿀까?
        }
        
        Item requestedItem;
        int itemId;
        try {
            itemId = Item.findItemIdByThingIdAndItemNum(itemRepository, thingId, itemNum);
        } catch(NotFoundException e) {
            return new ResponseWrapper<>(ResponseHeader.NOT_FOUND_EXCEPTION, null);
        } catch(WrongInDataBaseException e) {
            return new ResponseWrapper<>(ResponseHeader.WRONG_IN_DATABASE_EXCEPTION, null);
        }
            
        Optional<ItemDB> requestItemOptional = itemRepository.findById(itemId);
        if(requestItemOptional.isPresent()) {
            try {
                requestedItem = requestItemOptional.get().toItem(universityRepository, departmentRepository, majorRepository, userRepository, thingRepository, eventRepository);
            } catch(NotFoundException e) {
                return new ResponseWrapper<>(ResponseHeader.NOT_FOUND_EXCEPTION, null);
            }
        } else {
            return new ResponseWrapper<>(ResponseHeader.NOT_FOUND_EXCEPTION, null);
        }   
        if(!requestedItem.getStatus().equals("USABLE")) {
            return new ResponseWrapper<>(ResponseHeader.ITEM_NOT_AVAILABLE_EXCEPTION, null);
        }
        
        int lostManagerId;
        try {
            lostManagerId = User.findIdByUniversityCodeAndStudentId(universityRepository, userRepository, univCode, requestBody.getLostManagerStudentId());
        } catch(NotFoundException e) {
            return new ResponseWrapper<>(ResponseHeader.NOT_FOUND_EXCEPTION, null);
        } catch(WrongInDataBaseException e) {
            return new ResponseWrapper<>(ResponseHeader.WRONG_IN_DATABASE_EXCEPTION, null);
        }
        
        EventDB newEventDB = new EventDB();
        
        newEventDB.setItemId(requestedItem.getId());
        newEventDB.setRequesterId(0);
        newEventDB.setResponseManagerId(0);
        newEventDB.setReturnManagerId(0);
        newEventDB.setLostManagerId(lostManagerId);
        newEventDB.setRequestTimeStampZero();
        newEventDB.setResponseTimeStampZero();
        newEventDB.setReturnTimeStampZero();
        newEventDB.setCancelTimeStampZero();
        newEventDB.setLostTimeStampNow();
        
        Event output;
        try {
            output = newEventDB.toEvent(universityRepository, departmentRepository, majorRepository, userRepository, thingRepository, itemRepository, eventRepository);   
        } catch(NotFoundException e) {
            return new ResponseWrapper<>(ResponseHeader.NOT_FOUND_EXCEPTION, null);
        }
            
        ItemDB updatedItemDB = new ItemDB();
        updatedItemDB.setId(requestedItem.getId());
        updatedItemDB.setNum(requestedItem.getNum());
        updatedItemDB.setThingId(requestedItem.getThing().getId());
        updatedItemDB.setLastEventId(output.getId());;
        
        itemRepository.save(updatedItemDB);
        
        return new ResponseWrapper<>(ResponseHeader.OK, output);
    }

    @PatchMapping("/{id}/cancel")
    public ResponseWrapper<Event> cancelItem(@PathVariable String univCode, @PathVariable String departmentCode, @PathVariable int id) {
        int departmentId;
        try {
            departmentId = Department.findIdByUniversityCodeAndDepartmentCode(universityRepository, departmentRepository, univCode, departmentCode);
        } catch(NotFoundException e) {
            return new ResponseWrapper<>(ResponseHeader.NOT_FOUND_EXCEPTION, null);
        } catch(WrongInDataBaseException e) {
            return new ResponseWrapper<>(ResponseHeader.WRONG_IN_DATABASE_EXCEPTION, null);
        }
        
        Optional<EventDB> eventBeforeUpdateOptional = eventRepository.findById(id);
        if(eventBeforeUpdateOptional.isPresent()) {
            EventDB eventToUpdate = eventBeforeUpdateOptional.get();
            
            Event eventBeforeUpdate;
            try {
                eventBeforeUpdate = eventToUpdate.toEvent(universityRepository, departmentRepository, majorRepository, userRepository, thingRepository, itemRepository, eventRepository);
            } catch(NotFoundException e) {
                return new ResponseWrapper<>(ResponseHeader.NOT_FOUND_EXCEPTION, null);
            }
            if(eventBeforeUpdate.getItem().getThing().getDepartment().getId() != departmentId) {
                return new ResponseWrapper<>(ResponseHeader.NOT_FOUND_EXCEPTION, null); //TODO Exception바꿀까?
            }
            
            if(eventToUpdate.getStatus().equals("REQUESTED")) {
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

    @PatchMapping("/{id}/response")
    public ResponseWrapper<Event> responseItem(@PathVariable String univCode, @PathVariable String departmentCode, @PathVariable int id, @RequestBody EventRequestBody requestBody) {
        if(requestBody.getResponseManagerStudentId() == null) {
            return new ResponseWrapper<>(ResponseHeader.LACK_OF_REQUEST_BODY_EXCEPTION, null);
        }
        
        int departmentId;
        try {
            departmentId = Department.findIdByUniversityCodeAndDepartmentCode(universityRepository, departmentRepository, univCode, departmentCode);
        } catch(NotFoundException e) {
            return new ResponseWrapper<>(ResponseHeader.NOT_FOUND_EXCEPTION, null);
        } catch(WrongInDataBaseException e) {
            return new ResponseWrapper<>(ResponseHeader.WRONG_IN_DATABASE_EXCEPTION, null);
        }
        
        int responseManagerId;
        try {
            responseManagerId = User.findIdByUniversityCodeAndStudentId(universityRepository, userRepository, univCode, requestBody.getResponseManagerStudentId());
        } catch(NotFoundException e) {
            return new ResponseWrapper<>(ResponseHeader.NOT_FOUND_EXCEPTION, null);
        } catch(WrongInDataBaseException e) {
            return new ResponseWrapper<>(ResponseHeader.WRONG_IN_DATABASE_EXCEPTION, null);
        }
        
        Optional<EventDB> eventBeforeUpdateOptional = eventRepository.findById(id);
        if(eventBeforeUpdateOptional.isPresent()) {
            EventDB eventToUpdate = eventBeforeUpdateOptional.get();
            
            Event eventBeforeUpdate;
            try {
                eventBeforeUpdate = eventToUpdate.toEvent(universityRepository, departmentRepository, majorRepository, userRepository, thingRepository, itemRepository, eventRepository);
            } catch(NotFoundException e) {
                return new ResponseWrapper<>(ResponseHeader.NOT_FOUND_EXCEPTION, null);
            }
            if(eventBeforeUpdate.getItem().getThing().getDepartment().getId() != departmentId) {
                return new ResponseWrapper<>(ResponseHeader.NOT_FOUND_EXCEPTION, null); //TODO Exception바꿀까?
            }
            
            if(eventToUpdate.getStatus().equals("REQUESTED")) {
                eventToUpdate.setResponseTimeStampNow();
                eventToUpdate.setResponseManagerId(responseManagerId);
                
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
    public ResponseWrapper<Event> returnItem(@PathVariable String univCode, @PathVariable String departmentCode, @PathVariable int id, @RequestBody EventRequestBody requestBody) {
        if(requestBody.getReturnManagerStudentId() == null) {
            return new ResponseWrapper<>(ResponseHeader.LACK_OF_REQUEST_BODY_EXCEPTION, null);
        }
        int departmentId;
        try {
            departmentId = Department.findIdByUniversityCodeAndDepartmentCode(universityRepository, departmentRepository, univCode, departmentCode);
        } catch(NotFoundException e) {
            return new ResponseWrapper<>(ResponseHeader.NOT_FOUND_EXCEPTION, null);
        } catch(WrongInDataBaseException e) {
            return new ResponseWrapper<>(ResponseHeader.WRONG_IN_DATABASE_EXCEPTION, null);
        }
        
        int returnManagerId;
        try {
            returnManagerId = User.findIdByUniversityCodeAndStudentId(universityRepository, userRepository, univCode, requestBody.getReturnManagerStudentId());
        } catch(NotFoundException e) {
            return new ResponseWrapper<>(ResponseHeader.NOT_FOUND_EXCEPTION, null);
        } catch(WrongInDataBaseException e) {
            return new ResponseWrapper<>(ResponseHeader.WRONG_IN_DATABASE_EXCEPTION, null);
        }
        
        Optional<EventDB> eventBeforeUpdateOptional = eventRepository.findById(id);
        if(eventBeforeUpdateOptional.isPresent()) {
            EventDB eventToUpdate = eventBeforeUpdateOptional.get();
            
            Event eventBeforeUpdate;
            try {
                eventBeforeUpdate = eventToUpdate.toEvent(universityRepository, departmentRepository, majorRepository, userRepository, thingRepository, itemRepository, eventRepository);
            } catch(NotFoundException e) {
                return new ResponseWrapper<>(ResponseHeader.NOT_FOUND_EXCEPTION, null);
            }
            if(eventBeforeUpdate.getItem().getThing().getDepartment().getId() != departmentId) {
                return new ResponseWrapper<>(ResponseHeader.NOT_FOUND_EXCEPTION, null); //TODO Exception바꿀까?
            }
            
            if(eventToUpdate.getStatus().equals("USING") || eventToUpdate.getStatus().equals("DELAYED")) {
                eventToUpdate.setReturnTimeStampNow();
                eventToUpdate.setReturnManagerId(returnManagerId);
                
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
    public ResponseWrapper<Event> lostItem(@PathVariable String univCode, @PathVariable String departmentCode, @PathVariable int id, @RequestBody EventRequestBody requestBody) {
        if(requestBody.getLostManagerStudentId() == null) {
            return new ResponseWrapper<>(ResponseHeader.LACK_OF_REQUEST_BODY_EXCEPTION, null);
        }
        
        int departmentId;
        try {
            departmentId = Department.findIdByUniversityCodeAndDepartmentCode(universityRepository, departmentRepository, univCode, departmentCode);
        } catch(NotFoundException e) {
            return new ResponseWrapper<>(ResponseHeader.NOT_FOUND_EXCEPTION, null);
        } catch(WrongInDataBaseException e) {
            return new ResponseWrapper<>(ResponseHeader.WRONG_IN_DATABASE_EXCEPTION, null);
        }
        
        int lostManagerId;
        try {
            lostManagerId = User.findIdByUniversityCodeAndStudentId(universityRepository, userRepository, univCode, requestBody.getLostManagerStudentId());
        } catch(NotFoundException e) {
            return new ResponseWrapper<>(ResponseHeader.NOT_FOUND_EXCEPTION, null);
        } catch(WrongInDataBaseException e) {
            return new ResponseWrapper<>(ResponseHeader.WRONG_IN_DATABASE_EXCEPTION, null);
        }
        
        Optional<EventDB> eventBeforeUpdateOptional = eventRepository.findById(id);
        if(eventBeforeUpdateOptional.isPresent()) {
            EventDB eventToUpdate = eventBeforeUpdateOptional.get();
            
            Event eventBeforeUpdate;
            try {
                eventBeforeUpdate = eventToUpdate.toEvent(universityRepository, departmentRepository, majorRepository, userRepository, thingRepository, itemRepository, eventRepository);
            } catch(NotFoundException e) {
                return new ResponseWrapper<>(ResponseHeader.NOT_FOUND_EXCEPTION, null);
            }
            if(eventBeforeUpdate.getItem().getThing().getDepartment().getId() != departmentId) {
                return new ResponseWrapper<>(ResponseHeader.NOT_FOUND_EXCEPTION, null); //TODO Exception바꿀까?
            }
            
            if(eventToUpdate.getStatus().equals("USING") || eventToUpdate.getStatus().equals("DELAYED")) {
                eventToUpdate.setLostTimeStampNow();
                eventToUpdate.setLostManagerId(lostManagerId);
                
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
    public ResponseWrapper<Event> foundItem(@PathVariable String univCode, @PathVariable String departmentCode, @PathVariable int id, @RequestBody EventRequestBody requestBody) {
        if(requestBody.getReturnManagerStudentId() == null) {
            return new ResponseWrapper<>(ResponseHeader.LACK_OF_REQUEST_BODY_EXCEPTION, null);
        }
        
        int departmentId;
        try {
            departmentId = Department.findIdByUniversityCodeAndDepartmentCode(universityRepository, departmentRepository, univCode, departmentCode);
        } catch(NotFoundException e) {
            return new ResponseWrapper<>(ResponseHeader.NOT_FOUND_EXCEPTION, null);
        } catch(WrongInDataBaseException e) {
            return new ResponseWrapper<>(ResponseHeader.WRONG_IN_DATABASE_EXCEPTION, null);
        }
        
        int returnManagerId;
        try {
            returnManagerId = User.findIdByUniversityCodeAndStudentId(universityRepository, userRepository, univCode, requestBody.getReturnManagerStudentId());
        } catch(NotFoundException e) {
            return new ResponseWrapper<>(ResponseHeader.NOT_FOUND_EXCEPTION, null);
        } catch(WrongInDataBaseException e) {
            return new ResponseWrapper<>(ResponseHeader.WRONG_IN_DATABASE_EXCEPTION, null);
        }
        
        Optional<EventDB> eventBeforeUpdateOptional = eventRepository.findById(id);
        if(eventBeforeUpdateOptional.isPresent()) {
            EventDB eventToUpdate = eventBeforeUpdateOptional.get();
            
            Event eventBeforeUpdate;
            try {
                eventBeforeUpdate = eventToUpdate.toEvent(universityRepository, departmentRepository, majorRepository, userRepository, thingRepository, itemRepository, eventRepository);
            } catch(NotFoundException e) {
                return new ResponseWrapper<>(ResponseHeader.NOT_FOUND_EXCEPTION, null);
            }
            if(eventBeforeUpdate.getItem().getThing().getDepartment().getId() != departmentId) {
                return new ResponseWrapper<>(ResponseHeader.NOT_FOUND_EXCEPTION, null); //TODO Exception바꿀까?
            }
            
            if(eventToUpdate.getStatus().equals("LOST")) {
                eventToUpdate.setReturnTimeStampNow();
                eventToUpdate.setReturnManagerId(returnManagerId);
                
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
    
    private class EventRequestBody {
        private String requesterStudentId;
        private String responseManagerStudentId;
        private String returnManagerStudentId;
        private String lostManagerStudentId;
        
        public String getRequesterStudentId() {
            return requesterStudentId;
        }
        
        public String getResponseManagerStudentId() {
            return responseManagerStudentId;
        }
        
        public String getReturnManagerStudentId() {
            return returnManagerStudentId; 
        }
        
        public String getLostManagerStudentId() {
            return lostManagerStudentId;
        }
    }
}