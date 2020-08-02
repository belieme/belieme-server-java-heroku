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
    public ResponseWrapper<Iterable<ItemType>> getItems() {
        Iterable<ItemTypeDB> tmpItemTypes = itemTypeRepository.findAll();
        ArrayList<ItemType> result = new ArrayList<>();
        for (Iterator<ItemTypeDB> it = tmpItemTypes.iterator(); it.hasNext(); ) {
            ItemType tmpItemType = it.next().toItemType();
            tmpItemType.addInfo(itemTypeRepository, itemRepository, historyRepository);
            result.add(tmpItemType);
        }
        return new ResponseWrapper<>(ResponseHeader.OK, result);
    }

    @GetMapping("/{id}")
    public ResponseWrapper<ItemTypeWithItems> getItem(@PathVariable int id) {
        Optional<ItemTypeDB> tmpItemType =  itemTypeRepository.findById(id);
        if(tmpItemType.isPresent()) {
            ItemTypeWithItems itemType = new ItemTypeWithItems(tmpItemType.get());
            itemType.addInfo(itemTypeRepository, itemRepository, historyRepository);
            return new ResponseWrapper<>(ResponseHeader.OK, itemType);
        }
        return new ResponseWrapper<>(ResponseHeader.NOT_FOUND_EXCEPTION, null);
    }

    @PostMapping("/")
    public ResponseWrapper<Iterable<ItemType>> createItem(@RequestBody ItemType item) {
        if(item.getName() == null || item.getEmoji() == null) {
            return new ResponseWrapper<>(ResponseHeader.LACK_OF_REQUEST_BODY_EXCEPTION, null);
        }
        ItemTypeDB tmpItemType = itemTypeRepository.save(item.toItemTypeDB());
        for(int i = 0; i < item.getAmount(); i++) {
            Item newItem = new Item(tmpItemType.getId(), i + 1);
            itemRepository.save(newItem);
        }
        Iterable<ItemTypeDB> resultItemTypeDB = itemTypeRepository.findAll();
        Iterator<ItemTypeDB> iterator = resultItemTypeDB.iterator();

        ArrayList<ItemType> result = new ArrayList<>();
        while(iterator.hasNext()) {
            ItemType output = iterator.next().toItemType();
            output.addInfo(itemTypeRepository, itemRepository, historyRepository);
            result.add(output);
        }
        return new ResponseWrapper<>(ResponseHeader.OK, result);
    }

    @PutMapping("/")
    public ResponseWrapper<ArrayList<ItemType>> updateItem(@RequestBody ItemType item){
        if(item.getId() == 0 || item.getName() == null || item.getEmoji() == null) { // id가 0으로 자동 생성 될 수 있을까? 그리고 typeId 안쓰면 어차피 뒤에서 걸리는데 필요할까?
            return new ResponseWrapper<>(ResponseHeader.LACK_OF_REQUEST_BODY_EXCEPTION, null);
        }
        Optional<ItemTypeDB> itemBeforeUpdate = itemTypeRepository.findById(item.getId());
        if(itemBeforeUpdate.isPresent()) {
            ItemTypeDB beforeUpdate = itemBeforeUpdate.get();
            ItemTypeDB tmp = item.toItemTypeDB();
            beforeUpdate.setName(tmp.getName());
            beforeUpdate.setEmojiByte(tmp.getEmojiByte());
            itemTypeRepository.save(tmp).toItemType();

            Iterable<ItemTypeDB> resultItemTypeDB = itemTypeRepository.findAll();
            Iterator<ItemTypeDB> iterator = resultItemTypeDB.iterator();

            ArrayList<ItemType> result = new ArrayList<>();
            while(iterator.hasNext()) {
                ItemType output = iterator.next().toItemType();
                output.addInfo(itemTypeRepository, itemRepository, historyRepository);
                result.add(output);
            }
            return new ResponseWrapper<>(ResponseHeader.OK, result);
        }
        return new ResponseWrapper<>(ResponseHeader.NOT_FOUND_EXCEPTION,null); // 여기가 not found가 맞는 것인가
    }

    @PutMapping("/deactivate/{id}")
    public ResponseWrapper<Void> deactivateItem(@PathVariable int id) {
        if(itemTypeRepository.findById(id).isPresent()) {
            List<Item> itemList = itemRepository.findByTypeId(id);
            for (int i = 0; i < itemList.size(); i++) {
                itemList.get(i).deactivate();
                itemRepository.save(itemList.get(i));
            }
            return new ResponseWrapper<>(ResponseHeader.OK, null);
        }
        else {
            return new ResponseWrapper<>(ResponseHeader.NOT_FOUND_EXCEPTION, null);
        }
    }
}