package com.hanyang.belieme.demoserver;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Iterator;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping(path="/item")
public class ItemApiController {
    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private ItemTypeRepository itemTypeRepository;

    @Autowired
    private HistoryRepository historyRepository;

    @GetMapping("/")
    public Iterable<Item> getItems() {
        Iterable<Item> items = itemRepository.findAll();
        Iterator<Item> iterator = items.iterator();
        while(iterator.hasNext()) {
            Item item = iterator.next();
            item.addInfo(itemTypeRepository, historyRepository);
        }
        return items;
    }

    @GetMapping("/byTypeId/{typeId}")
    public Iterable<Item> getItemsByTypeName(@PathVariable int typeId) {
        Iterable<Item> items = itemRepository.findByTypeId(typeId);
        Iterator<Item> iterator = items.iterator();
        while(iterator.hasNext()) {
            Item item = iterator.next();
            item.addInfo(itemTypeRepository, historyRepository);
        }
        return items;
    }

    @GetMapping("/{id}")
    public Optional<Item> getItem(@PathVariable int id) {
        Optional<Item> itemOptional = itemRepository.findById(id);
        if(itemOptional.isPresent()) {
            Item item = itemOptional.get();
            item.addInfo(itemTypeRepository, historyRepository);
        }
        return itemOptional;
    }


    @PostMapping("/")
    public Item createItem(@RequestBody Item item) {
        List<Item> items = itemRepository.findByTypeId(item.getTypeId());

        Optional<ItemTypeDB> type = itemTypeRepository.findById(item.getTypeId());

        int max = 0;
        for(int i = 0; i < items.size(); i++) {
            if(max < items.get(i).getNum()) {
                max = items.get(i).getNum();
            }
        }
        item.setNum(max+1);
        item.setLastHistoryId(-1);

        if(type.isPresent()) {
            Item result = itemRepository.save(item);
            result.addInfo(itemTypeRepository, historyRepository);
            return result;
        }
        return null;
    }

    @PutMapping("/deactivate/{id}")
    public String deactivateItem(@PathVariable int id) {
        Optional<Item> itemOptional = itemRepository.findById(id);
        if(itemOptional.isPresent()) {
            Item item = itemOptional.get();
            item.deactivate();
            itemRepository.save(item);
            return "true";
        }
        return "false";
    }

    @PutMapping("/activate/{id}")
    public String activateItem(@PathVariable int id) {
        Optional<Item> itemOptional = itemRepository.findById(id);
        if(itemOptional.isPresent()) {
            Item item = itemOptional.get();
            item.activate();
            itemRepository.save(item);
            return "true";
        }
        return "false";
    }
}