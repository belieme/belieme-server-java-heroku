package com.hanyang.belieme.demoserver.developer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.Optional;

import com.hanyang.belieme.demoserver.thing.*;
import com.hanyang.belieme.demoserver.item.*;
import com.hanyang.belieme.demoserver.event.*;

@RestController
@RequestMapping(path="/developer")//git ignore
public class DeveloperApiController {
    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private ThingRepository thingRepository;

    @GetMapping("/")
    public String getInfo() {
        String result = "timeStamp : " + System.currentTimeMillis()/1000 + "\n" +
                "Date : " + (new Date(System.currentTimeMillis())) + "\n";
        return result;
    }

    @GetMapping("/things")
    public Iterable<ThingDB> getThings() {
        return thingRepository.findAll();
    }

    @PostMapping("/things")
    public ThingDB createThing(@RequestBody ThingDB thing) {
        return thingRepository.save(thing);
    }

    @PutMapping("/things")
    public ThingDB updateThing(@RequestBody ThingDB thing){
        Optional<ThingDB> tmp = thingRepository.findById(thing.getId());
        if(!tmp.isPresent()) {
            return null;
        }
        else {
            return thingRepository.save(thing);
        }
    }

    @DeleteMapping("/things/{id}")
    public ThingDB deleteThing(@PathVariable int id) {
        Optional<ThingDB> deletedItem = thingRepository.findById(id);
        if(!deletedItem.isPresent()) {
            return null;
        }
        else {
            thingRepository.deleteById(id);
            return deletedItem.get();
        }
    }
    
    @DeleteMapping("/things/deleteAll")
    public String deleteAllThing() {
        thingRepository.deleteAll();
        return "deleted all thing";
    }

    @GetMapping("/items")
    public Iterable<Item> getItems() {
        return itemRepository.findAll();
    }

    @PostMapping("/items")
    public Item createItem(@RequestBody Item item) {
        return itemRepository.save(item);
    }

    @PutMapping("/items/{id}")
    public Item updateItem(@PathVariable int id, @RequestBody Item item){
        Optional<Item> tmp = itemRepository.findById(id);
        if(!tmp.isPresent()) {
            return null;
        }
        else {
            return itemRepository.save(item);
        }
    }

    @DeleteMapping("/items/{id}")
    public Item deleteItem(@PathVariable int id) {
        Optional<Item> deletedItem = itemRepository.findById(id);
        if(!deletedItem.isPresent()) {
            return null;
        }
        else {
            itemRepository.deleteById(id);
            return deletedItem.get();
        }
    }
    
    @DeleteMapping("/items/deleteAll")
    public String deleteAllItem() {
        itemRepository.deleteAll();
        return "deleted all item";
    }

    @GetMapping("/events")
    public Iterable<Event> getHistories() {
        return eventRepository.findAll();
    }

    @PostMapping("/events")
    public Event createEvent(@RequestBody Event event) {
        return eventRepository.save(event);
    }

    @PutMapping("/events")
    public Event updateEvent(@RequestBody Event event){
        Optional<Event> tmp = eventRepository.findById(event.getId());
        if(!tmp.isPresent()) {
            return null;
        }
        else {
            return eventRepository.save(event);
        }
    }

    @DeleteMapping("/events/{id}")
    public Event deleteEvent(@PathVariable int id) {
        Optional<Event> deletedEvent = eventRepository.findById(id);
        if(!deletedEvent.isPresent()) {
            return null;
        }
        else {
            eventRepository.deleteById(id);
            return deletedEvent.get();
        }
    }
    
    @DeleteMapping("/events/deleteAll")
    public String deleteAllEvent() {
        eventRepository.deleteAll();
        return "deleted all histories";
    }

    @DeleteMapping("/deleteAll")
    public String deleteAll() {
        eventRepository.deleteAll();
        itemRepository.deleteAll();
        thingRepository.deleteAll();
        return "OK";
    }
}
