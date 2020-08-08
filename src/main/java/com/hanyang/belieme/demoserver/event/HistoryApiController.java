package com.hanyang.belieme.demoserver.event;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import com.hanyang.belieme.demoserver.thing.*;
import com.hanyang.belieme.demoserver.item.*;
import com.hanyang.belieme.demoserver.common.*;


@RestController
@RequestMapping(path="/history")
public class HistoryApiController {
    @Autowired
    private HistoryRepository historyRepository;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private ItemTypeRepository itemTypeRepository;

    @GetMapping("/")
    public ResponseWrapper<Iterable<History>> getItems() {
        Iterable<History> list = historyRepository.findAll();
        Iterator<History> iterator = list.iterator();
        while(iterator.hasNext()) {
            History history = iterator.next();
            // history.addInfo(itemTypeRepository);
            history.addInfo(itemTypeRepository, itemRepository, historyRepository);
        }
        return new ResponseWrapper<>(ResponseHeader.OK, list);
    }

    @GetMapping("/{id}")
    public ResponseWrapper<History> getItem(@PathVariable int id) {
        Optional<History> historyOptional = historyRepository.findById(id);
        if(historyOptional.isPresent()) {
            // historyOptional.get().addInfo(itemTypeRepository);
            historyOptional.get().addInfo(itemTypeRepository, itemRepository, historyRepository);
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
            // history.addInfo(itemTypeRepository);
            history.addInfo(itemTypeRepository, itemRepository, historyRepository);
        }
        return new ResponseWrapper<>(ResponseHeader.OK, list);
    }

    @PostMapping("/request/")
    public ResponseWrapper<PostMappingResponse> createRequestHistory(@RequestBody History item) {
        if(item.getRequesterId() == 0 || item.getRequesterName() == null || item.typeIdGetter() == 0) { // id가 -1으로 자동 생성 될 수 있을까? 그리고 typeId 안쓰면 어차피 뒤에서 걸리는데 필요할까?
            return new ResponseWrapper<>(ResponseHeader.LACK_OF_REQUEST_BODY_EXCEPTION, null);
        }
        item.setResponseManagerId(0);
        item.setResponseManagerName("");
        item.setReturnManagerId(0);
        item.setReturnManagerName("");
        item.setLostManagerId(0);
        item.setLostManagerName("");
        item.setRequestTimeStampNow();
        item.setResponseTimeStampZero();
        item.setReturnTimeStampZero();
        item.setCancelTimeStampZero();
        item.setLostTimeStampZero();

        List<History> historyList = historyRepository.findByTypeIdAndItemNum(item.typeIdGetter(), item.itemNumGetter()); 
        for(int i = 0; i < historyList.size(); i++) {
            // historyList.get(i).addInfo(itemTypeRepository);
            historyList.get(i).addInfo(itemTypeRepository, itemRepository, historyRepository);
            History tmp = historyList.get(i);
            if(tmp.getStatus().equals("REQUESTED") || tmp.getStatus().equals("USING") || tmp.getStatus().equals("DELAYED") || tmp.getStatus().equals("LOST")) {
                if(tmp.typeIdGetter() == item.typeIdGetter()) {
                    return new ResponseWrapper<>(ResponseHeader.HISTORY_FOR_SAME_ITEM_TYPE_EXCEPTION, null);
                }
            }
        }
        
        historyList = historyRepository.findByRequesterId(item.getRequesterId());
        int currentHistoryCount = 0;
        for(int i = 0; i < historyList.size(); i++) {
            // historyList.get(i).addInfo(itemTypeRepository);
            historyList.get(i).addInfo(itemTypeRepository, itemRepository, historyRepository);
            History tmp = historyList.get(i);
            if(tmp.getStatus().equals("REQUESTED") || tmp.getStatus().equals("USING") || tmp.getStatus().equals("DELAYED") || tmp.getStatus().equals("LOST")) {
                currentHistoryCount++;
            }
        }
        if(currentHistoryCount >= 3) {
            return new ResponseWrapper<>(ResponseHeader.OVER_THREE_CURRENT_HISTORY_EXCEPTION, null);
        } 
            
        Item requestedItem = null;
        List<Item> items = itemRepository.findByTypeId(item.typeIdGetter());
        for(int i = 0; i < items.size(); i++) {
            items.get(i).addInfo(itemTypeRepository, historyRepository);
            if (items.get(i).getStatus().equals("USABLE")) {
                requestedItem = items.get(i);
                break;
            }
        }

        if(requestedItem != null) {
            item.setItemNum(requestedItem.getNum());
            History historyResult = historyRepository.save(item);
            // historyResult.addInfo(itemTypeRepository);
            historyResult.addInfo(itemTypeRepository, itemRepository, historyRepository);
            requestedItem.setLastHistoryId(historyResult.getId());
            itemRepository.save(requestedItem);
            ArrayList<ItemType> itemTypeListResult = new ArrayList<>();
            Iterable<ItemTypeDB> itemTypeDB = itemTypeRepository.findAll();
            Iterator<ItemTypeDB> iterator = itemTypeDB.iterator();
            while(iterator.hasNext()) {
                ItemType tmpItemType = iterator.next().toItemType();
                tmpItemType.addInfo(itemTypeRepository, itemRepository, historyRepository);
                itemTypeListResult.add(tmpItemType);
            }
            return new ResponseWrapper<>(ResponseHeader.OK, new PostMappingResponse(historyResult, itemTypeListResult));
        }
        else {
            return new ResponseWrapper<>(ResponseHeader.ITEM_NOT_AVAILABLE_EXCEPTION, null);
        }
    }
    
    @PostMapping("/lost/")
    public ResponseWrapper<PostMappingResponse> createLostHistory(@RequestBody History item) {
        if(item.getLostManagerId() == 0 || item.getLostManagerName() == null || item.typeIdGetter() == 0 || item.itemNumGetter() == 0) { // id가 0으로 자동 생성 될 수 있을까? 그리고 typeId 안쓰면 어차피 뒤에서 걸리는데 필요할까?
            return new ResponseWrapper<>(ResponseHeader.LACK_OF_REQUEST_BODY_EXCEPTION, null);
        }
        item.setRequesterId(0);
        item.setRequesterName("");
        item.setResponseManagerId(0);
        item.setResponseManagerName("");
        item.setReturnManagerId(0);
        item.setReturnManagerName("");
        item.setRequestTimeStampZero();
        item.setResponseTimeStampZero();
        item.setReturnTimeStampZero();
        item.setCancelTimeStampZero();
        item.setLostTimeStampNow();

        List<History> historyList = historyRepository.findByTypeIdAndItemNum(item.typeIdGetter(), item.itemNumGetter()); 
        for(int i = 0; i < historyList.size(); i++) {
            // historyList.get(i).addInfo(itemTypeRepository);
            historyList.get(i).addInfo(itemTypeRepository, itemRepository, historyRepository);
            History tmp = historyList.get(i);
            if(tmp.getStatus().equals("REQUESTED") || tmp.getStatus().equals("USING") || tmp.getStatus().equals("DELAYED") || tmp.getStatus().equals("LOST")) {
                if(tmp.typeIdGetter() == item.typeIdGetter()) {
                    return new ResponseWrapper<>(ResponseHeader.HISTORY_FOR_SAME_ITEM_TYPE_EXCEPTION, null);
                }
            }
        }
        
        List<Item> requestedItemList = itemRepository.findByTypeIdAndNum(item.typeIdGetter(), item.itemNumGetter());
        
        if(requestedItemList.size() == 1) {
            Item requestedItem = requestedItemList.get(0);
            requestedItem.addInfo(itemTypeRepository, historyRepository);
            if(requestedItem.getStatus().equals("USABLE")) {
                History historyResult = historyRepository.save(item);
                // historyResult.addInfo(itemTypeRepository);
                historyResult.addInfo(itemTypeRepository, itemRepository, historyRepository);
                requestedItem.setLastHistoryId(historyResult.getId());
                itemRepository.save(requestedItem);
                ArrayList<ItemType> itemTypeListResult = new ArrayList<>();
                Iterable<ItemTypeDB> itemTypeDB = itemTypeRepository.findAll();
                Iterator<ItemTypeDB> iterator = itemTypeDB.iterator();
                while(iterator.hasNext()) {
                    ItemType tmpItemType = iterator.next().toItemType();
                    tmpItemType.addInfo(itemTypeRepository, itemRepository, historyRepository);
                    itemTypeListResult.add(tmpItemType);
                }
                return new ResponseWrapper<>(ResponseHeader.OK, new PostMappingResponse(historyResult, itemTypeListResult));
            }
            else {
                return new ResponseWrapper<>(ResponseHeader.ITEM_NOT_AVAILABLE_EXCEPTION, null);
            }
        }
        else if(requestedItemList.size() == 0) {
            return new ResponseWrapper<>(ResponseHeader.NOT_FOUND_EXCEPTION, null);
        }
        else {
            return new ResponseWrapper<>(ResponseHeader.WRONG_IN_DATABASE_EXCEPTION, null);
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
                    // historyTmp.addInfo(itemTypeRepository);
                    historyTmp.addInfo(itemTypeRepository, itemRepository, historyRepository);
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
                    // historyTmp.addInfo(itemTypeRepository);
                    historyTmp.addInfo(itemTypeRepository, itemRepository, historyRepository);
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
                    // historyTmp.addInfo(itemTypeRepository);
                    historyTmp.addInfo(itemTypeRepository, itemRepository, historyRepository);
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
    
    @PutMapping("/lost/{id}")
    public ResponseWrapper<Iterable<History>> lostItem(@PathVariable int id, @RequestBody History history) {
        if(history.getLostManagerId() == 0 || history.getLostManagerName() == null) {
            return new ResponseWrapper<>(ResponseHeader.LACK_OF_REQUEST_BODY_EXCEPTION, null);
        }
        Optional<History> itemBeforeUpdate = historyRepository.findById(id);
        if(itemBeforeUpdate.isPresent()) {
            History tmp = itemBeforeUpdate.get();
            if(tmp.getStatus().equals("USING") || tmp.getStatus().equals("DELAYED")) {
                tmp.setLostTimeStampNow();
                tmp.setLostManagerId(history.getLostManagerId());
                tmp.setLostManagerName(history.getLostManagerName());
                historyRepository.save(tmp);
                Iterable<History> result = historyRepository.findAll();
                Iterator<History> iterator = result.iterator();
                while (iterator.hasNext()) {
                    History historyTmp = iterator.next();
                    // historyTmp.addInfo(itemTypeRepository);
                    historyTmp.addInfo(itemTypeRepository, itemRepository, historyRepository);
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
    
    @PutMapping("/found/{id}")
    public ResponseWrapper<Iterable<History>> foundItem(@PathVariable int id, @RequestBody History history) {
        if(history.getReturnManagerId() == 0 || history.getReturnManagerName() == null) {
            return new ResponseWrapper<>(ResponseHeader.LACK_OF_REQUEST_BODY_EXCEPTION, null);
        }
        Optional<History> itemBeforeUpdate = historyRepository.findById(id);
        if(itemBeforeUpdate.isPresent()) {
            History tmp = itemBeforeUpdate.get();
            if(tmp.getStatus().equals("LOST")) {
                tmp.setReturnTimeStampNow();
                tmp.setReturnManagerId(history.getReturnManagerId());
                tmp.setReturnManagerName(history.getReturnManagerName());
                historyRepository.save(tmp);
                Iterable<History> result = historyRepository.findAll();
                Iterator<History> iterator = result.iterator();
                while (iterator.hasNext()) {
                    History historyTmp = iterator.next();
                    // historyTmp.addInfo(itemTypeRepository);
                    historyTmp.addInfo(itemTypeRepository, itemRepository, historyRepository);
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
        ArrayList<ItemType> itemTypeList;

        public PostMappingResponse(History history, ArrayList<ItemType> itemTypeList) {
            this.history = history;
            this.itemTypeList = itemTypeList;
        }

        public History getHistory() {
            return history;
        }

        public ArrayList<ItemType> getItemTypeList() {
            return itemTypeList;
        }
    }
}