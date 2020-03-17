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
        int i = 0;
        for (Iterator<ItemTypeDB> it = tmpItemTypes.iterator(); it.hasNext(); ) {
            ItemType tmpItemType = it.next().toItemType();
            tmpItemType.addInfo(itemTypeRepository, itemRepository, historyRepository);
            result.add(tmpItemType);
        }
        return result;

    }

    @GetMapping("/{id}")
    public Optional<ItemType> getItem(@PathVariable int id) {
        Optional<ItemTypeDB> tmpItemType =  itemTypeRepository.findById(id);
        if(tmpItemType.isPresent()) {
            ItemType itemType = tmpItemType.get().toItemType();
            itemType.addInfo(itemTypeRepository, itemRepository, historyRepository);
            return Optional.of(itemType);
        }
        return Optional.empty();
    }

    @PostMapping("/")
    public ItemType createItem(@RequestBody ItemType item) {
        ItemTypeDB tmpItemType = itemTypeRepository.save(item.toItemTypeDB());
        for(int i = 0; i < item.getAmount(); i++) {
            Item newItem = new Item(tmpItemType.getId(), i + 1);
            itemRepository.save(newItem);
        }
        ItemType output = tmpItemType.toItemType();
        output.addInfo(itemTypeRepository, itemRepository, historyRepository);
        return output;
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

    @PutMapping("/deactivate/{id}")
    public void deactivateItem(@PathVariable int id) {
        List<Item> itemList = itemRepository.findByTypeId(id);
        for(int i = 0; i < itemList.size(); i++) {
            itemList.get(i).deactivate();
            itemRepository.save(itemList.get(i));
        }
    }
}