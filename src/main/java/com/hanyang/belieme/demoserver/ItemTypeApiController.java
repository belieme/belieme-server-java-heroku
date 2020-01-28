package com.hanyang.belieme.demoserver;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

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
    public Iterable<ItemType> getItems() {
        return itemTypeRepository.findAll();
    }

    @GetMapping("/{id}")
    public Optional<ItemType> getItem(@PathVariable int id) {
        return itemTypeRepository.findById(id);
    }

    @PostMapping("/")
    public ItemType createItem(@RequestBody ItemType item) {
        item.setCount(0);
        item.setAmount(0);
        return itemTypeRepository.save(item);
    }

    @PutMapping("/")
    public String updateItem(@RequestBody ItemType item){
        Optional<ItemType> itemBeforeUpdate = itemTypeRepository.findById(item.getId());
        if(itemBeforeUpdate.isPresent()) {
            ItemType tmp = itemBeforeUpdate.get();
            tmp.setName(item.getName());
            tmp.setEmoji(item.getEmoji());
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