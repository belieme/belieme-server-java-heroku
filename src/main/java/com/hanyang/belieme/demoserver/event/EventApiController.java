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

    @GetMapping("/")
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
    
    @GetMapping("/byRequesterId/{requesterId}")
    public ResponseWrapper<List<Event>> getItemsByRequesterId(@PathVariable int requesterId) {
        List<Event> eventListByRequesterId = eventRepository.findByRequesterId(requesterId);
        for(int i = 0; i < eventListByRequesterId.size(); i++) {
            eventListByRequesterId.get(i).addInfo(thingRepository, itemRepository, eventRepository);
        }
        return new ResponseWrapper<>(ResponseHeader.OK, eventListByRequesterId);
    }

    @PostMapping("/request")
    public ResponseWrapper<PostMappingResponse> createRequestEvent(@RequestBody Event requestBody) {
        if(requestBody.getRequesterId() == 0 || requestBody.getRequesterName() == null || requestBody.thingIdGetter() == 0) { // id가 -1으로 자동 생성 될 수 있을까? 그리고 thingId 안쓰면 어차피 뒤에서 걸리는데 필요할까?
            return new ResponseWrapper<>(ResponseHeader.LACK_OF_REQUEST_BODY_EXCEPTION, null);
        }
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

        List<Event> eventListByItem = eventRepository.findByThingIdAndItemNum(requestBody.thingIdGetter(), requestBody.itemNumGetter()); 
        for(int i = 0; i < eventListByItem.size(); i++) {
            eventListByItem.get(i).addInfo(thingRepository, itemRepository, eventRepository);
            Event tmp = eventListByItem.get(i);
            if(tmp.getStatus().equals("REQUESTED") || tmp.getStatus().equals("USING") || tmp.getStatus().equals("DELAYED") || tmp.getStatus().equals("LOST")) {
                if(tmp.thingIdGetter() == requestBody.thingIdGetter()) {
                    return new ResponseWrapper<>(ResponseHeader.EVENT_FOR_SAME_THING_EXCEPTION, null);
                }
            }
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
        List<Item> items = itemRepository.findByThingId(requestBody.thingIdGetter());
        for(int i = 0; i < items.size(); i++) {
            items.get(i).addInfo(thingRepository, eventRepository);
            if (items.get(i).getStatus().equals("USABLE")) {
                requestedItem = items.get(i);
                break;
            }
        }

        if(requestedItem != null) {
            requestBody.setItemNum(requestedItem.getNum());
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
        else {
            return new ResponseWrapper<>(ResponseHeader.ITEM_NOT_AVAILABLE_EXCEPTION, null);
        }
    }
    
    @PostMapping("/lost")
    public ResponseWrapper<PostMappingResponse> createLostEvent(@RequestBody Event requestBody) {
        if(requestBody.getLostManagerId() == 0 || requestBody.getLostManagerName() == null || requestBody.thingIdGetter() == 0 || requestBody.itemNumGetter() == 0) { // id가 0으로 자동 생성 될 수 있을까? 그리고 thingId 안쓰면 어차피 뒤에서 걸리는데 필요할까?
            return new ResponseWrapper<>(ResponseHeader.LACK_OF_REQUEST_BODY_EXCEPTION, null);
        }
        requestBody.setRequesterId(0);
        requestBody.setRequesterName("");
        requestBody.setResponseManagerId(0);
        requestBody.setResponseManagerName("");
        requestBody.setReturnManagerId(0);
        requestBody.setReturnManagerName("");
        requestBody.setRequestTimeStampZero();
        requestBody.setResponseTimeStampZero();
        requestBody.setReturnTimeStampZero();
        requestBody.setCancelTimeStampZero();
        requestBody.setLostTimeStampNow();

        List<Event> eventListByItem = eventRepository.findByThingIdAndItemNum(requestBody.thingIdGetter(), requestBody.itemNumGetter()); 
        for(int i = 0; i < eventListByItem.size(); i++) {
            eventListByItem.get(i).addInfo(thingRepository, itemRepository, eventRepository);
            Event tmp = eventListByItem.get(i);
            if(tmp.getStatus().equals("REQUESTED") || tmp.getStatus().equals("USING") || tmp.getStatus().equals("DELAYED") || tmp.getStatus().equals("LOST")) {
                if(tmp.thingIdGetter() == requestBody.thingIdGetter()) {
                    return new ResponseWrapper<>(ResponseHeader.EVENT_FOR_SAME_THING_EXCEPTION, null);
                }
            }
        }
        
        List<Item> requestedItemList = itemRepository.findByThingIdAndNum(requestBody.thingIdGetter(), requestBody.itemNumGetter());
        
        if(requestedItemList.size() == 1) {
            Item requestedItem = requestedItemList.get(0);
            requestedItem.addInfo(thingRepository, eventRepository);
            if(requestedItem.getStatus().equals("USABLE")) {
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
            else {
                return new ResponseWrapper<>(ResponseHeader.ITEM_NOT_AVAILABLE_EXCEPTION, null);
            }
        }
        else if(requestedItemList.size() == 0) {
            return new ResponseWrapper<>(ResponseHeader.NOT_FOUND_EXCEPTION, null);
        }
        else {
            return new ResponseWrapper<>(ResponseHeader.WRONG_IN_DATABASE_EXCEPTION, null);
        }
    }

    @PutMapping("/cancel/{id}")
    public ResponseWrapper<List<Event>> cancelItem(@PathVariable int id) {
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