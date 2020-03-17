package com.hanyang.belieme.demoserver;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.util.Pair;
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
    public Iterable<History> getItems() {
        Iterable<History> list = historyRepository.findAll();
        Iterator<History> iterator = list.iterator();
        while(iterator.hasNext()) {
            History history = iterator.next();
            history.addInfo(itemTypeRepository);
        }
        return list;
    }

    @GetMapping("/{id}")
    public Optional<History> getItem(@PathVariable int id) {
        Optional<History> historyOptional = historyRepository.findById(id);
        if(historyOptional.isPresent()) {
            historyOptional.get().addInfo(itemTypeRepository);
        }
        return historyOptional;
    }
    
    @GetMapping("/byRequesterId/{requesterId}")
    public Iterable<History> getItemsByRequesterId(@PathVariable int requesterId) {
        Iterable<History> list = historyRepository.findByRequesterId(requesterId);
        Iterator<History> iterator = list.iterator();
        while(iterator.hasNext()) {
            History history = iterator.next();
            history.addInfo(itemTypeRepository);
        }
        return list;
    }

    @PostMapping("/")
    public Pair<Integer, Optional<History>> createItem(@RequestBody History item) {
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
                    return Pair.of(History.HISTORY_FOR_SAME_ITEM_TYPE_EXCEPTION, Optional.empty());
                }
                currentHistoryCount++;
            }
        }
        if(currentHistoryCount >= 3) {
            return Pair.of(History.OVER_THREE_CURRENT_HISTORY_EXCEPTION, Optional.empty());
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

        History result;
        if(requestedItem != null) {
            item.setItemNum(requestedItem.getNum());
            result = historyRepository.save(item);
            result.addInfo(itemTypeRepository);
            requestedItem.setLastHistoryId(result.getId());
            itemRepository.save(requestedItem);
            return Pair.of(History.OK, Optional.of(result));
        }
        else {
            return Pair.of(History.ITEM_NOT_AVAILABLE_EXCEPTION, Optional.empty());
        }
    }

    @PutMapping("/cancel/{id}")
    public String cancelItem(@PathVariable int id) {
        Optional<History> itemBeforeUpdate = historyRepository.findById(id);
        if(itemBeforeUpdate.isPresent()) {
            History tmp = itemBeforeUpdate.get();
            if(tmp.getStatus().equals("REQUESTED")) {
                tmp.setCancelTimeStampNow();
                historyRepository.save(tmp);
                return "true";
            }
        }
        return "false";
    }

    @PutMapping("/response/{id}")
    public String responseItem(@PathVariable int id, @RequestBody History history) {
        Optional<History> itemBeforeUpdate = historyRepository.findById(id);
        if(itemBeforeUpdate.isPresent()) {
            History tmp = itemBeforeUpdate.get();
            if(tmp.getStatus().equals("REQUESTED")) {
                tmp.setResponseTimeStampNow();
                tmp.setResponseManagerId(history.getResponseManagerId());
                tmp.setResponseManagerName(history.getResponseManagerName());
                historyRepository.save(tmp);
                return "true";
            }
        }
        return "false";
    }

    @PutMapping("/return/{id}")
    public String returnItem(@PathVariable int id, @RequestBody History history) {
        Optional<History> itemBeforeUpdate = historyRepository.findById(id);
        if(itemBeforeUpdate.isPresent()) {
            History tmp = itemBeforeUpdate.get();
            if(tmp.getStatus().equals("USING") || tmp.getStatus().equals("DELAYED")) {
                tmp.setReturnTimeStampNow();
                tmp.setReturnManagerId(history.getReturnManagerId());
                tmp.setReturnManagerName(history.getReturnManagerName());
                historyRepository.save(tmp);
                return "true";
            }
        }
        return "false";
    }
}