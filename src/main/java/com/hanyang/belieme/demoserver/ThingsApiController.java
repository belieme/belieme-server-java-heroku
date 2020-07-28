package com.hanyang.belieme.demoserver;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping(path="/things")
public class ThingsApiController {
    @Autowired
    private ThingsRepository thingsRepository;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private HistoryRepository historyRepository;

    @GetMapping("/")
    public ResponseWrapper<Iterable<Things>> getItems() {
        Iterable<ThingsDB> tmpThingss = thingsRepository.findAll();
        ArrayList<Things> result = new ArrayList<>();
        for (Iterator<ThingsDB> it = tmpThingss.iterator(); it.hasNext(); ) {
            Things tmpThings = it.next().toThings();
            tmpThings.addInfo(thingsRepository, itemRepository, historyRepository);
            result.add(tmpThings);
        }
        return new ResponseWrapper<>(ResponseHeader.OK, result);
    }

    @GetMapping("/{id}")
    public ResponseWrapper<Things> getItem(@PathVariable int id) {
        Optional<ThingsDB> tmpThings =  thingsRepository.findById(id);
        if(tmpThings.isPresent()) {
            Things things = tmpThings.get().toThings();
            things.addInfo(thingsRepository, itemRepository, historyRepository);
            return new ResponseWrapper<>(ResponseHeader.OK, things);
        }
        return new ResponseWrapper<>(ResponseHeader.NOT_FOUND_EXCEPTION, null);
    }

    @PostMapping("/")
    public ResponseWrapper<Iterable<Things>> createItem(@RequestBody Things item) {
        if(item.getName() == null || item.getEmoji() == null) {
            return new ResponseWrapper<>(ResponseHeader.LACK_OF_REQUEST_BODY_EXCEPTION, null);
        }
        ThingsDB tmpThings = thingsRepository.save(item.toThingsDB());
        for(int i = 0; i < item.getAmount(); i++) {
            Item newItem = new Item(tmpThings.getId(), i + 1);
            itemRepository.save(newItem);
        }
        Iterable<ThingsDB> resultThingsDB = thingsRepository.findAll();
        Iterator<ThingsDB> iterator = resultThingsDB.iterator();

        ArrayList<Things> result = new ArrayList<>();
        while(iterator.hasNext()) {
            Things output = iterator.next().toThings();
            output.addInfo(thingsRepository, itemRepository, historyRepository);
            result.add(output);
        }
        return new ResponseWrapper<>(ResponseHeader.OK, result);
    }

    @PutMapping("/")
    public ResponseWrapper<ArrayList<Things>> updateItem(@RequestBody Things item){
        if(item.getId() == 0 || item.getName() == null || item.getEmoji() == null) { // id가 0으로 자동 생성 될 수 있을까? 그리고 typeId 안쓰면 어차피 뒤에서 걸리는데 필요할까?
            return new ResponseWrapper<>(ResponseHeader.LACK_OF_REQUEST_BODY_EXCEPTION, null);
        }
        Optional<ThingsDB> itemBeforeUpdate = thingsRepository.findById(item.getId());
        if(itemBeforeUpdate.isPresent()) {
            ThingsDB beforeUpdate = itemBeforeUpdate.get();
            ThingsDB tmp = item.toThingsDB();
            beforeUpdate.setName(tmp.getName());
            beforeUpdate.setEmojiByte(tmp.getEmojiByte());
            thingsRepository.save(tmp).toThings();

            Iterable<ThingsDB> resultThingsDB = thingsRepository.findAll();
            Iterator<ThingsDB> iterator = resultThingsDB.iterator();

            ArrayList<Things> result = new ArrayList<>();
            while(iterator.hasNext()) {
                Things output = iterator.next().toThings();
                output.addInfo(thingsRepository, itemRepository, historyRepository);
                result.add(output);
            }
            return new ResponseWrapper<>(ResponseHeader.OK, result);
        }
        return new ResponseWrapper<>(ResponseHeader.NOT_FOUND_EXCEPTION,null); // 여기가 not found가 맞는 것인가
    }

    @PutMapping("/deactivate/{id}")
    public ResponseWrapper<Void> deactivateItem(@PathVariable int id) {
        if(thingsRepository.findById(id).isPresent()) {
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