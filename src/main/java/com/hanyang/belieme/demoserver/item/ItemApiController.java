package com.hanyang.belieme.demoserver.item;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import com.hanyang.belieme.demoserver.thing.*;
import com.hanyang.belieme.demoserver.event.*;
import com.hanyang.belieme.demoserver.common.*;


@RestController
@RequestMapping(path="/items")
public class ItemApiController {
    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private ThingRepository thingRepository;

    @Autowired
    private EventRepository eventRepository;

    @GetMapping("")
    public ResponseWrapper<Iterable<Item>> getAllItems() {
        Iterable<Item> allItemList = itemRepository.findAll();
        Iterator<Item> iterator = allItemList.iterator();
        while(iterator.hasNext()) {
            Item item = iterator.next();
            item.addInfo(thingRepository, eventRepository);
        }
        return new ResponseWrapper<>(ResponseHeader.OK, allItemList);
    }

    @GetMapping("/byThingId/{thingId}")
    public ResponseWrapper<List<Item>> getItemsByThingId(@PathVariable int thingId) {
        List<Item> itemListByThingId = itemRepository.findByThingId(thingId);
        for(int i = 0; i < itemListByThingId.size(); i++) {
            itemListByThingId.get(i).addInfo(thingRepository, eventRepository);
        }
        return new ResponseWrapper<>(ResponseHeader.OK, itemListByThingId);
    }

    @GetMapping("/{thingId}/{itemNum}") 
    public ResponseWrapper<Item> getItemByThingIdAndNum(@PathVariable int thingId, @PathVariable int num) {
        List<Item> itemList = itemRepository.findByThingIdAndNum(thingId, num);
        if(itemList.size() == 1) {
            Item output = itemList.get(0);
            output.addInfo(thingRepository, eventRepository);
            return new ResponseWrapper<>(ResponseHeader.OK, output);
        } else if(itemList.size() == 0) {
            return new ResponseWrapper<>(ResponseHeader.NOT_FOUND_EXCEPTION, null);
        } else { //Warning 으로 바꿀까?? 그건 좀 귀찮긴 할 듯
            return new ResponseWrapper<>(ResponseHeader.WRONG_IN_DATABASE_EXCEPTION, null);
        }
    }

    @PostMapping("")
    public ResponseWrapper<List<Item>> createNewItem(@RequestBody Item requestBody) {
        if(requestBody.thingIdGetter() == 0) {
            return new ResponseWrapper<>(ResponseHeader.LACK_OF_REQUEST_BODY_EXCEPTION, null);
        }

        List<Item> itemListByThingId = itemRepository.findByThingId(requestBody.thingIdGetter());

        Optional<ThingDB> thingOptional = thingRepository.findById(requestBody.thingIdGetter());

        int max = 0;
        for(int i = 0; i < itemListByThingId.size(); i++) {
            Item tmp = itemListByThingId.get(i);
            if(max < tmp.getNum()) {
                max = tmp.getNum();
            }
        }
        requestBody.setNum(max+1);
        requestBody.setLastEventId(0); //default는 0? -1?

        if(thingOptional.isPresent()) {
            itemRepository.save(requestBody);
            List<Item> responseBody = itemRepository.findByThingId(requestBody.thingIdGetter());
            
            for(int i = 0; i < responseBody.size(); i++) {
                responseBody.get(i).addInfo(thingRepository, eventRepository);
            }
            return new ResponseWrapper<>(ResponseHeader.OK, responseBody);
        }
        else {
            return new ResponseWrapper<>(ResponseHeader.NOT_FOUND_EXCEPTION, null);
        }
    }
}