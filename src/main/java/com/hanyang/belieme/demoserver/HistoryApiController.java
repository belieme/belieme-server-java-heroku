package com.hanyang.belieme.demoserver;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping(path="/history")
public class HistoryApiController {
    @Autowired
    private HistoryRepository historyRepository;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private ThingsRepository thingsRepository;

    @GetMapping("/")
    public ResponseWrapper<Iterable<History>> getItems() {
        Iterable<History> list = historyRepository.findAll();
        Iterator<History> iterator = list.iterator();
        while(iterator.hasNext()) {
            History history = iterator.next();
            history.addInfo(thingsRepository);
        }
        return new ResponseWrapper<>(ResponseHeader.OK, list);
    }

    @GetMapping("/{id}")
    public ResponseWrapper<History> getItem(@PathVariable int id) {
        Optional<History> historyOptional = historyRepository.findById(id);
        if(historyOptional.isPresent()) {
            historyOptional.get().addInfo(thingsRepository);
            return new ResponseWrapper<>(ResponseHeader.OK, historyOptional.get());
        }
        return new ResponseWrapper<>(ResponseHeader.NOT_FOUND_EXCEPTION, null);
    }
    
    @GetMapping("/byRequesterId/{requesterId}")
    public ResponseWrapper<Iterable<History>> getItemsByRequesterId(@PathVariable int requesterId) {
        Iterable<History> list = historyRepository.findByRequesterId(requesterId);
        Iterator<History> iterator = list.iterator();
        while(iterator.hasNext()) {
            History history = iterator.next();
            history.addInfo(thingsRepository);
        }
        return new ResponseWrapper<>(ResponseHeader.OK, list);
    }

    @PostMapping("/")
    public ResponseWrapper<PostMappingResponse> createItem(@RequestBody History item) {
        if(item.getRequesterId() == 0 || item.getRequesterName() == null || item.getTypeId() == 0) { // id가 0으로 자동 생성 될 수 있을까? 그리고 typeId 안쓰면 어차피 뒤에서 걸리는데 필요할까?
            return new ResponseWrapper<>(ResponseHeader.LACK_OF_REQUEST_BODY_EXCEPTION, null);
        }
        item.setResponseManagerId(0);
        item.setResponseManagerName("");
        item.setReturnManagerId(0);
        item.setReturnManagerName("");
        item.setRequestTimeStampNow();
        item.setResponseTimeStampZero();
        item.setReturnTimeStampZero();
        item.setCancelTimeStampZero();

        List<History> historyList = historyRepository.findByRequesterId(item.getRequesterId());
        int currentHistoryCount = 0;
        for(int i = 0; i < historyList.size(); i++) {
            historyList.get(i).addInfo(thingsRepository);
            History tmp = historyList.get(i);
            if(tmp.getStatus().equals("REQUESTED") || tmp.getStatus().equals("USING") || tmp.getStatus().equals("DELAYED")) {
                if(tmp.getTypeId() == item.getTypeId()) {
                    return new ResponseWrapper<>(ResponseHeader.HISTORY_FOR_SAME_ITEM_TYPE_EXCEPTION, null);
                }
                currentHistoryCount++;
            }
        }
        if(currentHistoryCount >= 3) {
            return new ResponseWrapper<>(ResponseHeader.OVER_THREE_CURRENT_HISTORY_EXCEPTION, null);
        }

        Item requestedItem = null;
        List<Item> items = itemRepository.findByTypeId(item.getTypeId());
        for(int i = 0; i < items.size(); i++) {
            items.get(i).addInfo(thingsRepository, historyRepository);
            if (items.get(i).getStatus().equals("USABLE")) {
                requestedItem = items.get(i);
                break;
            }
        }

        if(requestedItem != null) {
            item.setItemNum(requestedItem.getNum());
            History historyResult = historyRepository.save(item);
            historyResult.addInfo(thingsRepository);
            requestedItem.setLastHistoryId(historyResult.getId());
            itemRepository.save(requestedItem);
            ArrayList<Things> thingsListResult = new ArrayList<>();
            Iterable<ThingsDB> thingsDB = thingsRepository.findAll();
            Iterator<ThingsDB> iterator = thingsDB.iterator();
            while(iterator.hasNext()) {
                Things tmpThings = iterator.next().toThings();
                tmpThings.addInfo(thingsRepository, itemRepository, historyRepository);
                thingsListResult.add(tmpThings);
            }
            return new ResponseWrapper<>(ResponseHeader.OK, new PostMappingResponse(historyResult, thingsListResult));
        }
        else {
            return new ResponseWrapper<>(ResponseHeader.ITEM_NOT_AVAILABLE_EXCEPTION, null);
        }
    }

    @PutMapping("/cancel/{id}")
    public ResponseWrapper<Iterable<History>> cancelItem(@PathVariable int id) {
        Optional<History> itemBeforeUpdate = historyRepository.findById(id);
        if(itemBeforeUpdate.isPresent()) {
            History tmp = itemBeforeUpdate.get();
            if(tmp.getStatus().equals("REQUESTED")) {
                tmp.setCancelTimeStampNow();
                historyRepository.save(tmp);
                Iterable<History> result = historyRepository.findByRequesterId(tmp.getRequesterId());
                Iterator<History> iterator = result.iterator();
                while (iterator.hasNext()) {
                    History historyTmp = iterator.next();
                    historyTmp.addInfo(thingsRepository);
                }
                return new ResponseWrapper<>(ResponseHeader.OK, result); //설마 save method에서 null을 return하겠어
            }
            else {
                return new ResponseWrapper<>(ResponseHeader.WRONG_HISTORY_STATUS_EXCEPTION, null);
            }
        }
        else {
            return new ResponseWrapper<>(ResponseHeader.NOT_FOUND_EXCEPTION, null);
        }
    }

    @PutMapping("/response/{id}")
    public ResponseWrapper<Iterable<History>> responseItem(@PathVariable int id, @RequestBody History history) {
        if(history.getResponseManagerId() == 0 || history.getResponseManagerName() == null) {
            return new ResponseWrapper<>(ResponseHeader.LACK_OF_REQUEST_BODY_EXCEPTION, null);
        }
        Optional<History> itemBeforeUpdate = historyRepository.findById(id);
        if(itemBeforeUpdate.isPresent()) {
            History tmp = itemBeforeUpdate.get();
            if(tmp.getStatus().equals("REQUESTED")) {
                tmp.setResponseTimeStampNow();
                tmp.setResponseManagerId(history.getResponseManagerId());
                tmp.setResponseManagerName(history.getResponseManagerName());
                historyRepository.save(tmp);
                Iterable<History> result = historyRepository.findAll();
                Iterator<History> iterator = result.iterator();
                while (iterator.hasNext()) {
                    History historyTmp = iterator.next();
                    historyTmp.addInfo(thingsRepository);
                }
                return new ResponseWrapper<>(ResponseHeader.OK, result); //설마 save method에서 null을 return하겠어
            }
            else {
                return new ResponseWrapper<>(ResponseHeader.WRONG_HISTORY_STATUS_EXCEPTION, null);
            }
        }
        else {
            return new ResponseWrapper<>(ResponseHeader.NOT_FOUND_EXCEPTION, null);
        }
    }

    @PutMapping("/return/{id}")
    public ResponseWrapper<Iterable<History>> returnItem(@PathVariable int id, @RequestBody History history) {
        if(history.getReturnManagerId() == 0 || history.getReturnManagerName() == null) {
            return new ResponseWrapper<>(ResponseHeader.LACK_OF_REQUEST_BODY_EXCEPTION, null);
        }
        Optional<History> itemBeforeUpdate = historyRepository.findById(id);
        if(itemBeforeUpdate.isPresent()) {
            History tmp = itemBeforeUpdate.get();
            if(tmp.getStatus().equals("USING") || tmp.getStatus().equals("DELAYED")) {
                tmp.setReturnTimeStampNow();
                tmp.setReturnManagerId(history.getReturnManagerId());
                tmp.setReturnManagerName(history.getReturnManagerName());
                historyRepository.save(tmp);
                Iterable<History> result = historyRepository.findAll();
                Iterator<History> iterator = result.iterator();
                while (iterator.hasNext()) {
                    History historyTmp = iterator.next();
                    historyTmp.addInfo(thingsRepository);
                }
                return new ResponseWrapper<>(ResponseHeader.OK, result); //설마 save method에서 null을 return하겠어
            }
            else {
                return new ResponseWrapper<>(ResponseHeader.WRONG_HISTORY_STATUS_EXCEPTION, null);
            }
        }
        else {
            return new ResponseWrapper<>(ResponseHeader.NOT_FOUND_EXCEPTION, null);
        }
    }

    public class PostMappingResponse {
        History history;
        ArrayList<Things> thingsList;

        public PostMappingResponse(History history, ArrayList<Things> thingsList) {
            this.history = history;
            this.thingsList = thingsList;
        }

        public History getHistory() {
            return history;
        }

        public ArrayList<Things> getThingsList() {
            return thingsList;
        }
    }
}