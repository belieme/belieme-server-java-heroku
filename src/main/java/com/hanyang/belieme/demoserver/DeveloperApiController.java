package com.hanyang.belieme.demoserver;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.Optional;

@RestController
@RequestMapping(path="/developer")//git ignore
public class DeveloperApiController {
    @Autowired
    private HistoryRepository historyRepository;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private ThingsRepository thingsRepository;

    @GetMapping("/")
    public String getInfo() {
        String result = "timeStamp : " + System.currentTimeMillis()/1000 + "\n" +
                "Date : " + (new Date(System.currentTimeMillis())) + "\n";
        return result;
    }

    @GetMapping("/things")
    public Iterable<ThingsDB> getThingss() {
        return thingsRepository.findAll();
    }

    @PostMapping("/things")
    public ThingsDB createThings(@RequestBody ThingsDB things) {
        return thingsRepository.save(things);
    }

    @PutMapping("/things")
    public ThingsDB updateThings(@RequestBody ThingsDB things){
        Optional<ThingsDB> tmp = thingsRepository.findById(things.getId());
        if(!tmp.isPresent()) {
            return null;
        }
        else {
            return thingsRepository.save(things);
        }
    }

    @DeleteMapping("/things/{id}")
    public ThingsDB deleteThings(@PathVariable int id) {
        Optional<ThingsDB> deletedItem = thingsRepository.findById(id);
        if(!deletedItem.isPresent()) {
            return null;
        }
        else {
            thingsRepository.deleteById(id);
            return deletedItem.get();
        }
    }

    @GetMapping("/item")
    public Iterable<Item> getItems() {
        return itemRepository.findAll();
    }

    @PostMapping("/item")
    public Item createItem(@RequestBody Item item) {
        return itemRepository.save(item);
    }

    @PutMapping("/item")
    public Item updateItem(@RequestBody Item item){
        Optional<Item> tmp = itemRepository.findById(item.getId());
        if(!tmp.isPresent()) {
            return null;
        }
        else {
            return itemRepository.save(item);
        }
    }

    @DeleteMapping("/item/{id}")
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

    @GetMapping("/history")
    public Iterable<History> getHistories() {
        return historyRepository.findAll();
    }

    @PostMapping("/history")
    public History createHistory(@RequestBody History history) {
        return historyRepository.save(history);
    }

    @PutMapping("/history")
    public History updateHistory(@RequestBody History history){
        Optional<History> tmp = historyRepository.findById(history.getId());
        if(!tmp.isPresent()) {
            return null;
        }
        else {
            return historyRepository.save(history);
        }
    }

    @DeleteMapping("/history/{id}")
    public History deleteHistory(@PathVariable int id) {
        Optional<History> deletedHistory = historyRepository.findById(id);
        if(!deletedHistory.isPresent()) {
            return null;
        }
        else {
            historyRepository.deleteById(id);
            return deletedHistory.get();
        }
    }

    @DeleteMapping("/deleteAll")
    public String deleteAll() {
        historyRepository.deleteAll();
        itemRepository.deleteAll();
        thingsRepository.deleteAll();
        return "OK";
    }
}
