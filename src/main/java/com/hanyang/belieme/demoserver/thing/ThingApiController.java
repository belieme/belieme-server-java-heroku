package com.hanyang.belieme.demoserver.thing;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Optional;

import com.hanyang.belieme.demoserver.item.*;
import com.hanyang.belieme.demoserver.event.*;
import com.hanyang.belieme.demoserver.common.*;

@RestController
@RequestMapping(path="/things")
public class ThingApiController {
    @Autowired
    private ThingRepository thingRepository;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private EventRepository eventRepository;

    @GetMapping("")
    public ResponseWrapper<Iterable<Thing>> getItems() {
        Iterable<ThingDB> tmpThings = thingRepository.findAll();
        ArrayList<Thing> responseBody = new ArrayList<>();
        for (Iterator<ThingDB> it = tmpThings.iterator(); it.hasNext(); ) {
            Thing tmp = it.next().toThing();
            tmp.addInfo(thingRepository, itemRepository, eventRepository);
            responseBody.add(tmp);
        }
        return new ResponseWrapper<>(ResponseHeader.OK, responseBody);
    }

    @GetMapping("/{id}")
    public ResponseWrapper<ThingWithItems> getItem(@PathVariable int id) {
        Optional<ThingDB> tmpThing =  thingRepository.findById(id);
        if(tmpThing.isPresent()) {
            ThingWithItems responseBody = tmpThing.get().toThing().toThingWithItems();
            responseBody.addInfo(thingRepository, itemRepository, eventRepository);
            return new ResponseWrapper<>(ResponseHeader.OK, responseBody);
        }
        return new ResponseWrapper<>(ResponseHeader.NOT_FOUND_EXCEPTION, null);
    }

    @PostMapping("")
    public ResponseWrapper<Iterable<Thing>> createItem(@RequestBody Thing requestBody) {
        if(requestBody.getName() == null || requestBody.getEmoji() == null) {
            return new ResponseWrapper<>(ResponseHeader.LACK_OF_REQUEST_BODY_EXCEPTION, null);
        }
        ThingDB savedThing = thingRepository.save(requestBody.toThingDB());
        for(int i = 0; i < requestBody.getAmount(); i++) {
            Item newItem = new Item(savedThing.getId(), i + 1);
            itemRepository.save(newItem);
        }
        
        Iterable<ThingDB> allThingsListDB = thingRepository.findAll();
        Iterator<ThingDB> iterator = allThingsListDB.iterator();

        ArrayList<Thing> responseBody = new ArrayList<>();
        while(iterator.hasNext()) {
            Thing tmp = iterator.next().toThing();
            tmp.addInfo(thingRepository, itemRepository, eventRepository);
            responseBody.add(tmp);
        }
        return new ResponseWrapper<>(ResponseHeader.OK, responseBody);
    }

    @PutMapping("")
    public ResponseWrapper<ArrayList<Thing>> updateItem(@RequestBody Thing requestBody){
        if(requestBody.getId() == 0 || requestBody.getName() == null || requestBody.getEmoji() == null) { // id가 0으로 자동 생성 될 수 있을까? 그리고 Id 안쓰면 어차피 뒤에서 걸리는데 필요할까?
            return new ResponseWrapper<>(ResponseHeader.LACK_OF_REQUEST_BODY_EXCEPTION, null);
        }
        Optional<ThingDB> targetOptional = thingRepository.findById(requestBody.getId());
        if(targetOptional.isPresent()) {
            ThingDB target = targetOptional.get();
            ThingDB requestBodyDB = requestBody.toThingDB();
            target.setName(requestBodyDB.getName());
            target.setEmojiByte(requestBodyDB.getEmojiByte());
            thingRepository.save(target).toThing();

            Iterable<ThingDB> allThingsListDB = thingRepository.findAll();
            Iterator<ThingDB> iterator = allThingsListDB.iterator();

            ArrayList<Thing> responseBody = new ArrayList<>();
            while(iterator.hasNext()) {
                Thing tmp = iterator.next().toThing();
                tmp.addInfo(thingRepository, itemRepository, eventRepository);
                responseBody.add(tmp);
            }
            return new ResponseWrapper<>(ResponseHeader.OK, responseBody);
        }
        return new ResponseWrapper<>(ResponseHeader.NOT_FOUND_EXCEPTION,null); // 여기가 not found가 맞는 것인가
    }

    // deactivate Thing을 만들긴 해야할 거 같지만 생각좀 해봐야 할 듯
    // @PutMapping("/deactivate/{id}/")
    // public ResponseWrapper<Void> deactivateItem(@PathVariable int id) {
    //     if(itemTypeRepository.findById(id).isPresent()) {
    //         List<Item> itemList = itemRepository.findByTypeId(id);
    //         for (int i = 0; i < itemList.size(); i++) {
    //             itemList.get(i).deactivate();
    //             itemRepository.save(itemList.get(i));
    //         }
    //         return new ResponseWrapper<>(ResponseHeader.OK, null);
    //     }
    //     else {
    //         return new ResponseWrapper<>(ResponseHeader.NOT_FOUND_EXCEPTION, null);
    //     }
    // }
}