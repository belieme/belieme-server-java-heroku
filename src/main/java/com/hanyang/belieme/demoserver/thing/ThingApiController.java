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
    public ResponseWrapper<Iterable<Thing>> getAllThings() {
        Iterable<ThingDB> allThingDBList = thingRepository.findAll();
        ArrayList<Thing> responseBody = new ArrayList<>();
        for (Iterator<ThingDB> it = allThingDBList.iterator(); it.hasNext(); ) {
            Thing tmp = it.next().toThing();
            tmp.addInfo(thingRepository, itemRepository, eventRepository);
            responseBody.add(tmp);
        }
        return new ResponseWrapper<>(ResponseHeader.OK, responseBody);
    }

    @GetMapping("/{id}")
    public ResponseWrapper<ThingWithItems> getThingById(@PathVariable int id) {
        Optional<ThingDB> targetOptional = thingRepository.findById(id);
        if(targetOptional.isPresent()) {
            ThingWithItems responseBody = targetOptional.get().toThing().toThingWithItems();
            responseBody.addInfo(thingRepository, itemRepository, eventRepository);
            return new ResponseWrapper<>(ResponseHeader.OK, responseBody);
        }
        return new ResponseWrapper<>(ResponseHeader.NOT_FOUND_EXCEPTION, null);
    }

    @PostMapping("")
    public ResponseWrapper<Iterable<Thing>> createNewThing(@RequestBody Thing requestBody) {
        if(requestBody.getName() == null || requestBody.getEmoji() == null) { //getAmount는 체크 안하는 이유가 amout를 입력 안하면 0으로 자동저장 되어서 item이 0개인 thing이 생성된다.
            return new ResponseWrapper<>(ResponseHeader.LACK_OF_REQUEST_BODY_EXCEPTION, null);
        }
        ThingDB savedThing = thingRepository.save(requestBody.toThingDB()); 
        for(int i = 0; i < requestBody.getAmount(); i++) { // requestBody에 amout값이 주어졌을때 작동 됨
            Item newItem = new Item(savedThing.getId(), i + 1);
            itemRepository.save(newItem);
        }
        
        Iterable<ThingDB> allThingDBList = thingRepository.findAll();
        Iterator<ThingDB> iterator = allThingDBList.iterator();

        ArrayList<Thing> responseBody = new ArrayList<>();
        while(iterator.hasNext()) {
            Thing tmp = iterator.next().toThing();
            tmp.addInfo(thingRepository, itemRepository, eventRepository);
            responseBody.add(tmp);
        }
        return new ResponseWrapper<>(ResponseHeader.OK, responseBody);
    }

    @PutMapping("{id}")
    public ResponseWrapper<ArrayList<Thing>> updateNameAndEmojiOfThing(@PathVariable int id, @RequestBody Thing requestBody){
        if(requestBody.getName() == null || requestBody.getEmoji() == null) {
            return new ResponseWrapper<>(ResponseHeader.LACK_OF_REQUEST_BODY_EXCEPTION, null);
        }
        Optional<ThingDB> targetOptional = thingRepository.findById(id);
        if(targetOptional.isPresent()) {
            ThingDB target = targetOptional.get();
            ThingDB requestBodyDB = requestBody.toThingDB();
            target.setName(requestBodyDB.getName());
            target.setEmojiByte(requestBodyDB.getEmojiByte());
            thingRepository.save(target);

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
        return new ResponseWrapper<>(ResponseHeader.NOT_FOUND_EXCEPTION,null);
    }

    // deactivate Thing을 만들긴 해야할 거 같지만 생각좀 해봐야 할 듯
    // @PutMapping("/deactivate/{id}/")
    // public ResponseWrapper<Void> deactivateThing(@PathVariable int id) {
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