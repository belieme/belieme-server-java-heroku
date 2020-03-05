package com.hanyang.belieme.demoserver;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping(path="/itemType")
public class ItemTypeApiController {
    @Autowired
    private ItemTypeRepository itemTypeRepository;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private HistoryRepository historyRepository;

    @GetMapping("/")
    public ArrayList<ItemType> getItems() {
        Iterable<ItemTypeDB> tmpItemTypes = itemTypeRepository.findAll();
        ArrayList<ItemType> result = new ArrayList<ItemType>();
        for (Iterator<ItemTypeDB> it = tmpItemTypes.iterator(); it.hasNext(); ) {
            ItemTypeDB tmpItemType = it.next();
            result.add(tmpItemType.toItemType());
        }
        for (int i = 0; i < result.size(); i++) {
            ItemType itemType = result.get(i);
            itemType.resetCount();
            itemType.resetAmount();
            List<Item> itemList = itemRepository.findByTypeId(itemType.getId());
            for(int j = 0; j < itemList.size(); j++) {
                Item item = itemList.get(j);
                addInfo(item);
                if(item.getStatus().equals("USABLE")) {
                    itemType.increaseCount();
                }
                itemType.increaseAmount();
            }
        }
        return result;

    }

    @GetMapping("/{id}")
    public Optional<ItemType> getItem(@PathVariable int id) {
        Optional<ItemTypeDB> tmpItemType =  itemTypeRepository.findById(id);
        if(tmpItemType.isPresent()) {
            ItemType itemType = tmpItemType.get().toItemType();

            itemType.resetCount();
            itemType.resetAmount();
            List<Item> itemList = itemRepository.findByTypeId(itemType.getId());
            for(int i = 0; i < itemList.size(); i++) {
                Item item = itemList.get(i);
                addInfo(item);
                if(item.getStatus().equals("USABLE")) {
                    itemType.increaseCount();
                }
                itemType.increaseAmount();
            }

            return Optional.of(itemType);
        }
        return Optional.empty();
    }

    @PostMapping("/")
    public ItemType createItem(@RequestBody ItemType item) {
        item.resetCount();
        item.resetAmount();
        ItemTypeDB tmpItemType = itemTypeRepository.save(item.toItemTypeDB());
        return tmpItemType.toItemType();
    }

    @PutMapping("/")
    public String updateItem(@RequestBody ItemType item){
        Optional<ItemTypeDB> itemBeforeUpdate = itemTypeRepository.findById(item.getId());
        if(itemBeforeUpdate.isPresent()) {
            ItemTypeDB beforeUpdate = itemBeforeUpdate.get();
            ItemTypeDB tmp = item.toItemTypeDB();
            beforeUpdate.setName(tmp.getName());
            beforeUpdate.setEmojiByte(tmp.getEmojiByte());
            itemTypeRepository.save(tmp);
            return "true";
        }
        return "false";
    }

    @DeleteMapping("/{id}")
    public void deleteItem(@PathVariable int id) {
        itemTypeRepository.deleteById(id);

        List<Item> itemList = itemRepository.findByTypeId(id);
        for(int i = 0; i < itemList.size(); i++) {
            itemRepository.deleteById(itemList.get(i).getId());
        }
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