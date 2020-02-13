package com.hanyang.belieme.demoserver;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping(path="/developer")//git ignore
public class DeveloperApiRepository {
//    @Autowired
//    private HistoryRepository historyRepository;
//
//    @Autowired
//    private ItemRepository itemRepository;
//
//    @Autowired
//    private ItemTypeRepository itemTypeRepository;
//
//    @GetMapping("/")
//    public String getInfo() {
//        String result = "${spring.datasource.driver-class-name}" +"\n" +
//                "${spring.datasource.url}" + "\n" +
//                "${spring.datasource.sql-script-encoding}";
//        return result;
//    }
//
//    @GetMapping("/itemType")
//    public Iterable<ItemTypeDB> getItemTypes() {
//        return itemTypeRepository.findAll();
//    }
//
//    @PostMapping("/itemType")
//    public ItemTypeDB createItemType(@RequestBody ItemTypeDB itemType) {
//        return itemTypeRepository.save(itemType);
//    }
//
//    @PutMapping("/itemType")
//    public ItemTypeDB updateItemType(@RequestBody ItemTypeDB itemType){
//        Optional<ItemTypeDB> tmp = itemTypeRepository.findById(itemType.getId());
//        if(!tmp.isPresent()) {
//            return null;
//        }
//        else {
//            return itemTypeRepository.save(itemType);
//        }
//    }
//
//    @DeleteMapping("/itemType/{id}")
//    public ItemTypeDB deleteItemType(@PathVariable int id) {
//        Optional<ItemTypeDB> deletedItem = itemTypeRepository.findById(id);
//        if(!deletedItem.isPresent()) {
//            return null;
//        }
//        else {
//            itemTypeRepository.deleteById(id);
//            return deletedItem.get();
//        }
//    }
//
//    @GetMapping("/item")
//    public Iterable<Item> getItems() {
//        return itemRepository.findAll();
//    }
//
//    @PostMapping("/item")
//    public Item createItem(@RequestBody Item item) {
//        return itemRepository.save(item);
//    }
//
//    @PutMapping("/item")
//    public Item updateItem(@RequestBody Item item){
//        Optional<Item> tmp = itemRepository.findById(item.getId());
//        if(!tmp.isPresent()) {
//            return null;
//        }
//        else {
//            return itemRepository.save(item);
//        }
//    }
//
//    @DeleteMapping("/item/{id}")
//    public Item deleteItem(@PathVariable int id) {
//        Optional<Item> deletedItem = itemRepository.findById(id);
//        if(!deletedItem.isPresent()) {
//            return null;
//        }
//        else {
//            itemRepository.deleteById(id);
//            return deletedItem.get();
//        }
//    }
//
//    @GetMapping("/history")
//    public Iterable<History> getHistories() {
//        return historyRepository.findAll();
//    }
//
//    @PostMapping("/history")
//    public History createHistory(@RequestBody History history) {
//        return historyRepository.save(history);
//    }
//
//    @PutMapping("/history")
//    public History updateHistory(@RequestBody History history){
//        Optional<History> tmp = historyRepository.findById(history.getId());
//        if(!tmp.isPresent()) {
//            return null;
//        }
//        else {
//            return historyRepository.save(history);
//        }
//    }
//
//    @DeleteMapping("/history/{id}")
//    public History deleteHistory(@PathVariable int id) {
//        Optional<History> deletedHistory = historyRepository.findById(id);
//        if(!deletedHistory.isPresent()) {
//            return null;
//        }
//        else {
//            historyRepository.deleteById(id);
//            return deletedHistory.get();
//        }
//    }
}
