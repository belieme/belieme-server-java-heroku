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
            addInfo(item);
        }
        return items;
    }

    @GetMapping("/byTypeId/{typeId}")
    public Iterable<Item> getItemsByTypeName(@PathVariable int typeId) {
        Iterable<Item> items = itemRepository.findByTypeId(typeId);
        Iterator<Item> iterator = items.iterator();
        while(iterator.hasNext()) {
            Item item = iterator.next();
            addInfo(item);
        }
        return items;
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
            addInfo(result);
            return result;
        }
        return null;
    }

//    @PutMapping("/")
//    public String updateItem(@RequestBody Item item){
//        Optional<Item> itemBeforeUpdate = itemRepository.findById(item.getId());
//        if(!itemBeforeUpdate.isEmpty()) {
//            Item tmp = itemBeforeUpdate.get();
//            tmp.setLastHistoryId(item.getLastHistoryId());
//            itemRepository.save(tmp);
//            return "true";
//        }
//        return "false";
//    }

    @DeleteMapping("/{id}")
    public void deleteItem(@PathVariable int id) {
        itemRepository.deleteById(id);
    }

    private void addInfo(Item item) {
        Optional<History> lastHistory = historyRepository.findById(item.getLastHistoryId());
        if(lastHistory.isPresent()) {
            String lastHistoryStatus = lastHistory.get().getStatus();
            if(lastHistoryStatus.equals("EXPIRED")||lastHistoryStatus.equals("RETURNED")) {
                item.usableStatus();
            }
            else {
                item.unusableStatus();
            }
        }
        else {
            item.usableStatus();
        }
        Optional<ItemTypeDB> itemType = itemTypeRepository.findById(item.getTypeId());

        if(itemType.isPresent()) {
            item.setTypeName(itemType.get().getName());
            item.setTypeEmoji(itemType.get().toItemType().getEmoji());
        }
        else {
            item.setTypeName("");
            item.setTypeEmoji("");
        }
    }
}