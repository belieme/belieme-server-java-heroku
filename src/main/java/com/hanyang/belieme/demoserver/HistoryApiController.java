package com.hanyang.belieme.demoserver;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

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
    private ItemTypeRepository itemTypeRepository;

    @GetMapping("/")
    public ResponseWrapper<Iterable<History>> getItems() {
        Iterable<History> list = historyRepository.findAll();
        Iterator<History> iterator = list.iterator();
        while(iterator.hasNext()) {
            History history = iterator.next();
            history.addInfo(itemTypeRepository);
        }
        return new ResponseWrapper<>(ResponseHeader.OK, list);
    }

    @GetMapping("/{id}")
    public ResponseWrapper<History> getItem(@PathVariable int id) {
        Optional<History> historyOptional = historyRepository.findById(id);
        if(historyOptional.isPresent()) {
            historyOptional.get().addInfo(itemTypeRepository);
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
            history.addInfo(itemTypeRepository);
        }
        return new ResponseWrapper<>(ResponseHeader.OK, list);
    }

    @PostMapping("/")
    public ResponseWrapper<History> createItem(@RequestBody History item) {
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
            historyList.get(i).addInfo(itemTypeRepository);
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
            items.get(i).addInfo(itemTypeRepository, historyRepository);
            if (items.get(i).getStatus().equals("USABLE")) {
                requestedItem = items.get(i);
                break;
            }
        }

        if(requestedItem != null) {
            item.setItemNum(requestedItem.getNum());
            History result = historyRepository.save(item);
            result.addInfo(itemTypeRepository);
            requestedItem.setLastHistoryId(result.getId());
            itemRepository.save(requestedItem);
            return new ResponseWrapper<>(ResponseHeader.OK, result);
        }
        else {
            return new ResponseWrapper<>(ResponseHeader.ITEM_NOT_AVAILABLE_EXCEPTION, null);
        }
    }

    @PutMapping("/cancel/{id}")
    public ResponseWrapper<History> cancelItem(@PathVariable int id) {
        Optional<History> itemBeforeUpdate = historyRepository.findById(id);
        if(itemBeforeUpdate.isPresent()) {
            History tmp = itemBeforeUpdate.get();
            if(tmp.getStatus().equals("REQUESTED")) {
                tmp.setCancelTimeStampNow();
                History result = historyRepository.save(tmp);
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
    public ResponseWrapper<History> responseItem(@PathVariable int id, @RequestBody History history) {
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
                History result = historyRepository.save(tmp);
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
    public ResponseWrapper<History> returnItem(@PathVariable int id, @RequestBody History history) {
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
                History result = historyRepository.save(tmp);
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
}