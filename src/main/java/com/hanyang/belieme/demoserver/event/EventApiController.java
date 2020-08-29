package com.hanyang.belieme.demoserver.event;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import com.hanyang.belieme.demoserver.thing.*;
import com.hanyang.belieme.demoserver.university.UniversityRepository;
import com.hanyang.belieme.demoserver.item.*;
import com.hanyang.belieme.demoserver.common.*;
import com.hanyang.belieme.demoserver.department.Department;
import com.hanyang.belieme.demoserver.department.DepartmentRepository;
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
    private EventRepository eventRepository;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private ThingRepository thingRepository;

    @GetMapping("/")
    public ResponseWrapper<Iterable<Event>> getItems(@PathVariable String univCode, @PathVariable String departmentCode, @RequestParam(value = "requesterId", required = false) Integer requesterId) {
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
            Event tmp = eventDB.toEvent(universityRepository, departmentRepository, thingRepository, itemRepository, eventRepository);
            if(tmp.getItem().getThing().getDepartment().getId() == departmentId) {
                if(requesterId == null || tmp.getRequesterId() == requesterId) {
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
            output = eventOptional.get().toEvent(universityRepository, departmentRepository, thingRepository, itemRepository, eventRepository);
            if(output.getItem().getThing().getDepartment().getId() == departmentId) {
                 return new ResponseWrapper<>(ResponseHeader.OK, output);   
            }
            //TODO NotFoundException의 종류를 늘려야 하나? Exception바꿀까?
        } 
        return new ResponseWrapper<>(ResponseHeader.NOT_FOUND_EXCEPTION, null);
    }

    @PostMapping("/request")
    public ResponseWrapper<PostMappingResponse> createRequestEvent(@PathVariable String univCode, @PathVariable String departmentCode, @RequestParam(value = "thingId", required = true) int thingId, @RequestParam(value = "itemNum", required = false) Integer itemNum, @RequestBody Event requestBody) {
        if(requestBody.getRequesterId() == 0 || requestBody.getRequesterName() == null) { 
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
        
        List<EventDB> eventListByRequesterId = eventRepository.findByRequesterId(requestBody.getRequesterId());
        int currentEventCount = 0;
        for(int i = 0; i < eventListByRequesterId.size(); i++) {
            Event tmp = eventListByRequesterId.get(i).toEvent(universityRepository, departmentRepository, thingRepository, itemRepository, eventRepository);
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
                requestedItem = itemListByThingId.get(i).toItem(universityRepository, departmentRepository, thingRepository, eventRepository);
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
                requestedItem = requestItemOptional.get().toItem(universityRepository, departmentRepository, thingRepository, eventRepository);
            } else {
                return new ResponseWrapper<>(ResponseHeader.NOT_FOUND_EXCEPTION, null);
            }
            
            if(!requestedItem.getStatus().equals("USABLE")) {
                return new ResponseWrapper<>(ResponseHeader.ITEM_NOT_AVAILABLE_EXCEPTION, null);
            }
        }

        EventDB newEventDB = new EventDB();
        
        newEventDB.setItemId(requestedItem.getId());
        newEventDB.setRequesterId(requestBody.getRequesterId());
        newEventDB.setRequesterName(requestBody.getRequesterName());
        newEventDB.setResponseManagerId(0);
        newEventDB.setResponseManagerName(null);
        newEventDB.setReturnManagerId(0);
        newEventDB.setReturnManagerName(null);
        newEventDB.setLostManagerId(0);
        newEventDB.setLostManagerName(null);
        newEventDB.setRequestTimeStampNow();
        newEventDB.setResponseTimeStampZero();
        newEventDB.setReturnTimeStampZero();
        newEventDB.setCancelTimeStampZero();
        newEventDB.setLostTimeStampZero();
            
        Event eventOutput = eventRepository.save(newEventDB).toEvent(universityRepository, departmentRepository, thingRepository, itemRepository, eventRepository);
            
        ItemDB updatedItemDB = new ItemDB();
        updatedItemDB.setId(requestedItem.getId());
        updatedItemDB.setNum(requestedItem.getNum());
        updatedItemDB.setThingId(requestedItem.getThing().getId());
        updatedItemDB.setLastEventId(eventOutput.getId());
        
        itemRepository.save(updatedItemDB);
            
        ArrayList<Thing> thingListOutput = new ArrayList<>();
        Iterable<ThingDB> allThingDBList = thingRepository.findAll();
        Iterator<ThingDB> iterator = allThingDBList.iterator();
        while(iterator.hasNext()) {
            Thing tmp = iterator.next().toThing(universityRepository, departmentRepository, thingRepository, itemRepository, eventRepository);
            if(tmp.getDepartment().getId() == departmentId) {
                thingListOutput.add(tmp);   
            }
        }
        return new ResponseWrapper<>(ResponseHeader.OK, new PostMappingResponse(eventOutput, thingListOutput));
    }
    
    @PostMapping("/lost")
    public ResponseWrapper<PostMappingResponse> createLostEvent(@PathVariable String univCode, @PathVariable String departmentCode, @RequestParam(value = "thingId", required = true) int thingId, @RequestParam(value = "itemNum", required = true) int itemNum, @RequestBody Event requestBody) { //output에 굳이 thing list 필요할까?
        if(requestBody.getLostManagerId() == 0 || requestBody.getLostManagerName() == null) {
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
        } else if(targetThingOptional.get().getDepartmentId() !=     departmentId) {
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
            requestedItem = requestItemOptional.get().toItem(universityRepository, departmentRepository, thingRepository, eventRepository);
        } else {
            return new ResponseWrapper<>(ResponseHeader.NOT_FOUND_EXCEPTION, null);
        }
            
        if(!requestedItem.getStatus().equals("USABLE")) {
            return new ResponseWrapper<>(ResponseHeader.ITEM_NOT_AVAILABLE_EXCEPTION, null);
        }      
        
        EventDB newEventDB = new EventDB();
        
        newEventDB.setItemId(requestedItem.getId());
        newEventDB.setRequesterId(0);
        newEventDB.setRequesterName(null);
        newEventDB.setResponseManagerId(0);
        newEventDB.setResponseManagerName(null);
        newEventDB.setReturnManagerId(0);
        newEventDB.setReturnManagerName(null);
        newEventDB.setLostManagerId(requestBody.getLostManagerId());
        newEventDB.setLostManagerName(requestBody.getLostManagerName());
        newEventDB.setRequestTimeStampZero();
        newEventDB.setResponseTimeStampZero();
        newEventDB.setReturnTimeStampZero();
        newEventDB.setCancelTimeStampZero();
        newEventDB.setLostTimeStampNow();
        
        Event eventOutput = eventRepository.save(newEventDB).toEvent(universityRepository, departmentRepository, thingRepository, itemRepository, eventRepository);
            
        ItemDB updatedItemDB = new ItemDB();
        updatedItemDB.setId(requestedItem.getId());
        updatedItemDB.setNum(requestedItem.getNum());
        updatedItemDB.setThingId(requestedItem.getThing().getId());
        updatedItemDB.setLastEventId(eventOutput.getId());;
        
        itemRepository.save(updatedItemDB);
        
        ArrayList<Thing> thingListOutput = new ArrayList<>();
        Iterable<ThingDB> allThingDBList = thingRepository.findAll();
        Iterator<ThingDB> iterator = allThingDBList.iterator();
        while(iterator.hasNext()) {
            Thing tmp = iterator.next().toThing(universityRepository, departmentRepository, thingRepository, itemRepository, eventRepository);
            if(tmp.getDepartment().getId() == departmentId) {
                thingListOutput.add(tmp);   
            }
        }
        return new ResponseWrapper<>(ResponseHeader.OK, new PostMappingResponse(eventOutput, thingListOutput));
    }

    @PutMapping("/{id}/cancel")
    public ResponseWrapper<List<Event>> cancelItem(@PathVariable String univCode, @PathVariable String departmentCode, @PathVariable int id) { // requester의 event들만 output으로 해야하는가?
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
            
            Event eventBeforeUpdate = eventToUpdate.toEvent(universityRepository, departmentRepository, thingRepository, itemRepository, eventRepository);
            if(eventBeforeUpdate.getItem().getThing().getDepartment().getId() != departmentId) {
                return new ResponseWrapper<>(ResponseHeader.NOT_FOUND_EXCEPTION, null); //TODO Exception바꿀까?
            }
            
            if(eventToUpdate.getStatus().equals("REQUESTED")) {
                eventToUpdate.setCancelTimeStampNow();
                eventRepository.save(eventToUpdate);
                List<Event> output = new ArrayList<>();
                List<EventDB> eventDBList = eventRepository.findByRequesterId(eventToUpdate.getRequesterId());
                for(int i = 0; i < eventDBList.size(); i++) {
                    Event tmp = eventDBList.get(i).toEvent(universityRepository, departmentRepository, thingRepository, itemRepository, eventRepository);
                    if(tmp.getItem().getThing().getDepartment().getId() == departmentId) {
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

    @PutMapping("/{id}/response")
    public ResponseWrapper<Iterable<Event>> responseItem(@PathVariable String univCode, @PathVariable String departmentCode, @PathVariable int id, @RequestBody Event requestBody) {
        if(requestBody.getResponseManagerId() == 0 || requestBody.getResponseManagerName() == null) {
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
        
        Optional<EventDB> eventBeforeUpdateOptional = eventRepository.findById(id);
        if(eventBeforeUpdateOptional.isPresent()) {
            EventDB eventToUpdate = eventBeforeUpdateOptional.get();
            
            Event eventBeforeUpdate = eventToUpdate.toEvent(universityRepository, departmentRepository, thingRepository, itemRepository, eventRepository);
            if(eventBeforeUpdate.getItem().getThing().getDepartment().getId() != departmentId) {
                return new ResponseWrapper<>(ResponseHeader.NOT_FOUND_EXCEPTION, null); //TODO Exception바꿀까?
            }
            
            if(eventToUpdate.getStatus().equals("REQUESTED")) {
                eventToUpdate.setResponseTimeStampNow();
                eventToUpdate.setResponseManagerId(requestBody.getResponseManagerId());
                eventToUpdate.setResponseManagerName(requestBody.getResponseManagerName());
                eventRepository.save(eventToUpdate);
                
                List<Event> output = new ArrayList<>();
                Iterable<EventDB> eventDBList = eventRepository.findAll();
                Iterator<EventDB> iterator = eventDBList.iterator();
                while (iterator.hasNext()) {
                    Event tmp = iterator.next().toEvent(universityRepository, departmentRepository, thingRepository, itemRepository, eventRepository);
                    if(tmp.getItem().getThing().getDepartment().getId() == departmentId) {
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

    @PutMapping("/{id}/return")
    public ResponseWrapper<Iterable<Event>> returnItem(@PathVariable String univCode, @PathVariable String departmentCode, @PathVariable int id, @RequestBody Event requestBody) {
        if(requestBody.getReturnManagerId() == 0 || requestBody.getReturnManagerName() == null) {
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
        
        Optional<EventDB> eventBeforeUpdateOptional = eventRepository.findById(id);
        if(eventBeforeUpdateOptional.isPresent()) {
            EventDB eventToUpdate = eventBeforeUpdateOptional.get();
            
            Event eventBeforeUpdate = eventToUpdate.toEvent(universityRepository, departmentRepository, thingRepository, itemRepository, eventRepository);
            if(eventBeforeUpdate.getItem().getThing().getDepartment().getId() != departmentId) {
                return new ResponseWrapper<>(ResponseHeader.NOT_FOUND_EXCEPTION, null); //TODO Exception바꿀까?
            }
            
            if(eventToUpdate.getStatus().equals("USING") || eventToUpdate.getStatus().equals("DELAYED")) {
                eventToUpdate.setReturnTimeStampNow();
                eventToUpdate.setReturnManagerId(requestBody.getReturnManagerId());
                eventToUpdate.setReturnManagerName(requestBody.getReturnManagerName());
                eventRepository.save(eventToUpdate);
                
                List<Event> output = new ArrayList<>();
                Iterable<EventDB> eventDBList = eventRepository.findAll();
                Iterator<EventDB> iterator = eventDBList.iterator();
                while (iterator.hasNext()) {
                    Event tmp = iterator.next().toEvent(universityRepository, departmentRepository, thingRepository, itemRepository, eventRepository);
                    if(tmp.getItem().getThing().getDepartment().getId() == departmentId) {
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
    
    @PutMapping("{id}/lost")
    public ResponseWrapper<Iterable<Event>> lostItem(@PathVariable String univCode, @PathVariable String departmentCode, @PathVariable int id, @RequestBody Event requestBody) {
        if(requestBody.getLostManagerId() == 0 || requestBody.getLostManagerName() == null) {
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
        
        Optional<EventDB> eventBeforeUpdateOptional = eventRepository.findById(id);
        if(eventBeforeUpdateOptional.isPresent()) {
            EventDB eventToUpdate = eventBeforeUpdateOptional.get();
            
            Event eventBeforeUpdate = eventToUpdate.toEvent(universityRepository, departmentRepository, thingRepository, itemRepository, eventRepository);
            if(eventBeforeUpdate.getItem().getThing().getDepartment().getId() != departmentId) {
                return new ResponseWrapper<>(ResponseHeader.NOT_FOUND_EXCEPTION, null); //TODO Exception바꿀까?
            }
            
            if(eventToUpdate.getStatus().equals("USING") || eventToUpdate.getStatus().equals("DELAYED")) {
                eventToUpdate.setLostTimeStampNow();
                eventToUpdate.setLostManagerId(requestBody.getLostManagerId());
                eventToUpdate.setLostManagerName(requestBody.getLostManagerName());
                eventRepository.save(eventToUpdate);
                
                List<Event> output = new ArrayList<>();
                Iterable<EventDB> eventDBList = eventRepository.findAll();
                Iterator<EventDB> iterator = eventDBList.iterator();
                while (iterator.hasNext()) {
                    Event tmp = iterator.next().toEvent(universityRepository, departmentRepository, thingRepository, itemRepository, eventRepository);
                    if(tmp.getItem().getThing().getDepartment().getId() == departmentId) {
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
    
    @PutMapping("/{id}/found")
    public ResponseWrapper<Iterable<Event>> foundItem(@PathVariable String univCode, @PathVariable String departmentCode, @PathVariable int id, @RequestBody Event requestBody) {
        if(requestBody.getReturnManagerId() == 0 || requestBody.getReturnManagerName() == null) {
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
        
        Optional<EventDB> eventBeforeUpdateOptional = eventRepository.findById(id);
        if(eventBeforeUpdateOptional.isPresent()) {
            EventDB eventToUpdate = eventBeforeUpdateOptional.get();
            
            Event eventBeforeUpdate = eventToUpdate.toEvent(universityRepository, departmentRepository, thingRepository, itemRepository, eventRepository);
            if(eventBeforeUpdate.getItem().getThing().getDepartment().getId() != departmentId) {
                return new ResponseWrapper<>(ResponseHeader.NOT_FOUND_EXCEPTION, null); //TODO Exception바꿀까?
            }
            
            if(eventToUpdate.getStatus().equals("LOST")) {
                eventToUpdate.setReturnTimeStampNow();
                eventToUpdate.setReturnManagerId(requestBody.getReturnManagerId());
                eventToUpdate.setReturnManagerName(requestBody.getReturnManagerName());
                eventRepository.save(eventToUpdate);
                
                List<Event> output = new ArrayList<>();
                Iterable<EventDB> eventDBList = eventRepository.findAll();
                Iterator<EventDB> iterator = eventDBList.iterator();
                while (iterator.hasNext()) {
                    Event tmp = iterator.next().toEvent(universityRepository, departmentRepository, thingRepository, itemRepository, eventRepository);
                    if(tmp.getItem().getThing().getDepartment().getId() == departmentId) {
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