package com.hanyang.belieme.demoserver.event;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import com.hanyang.belieme.demoserver.thing.*;
import com.hanyang.belieme.demoserver.item.*;
import com.hanyang.belieme.demoserver.common.*;


@RestController
@RequestMapping(path="/events")
public class EventApiController {
    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private ThingRepository thingRepository;

    @GetMapping("/all")
    public ResponseWrapper<Iterable<Event>> getItems() {
        Iterable<Event> allEventList = eventRepository.findAll();
        Iterator<Event> iterator = allEventList.iterator();
        while(iterator.hasNext()) {
            Event event = iterator.next();
            event.addInfo(thingRepository, itemRepository, eventRepository);
        }
        return new ResponseWrapper<>(ResponseHeader.OK, allEventList);
    }

    @GetMapping("/{id}")
    public ResponseWrapper<Event> getItem(@PathVariable int id) {
        Optional<Event> eventOptional = eventRepository.findById(id);
        if(eventOptional.isPresent()) {
            eventOptional.get().addInfo(thingRepository, itemRepository, eventRepository);
            return new ResponseWrapper<>(ResponseHeader.OK, eventOptional.get());
        }
        return new ResponseWrapper<>(ResponseHeader.NOT_FOUND_EXCEPTION, null);
    }
    
    @GetMapping("")
    public ResponseWrapper<List<Event>> getItemsByRequesterId(@RequestParam("requesterId") int requesterId) {
        List<Event> eventListByRequesterId = eventRepository.findByRequesterId(requesterId);
        for(int i = 0; i < eventListByRequesterId.size(); i++) {
            eventListByRequesterId.get(i).addInfo(thingRepository, itemRepository, eventRepository);
        }
        return new ResponseWrapper<>(ResponseHeader.OK, eventListByRequesterId);
    }

    @PostMapping("/request/{thingId}/{itemNum}")
    public ResponseWrapper<PostMappingResponse> createRequestEvent(@PathVariable int thingId, @PathVariable int itemNum, @RequestBody Event requestBody) {
        if(requestBody.getRequesterId() == 0 || requestBody.getRequesterName() == null) { 
            return new ResponseWrapper<>(ResponseHeader.LACK_OF_REQUEST_BODY_EXCEPTION, null);
        }
        
        List<Event> eventListByRequesterId = eventRepository.findByRequesterId(requestBody.getRequesterId());
        int currentEventCount = 0;
        for(int i = 0; i < eventListByRequesterId.size(); i++) {
            eventListByRequesterId.get(i).addInfo(thingRepository, itemRepository, eventRepository);
            Event tmp = eventListByRequesterId.get(i);
            if(tmp.getStatus().equals("REQUESTED") || tmp.getStatus().equals("USING") || tmp.getStatus().equals("DELAYED") || tmp.getStatus().equals("LOST")) {
                currentEventCount++;
            }
        }
        if(currentEventCount >= 3) {
            return new ResponseWrapper<>(ResponseHeader.OVER_THREE_CURRENT_EVENT_EXCEPTION, null);
        }

        List<Item> itemListByThingIdAndNum = itemRepository.findByThingIdAndNum(thingId, itemNum);
        Item requestedItem;
        if(itemListByThingIdAndNum.size() == 0) {
            return new ResponseWrapper<>(ResponseHeader.NOT_FOUND_EXCEPTION, null);
        } else if(itemListByThingIdAndNum.size() != 1) { //Warning 으로 바꿀까?? 그건 좀 귀찮긴 할 듯
            return new ResponseWrapper<>(ResponseHeader.WRONG_IN_DATABASE_EXCEPTION, null);
        }
        
        Optional<Item> requestItemOptional = itemRepository.findById(itemListByThingIdAndNum.get(0).getId());
        if(requestItemOptional.isPresent()) {
            requestedItem = requestItemOptional.get();
            requestedItem.addInfo(thingRepository, eventRepository);
        }
        else {
            return new ResponseWrapper<>(ResponseHeader.NOT_FOUND_EXCEPTION, null);
        }         
        
        if(!requestedItem.getStatus().equals("USABLE")) {
            return new ResponseWrapper<>(ResponseHeader.ITEM_NOT_AVAILABLE_EXCEPTION, null);
        }
        
        requestBody.setItemId(requestedItem.getId());
        requestBody.setResponseManagerId(0);
        requestBody.setResponseManagerName(null);
        requestBody.setReturnManagerId(0);
        requestBody.setReturnManagerName(null);
        requestBody.setLostManagerId(0);
        requestBody.setLostManagerName(null);
        requestBody.setRequestTimeStampNow();
        requestBody.setResponseTimeStampZero();
        requestBody.setReturnTimeStampZero();
        requestBody.setCancelTimeStampZero();
        requestBody.setLostTimeStampZero();
            
        Event eventOutput = eventRepository.save(requestBody);
        eventOutput.addInfo(thingRepository, itemRepository, eventRepository);
            
        requestedItem.setLastEventId(eventOutput.getId());
        itemRepository.save(requestedItem);
            
        ArrayList<Thing> thingListOutput = new ArrayList<>();
        Iterable<ThingDB> allThingDBList = thingRepository.findAll();
        Iterator<ThingDB> iterator = allThingDBList.iterator();
        while(iterator.hasNext()) {
            Thing tmp = iterator.next().toThing();
            tmp.addInfo(thingRepository, itemRepository, eventRepository);
            thingListOutput.add(tmp);
        }
        return new ResponseWrapper<>(ResponseHeader.OK, new PostMappingResponse(eventOutput, thingListOutput));
    }
    
    @PostMapping("/request/{thingId}")
    public ResponseWrapper<PostMappingResponse> createRequestEvent(@PathVariable int thingId, @RequestBody Event requestBody) { //output에 굳이 thing list 필요할까?
        if(requestBody.getRequesterId() == 0 || requestBody.getRequesterName() == null) { 
            return new ResponseWrapper<>(ResponseHeader.LACK_OF_REQUEST_BODY_EXCEPTION, null);
        }
        List<Event> eventListByRequesterId = eventRepository.findByRequesterId(requestBody.getRequesterId());
        int currentEventCount = 0;
        for(int i = 0; i < eventListByRequesterId.size(); i++) {
            eventListByRequesterId.get(i).addInfo(thingRepository, itemRepository, eventRepository);
            Event tmp = eventListByRequesterId.get(i);
            if(tmp.getStatus().equals("REQUESTED") || tmp.getStatus().equals("USING") || tmp.getStatus().equals("DELAYED") || tmp.getStatus().equals("LOST")) {
                currentEventCount++;
            }
        }
        if(currentEventCount >= 3) {
            return new ResponseWrapper<>(ResponseHeader.OVER_THREE_CURRENT_EVENT_EXCEPTION, null);
        }
        
        Item requestedItem = null;
        List<Item> itemListByThingId = itemRepository.findByThingId(thingId);
        for(int i = 0; i < itemListByThingId.size(); i++) {
            itemListByThingId.get(i).addInfo(thingRepository, eventRepository);
            if (itemListByThingId.get(i).getStatus().equals("USABLE")) {
                requestedItem = itemListByThingId.get(i);
                break;
            }
        }
        if(requestedItem == null) {
            return new ResponseWrapper<>(ResponseHeader.ITEM_NOT_AVAILABLE_EXCEPTION, null);
        }
        
        requestBody.setItemId(requestedItem.getId());
        requestBody.setResponseManagerId(0);
        requestBody.setResponseManagerName(null);
        requestBody.setReturnManagerId(0);
        requestBody.setReturnManagerName(null);
        requestBody.setLostManagerId(0);
        requestBody.setLostManagerName(null);
        requestBody.setRequestTimeStampNow();
        requestBody.setResponseTimeStampZero();
        requestBody.setReturnTimeStampZero();
        requestBody.setCancelTimeStampZero();
        requestBody.setLostTimeStampZero();
        
        Event eventOutput = eventRepository.save(requestBody);
        eventOutput.addInfo(thingRepository, itemRepository, eventRepository);
        
        requestedItem.setLastEventId(eventOutput.getId());
        itemRepository.save(requestedItem);
        
        ArrayList<Thing> thingListOutput = new ArrayList<>();
        Iterable<ThingDB> allThingDBList = thingRepository.findAll();
        Iterator<ThingDB> iterator = allThingDBList.iterator();
        while(iterator.hasNext()) {
            Thing tmp = iterator.next().toThing();
            tmp.addInfo(thingRepository, itemRepository, eventRepository);
            thingListOutput.add(tmp);
        }
        return new ResponseWrapper<>(ResponseHeader.OK, new PostMappingResponse(eventOutput, thingListOutput));
    }
    
    @PostMapping("/lost/{thingId}/{itemNum}")
    public ResponseWrapper<PostMappingResponse> createLostEvent(@PathVariable int thingId, @PathVariable int itemNum, @RequestBody Event requestBody) { //output에 굳이 thing list 필요할까?
        if(requestBody.getLostManagerId() == 0 || requestBody.getLostManagerName() == null) {
            return new ResponseWrapper<>(ResponseHeader.LACK_OF_REQUEST_BODY_EXCEPTION, null);
        }
        
        List<Item> itemListByThingIdAndNum = itemRepository.findByThingIdAndNum(thingId, itemNum);
        Item requestedItem;
        if(itemListByThingIdAndNum.size() == 0) {
            return new ResponseWrapper<>(ResponseHeader.NOT_FOUND_EXCEPTION, null);
        } else if(itemListByThingIdAndNum.size() != 1) { //Warning 으로 바꿀까?? 그건 좀 귀찮긴 할 듯
            return new ResponseWrapper<>(ResponseHeader.WRONG_IN_DATABASE_EXCEPTION, null);
        }
        
        Optional<Item> requestItemOptional = itemRepository.findById(itemListByThingIdAndNum.get(0).getId());
        if(requestItemOptional.isPresent()) {
            requestedItem = requestItemOptional.get();
            requestedItem.addInfo(thingRepository, eventRepository);
        }
        else {
            return new ResponseWrapper<>(ResponseHeader.NOT_FOUND_EXCEPTION, null);
        }         
        
        if(!requestedItem.getStatus().equals("USABLE")) {
            return new ResponseWrapper<>(ResponseHeader.ITEM_NOT_AVAILABLE_EXCEPTION, null);
        }        
        
        requestBody.setItemId(requestedItem.getId());
        requestBody.setRequesterId(0);
        requestBody.setRequesterName(null);
        requestBody.setResponseManagerId(0);
        requestBody.setResponseManagerName(null);
        requestBody.setReturnManagerId(0);
        requestBody.setReturnManagerName(null);
        requestBody.setRequestTimeStampZero();
        requestBody.setResponseTimeStampZero();
        requestBody.setReturnTimeStampZero();
        requestBody.setCancelTimeStampZero();
        requestBody.setLostTimeStampNow();
        
        Event eventOutput = eventRepository.save(requestBody);
        eventOutput.addInfo(thingRepository, itemRepository, eventRepository);
        
        requestedItem.setLastEventId(eventOutput.getId());
        itemRepository.save(requestedItem);
        
        ArrayList<Thing> thingListOutput = new ArrayList<>();
        Iterable<ThingDB> allThingDBList = thingRepository.findAll();
        Iterator<ThingDB> iterator = allThingDBList.iterator();
        while(iterator.hasNext()) {
            Thing tmp = iterator.next().toThing();
            tmp.addInfo(thingRepository, itemRepository, eventRepository);
            thingListOutput.add(tmp);
        }
        return new ResponseWrapper<>(ResponseHeader.OK, new PostMappingResponse(eventOutput, thingListOutput));
    }

    @PutMapping("/cancel/{id}")
    public ResponseWrapper<List<Event>> cancelItem(@PathVariable int id) { // requester의 event들만 output으로 해야하는가?
        Optional<Event> eventBeforeUpdateOptional = eventRepository.findById(id);
        if(eventBeforeUpdateOptional.isPresent()) {
            Event eventToUpdate = eventBeforeUpdateOptional.get();
            if(eventToUpdate.getStatus().equals("REQUESTED")) {
                eventToUpdate.setCancelTimeStampNow();
                eventRepository.save(eventToUpdate);
                List<Event> output = eventRepository.findByRequesterId(eventToUpdate.getRequesterId());
                for(int i = 0; i < output.size(); i++) {
                    Event tmp = output.get(i);
                    tmp.addInfo(thingRepository, itemRepository, eventRepository);
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

    @PutMapping("/response/{id}")
    public ResponseWrapper<Iterable<Event>> responseItem(@PathVariable int id, @RequestBody Event requestBody) {
        if(requestBody.getResponseManagerId() == 0 || requestBody.getResponseManagerName() == null) {
            return new ResponseWrapper<>(ResponseHeader.LACK_OF_REQUEST_BODY_EXCEPTION, null);
        }
        Optional<Event> eventBeforeUpdateOptional = eventRepository.findById(id);
        if(eventBeforeUpdateOptional.isPresent()) {
            Event eventToUpdate = eventBeforeUpdateOptional.get();
            if(eventToUpdate.getStatus().equals("REQUESTED")) {
                eventToUpdate.setResponseTimeStampNow();
                eventToUpdate.setResponseManagerId(requestBody.getResponseManagerId());
                eventToUpdate.setResponseManagerName(requestBody.getResponseManagerName());
                eventRepository.save(eventToUpdate);
                Iterable<Event> output = eventRepository.findAll();
                Iterator<Event> iterator = output.iterator();
                while (iterator.hasNext()) {
                    Event tmp = iterator.next();
                    tmp.addInfo(thingRepository, itemRepository, eventRepository);
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

    @PutMapping("/return/{id}")
    public ResponseWrapper<Iterable<Event>> returnItem(@PathVariable int id, @RequestBody Event requestBody) {
        if(requestBody.getReturnManagerId() == 0 || requestBody.getReturnManagerName() == null) {
            return new ResponseWrapper<>(ResponseHeader.LACK_OF_REQUEST_BODY_EXCEPTION, null);
        }
        Optional<Event> eventBeforeUpdateOptional = eventRepository.findById(id);
        if(eventBeforeUpdateOptional.isPresent()) {
            Event eventToEvent = eventBeforeUpdateOptional.get();
            if(eventToEvent.getStatus().equals("USING") || eventToEvent.getStatus().equals("DELAYED")) {
                eventToEvent.setReturnTimeStampNow();
                eventToEvent.setReturnManagerId(requestBody.getReturnManagerId());
                eventToEvent.setReturnManagerName(requestBody.getReturnManagerName());
                eventRepository.save(eventToEvent);
                Iterable<Event> output = eventRepository.findAll();
                Iterator<Event> iterator = output.iterator();
                while (iterator.hasNext()) {
                    Event tmp = iterator.next();
                    tmp.addInfo(thingRepository, itemRepository, eventRepository);
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
    
    @PutMapping("/lost/{id}")
    public ResponseWrapper<Iterable<Event>> lostItem(@PathVariable int id, @RequestBody Event requestBody) {
        if(requestBody.getLostManagerId() == 0 || requestBody.getLostManagerName() == null) {
            return new ResponseWrapper<>(ResponseHeader.LACK_OF_REQUEST_BODY_EXCEPTION, null);
        }
        Optional<Event> eventBeforeUpdateOptional = eventRepository.findById(id);
        if(eventBeforeUpdateOptional.isPresent()) {
            Event eventToUpdate = eventBeforeUpdateOptional.get();
            if(eventToUpdate.getStatus().equals("USING") || eventToUpdate.getStatus().equals("DELAYED")) {
                eventToUpdate.setLostTimeStampNow();
                eventToUpdate.setLostManagerId(requestBody.getLostManagerId());
                eventToUpdate.setLostManagerName(requestBody.getLostManagerName());
                eventRepository.save(eventToUpdate);
                Iterable<Event> output = eventRepository.findAll();
                Iterator<Event> iterator = output.iterator();
                while (iterator.hasNext()) {
                    Event tmp = iterator.next();
                    tmp.addInfo(thingRepository, itemRepository, eventRepository);
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
    
    @PutMapping("/found/{id}")
    public ResponseWrapper<Iterable<Event>> foundItem(@PathVariable int id, @RequestBody Event requestBody) {
        if(requestBody.getReturnManagerId() == 0 || requestBody.getReturnManagerName() == null) {
            return new ResponseWrapper<>(ResponseHeader.LACK_OF_REQUEST_BODY_EXCEPTION, null);
        }
        Optional<Event> eventBeforeUpdateOptional = eventRepository.findById(id);
        if(eventBeforeUpdateOptional.isPresent()) {
            Event eventToEvent = eventBeforeUpdateOptional.get();
            if(eventToEvent.getStatus().equals("LOST")) {
                eventToEvent.setReturnTimeStampNow();
                eventToEvent.setReturnManagerId(requestBody.getReturnManagerId());
                eventToEvent.setReturnManagerName(requestBody.getReturnManagerName());
                eventRepository.save(eventToEvent);
                Iterable<Event> output = eventRepository.findAll();
                Iterator<Event> iterator = output.iterator();
                while (iterator.hasNext()) {
                    Event tmp = iterator.next();
                    tmp.addInfo(thingRepository, itemRepository, eventRepository);
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