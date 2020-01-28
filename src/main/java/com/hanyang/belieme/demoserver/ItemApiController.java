package com.hanyang.belieme.demoserver;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping(path="/item")
public class ItemApiController {
    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private ItemTypeRepository itemTypeRepository;

    @GetMapping("/")
    public Iterable<Item> getItems() {
        return itemRepository.findAll();
    }

    @GetMapping("/byTypeId/{typeId}")
    public Iterable<Item> getItemsByTypeName(@PathVariable int typeId) {
        return itemRepository.findByTypeId(typeId);
    }


    @PostMapping("/")
    public Item createItem(@RequestBody Item item) {
        List<Item> items = itemRepository.findByTypeId(item.getTypeId());

        Optional<ItemType> type = itemTypeRepository.findById(item.getTypeId());

        int max = 0;
        for(int i = 0; i < items.size(); i++) {
            if(max < items.get(i).getNum()) {
                max = items.get(i).getNum();
            }
        }
        item.setNum(max+1);
        item.setStatus("USABLE");
        item.setLastHistoryId(-1);

        if(!type.isEmpty()) {
            ItemType newType = type.get();
            newType.setAmount(newType.getAmount() +  1);
            newType.setCount(newType.getCount() + 1);
            itemTypeRepository.save(newType);
            return itemRepository.save(item);
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
        Optional<Item> deletedItem = itemRepository.findById(id);
        if(!deletedItem.isEmpty()) {
            ItemType type = itemTypeRepository.findById(deletedItem.get().getTypeId()).get();
            if(type != null) {
                type.setAmount(type.getAmount() - 1);
                if(deletedItem.get().getStatus().equals("USABLE")) {
                    type.setCount(type.getCount() - 1);
                }
                itemTypeRepository.save(type);
            }
        }
        itemRepository.deleteById(id);
    }
}