package com.hanyang.belieme.demoserver;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Iterator;
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
    public ResponseWrapper<Iterable<Item>> getItems() {
        Iterable<Item> items = itemRepository.findAll();
        Iterator<Item> iterator = items.iterator();
        while(iterator.hasNext()) {
            Item item = iterator.next();
            item.addInfo(itemTypeRepository, historyRepository);
        }
        return new ResponseWrapper<>(ResponseHeader.OK, items);
    }

    @GetMapping("/byTypeId/{typeId}")
    public ResponseWrapper<Iterable<Item>> getItemsByTypeName(@PathVariable int typeId) {
        Iterable<Item> items = itemRepository.findByTypeId(typeId);
        Iterator<Item> iterator = items.iterator();
        while(iterator.hasNext()) {
            Item item = iterator.next();
            item.addInfo(itemTypeRepository, historyRepository);
        }
        return new ResponseWrapper<>(ResponseHeader.OK, items);
    }

    @GetMapping("/{id}")
    public ResponseWrapper<Item> getItem(@PathVariable int id) {
        Optional<Item> itemOptional = itemRepository.findById(id);
        if(itemOptional.isPresent()) {
            Item item = itemOptional.get();
            item.addInfo(itemTypeRepository, historyRepository);
            return new ResponseWrapper<>(ResponseHeader.OK, item);
        } else {
            return new ResponseWrapper<>(ResponseHeader.NOT_FOUND_EXCEPTION, null);
        }
    }


    @PostMapping("/")
    public ResponseWrapper<Iterable<Item>> createItem(@RequestBody Item item) {
        if(item.typeId == 0) { // id가 0으로 자동 생성 될 수 있을까? 그리고 typeId 안쓰면 어차피 뒤에서 걸리는데 필요할까?
            return new ResponseWrapper<>(ResponseHeader.LACK_OF_REQUEST_BODY_EXCEPTION, null);
        }
        Iterator<Item> iterator;

        Iterable<Item> items = itemRepository.findByTypeId(item.typeIdGetter());
        iterator = items.iterator();

        Optional<ItemTypeDB> type = itemTypeRepository.findById(item.typeIdGetter());

        int max = 0;
        while(iterator.hasNext()) {
            Item tmp = iterator.next();
            if(max < tmp.getNum()) {
                max = tmp.getNum();
            }
        }
        item.setNum(max+1);
        item.setLastHistoryId(-1);

        if(type.isPresent()) {
            itemRepository.save(item);
            Iterable<Item> result = itemRepository.findByTypeId(item.typeIdGetter());
            iterator = result.iterator();

            while(iterator.hasNext()) {
                iterator.next().addInfo(itemTypeRepository, historyRepository);
            }
            return new ResponseWrapper<>(ResponseHeader.OK, result);
        }
        else {
            return new ResponseWrapper<>(ResponseHeader.NOT_FOUND_EXCEPTION, null);
        }
    }

    @PutMapping("/deactivate/{id}")
    public ResponseWrapper<Item> deactivateItem(@PathVariable int id) {
        Optional<Item> itemOptional = itemRepository.findById(id);
        if(itemOptional.isPresent()) {
            Item item = itemOptional.get();
            item.deactivate();
            Item result = itemRepository.save(item);
            result.addInfo(itemTypeRepository,historyRepository);
            return new ResponseWrapper<>(ResponseHeader.OK, result);
        }
        else {
            return new ResponseWrapper<>(ResponseHeader.NOT_FOUND_EXCEPTION, null);
        }
    }

    @PutMapping("/activate/{id}")
    public ResponseWrapper<Item> activateItem(@PathVariable int id) {
        Optional<Item> itemOptional = itemRepository.findById(id);
        if (itemOptional.isPresent()) {
            Item item = itemOptional.get();
            item.activate();
            Item result = itemRepository.save(item);
            result.addInfo(itemTypeRepository,historyRepository);
            return new ResponseWrapper<>(ResponseHeader.OK, result);
        } else {
            return new ResponseWrapper<>(ResponseHeader.NOT_FOUND_EXCEPTION, null);
        }
    }
}