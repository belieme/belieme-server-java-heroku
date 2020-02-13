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

    @GetMapping("/")
    public ArrayList<ItemType> getItems() {
        Iterable<ItemTypeDB> tmpItemTypes = itemTypeRepository.findAll();
        ArrayList<ItemType> result = new ArrayList<ItemType>();
        for (Iterator<ItemTypeDB> it = tmpItemTypes.iterator(); it.hasNext(); ) {
            ItemTypeDB tmpItemType = it.next();
            result.add(tmpItemType.toItemType());
        }
        return result;

    }

    @GetMapping("/{id}")
    public Optional<ItemType> getItem(@PathVariable int id) {
        Optional<ItemTypeDB> tmpItemType =  itemTypeRepository.findById(id);
        if(tmpItemType.isPresent()) {
            return Optional.of(tmpItemType.get().toItemType());
        }
        return Optional.empty();
    }

    @PostMapping("/")
    public ItemType createItem(@RequestBody ItemType item) {
        item.setCount(0);
        item.setAmount(0);
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
}