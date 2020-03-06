package com.hanyang.belieme.demoserver;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

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
    private ItemTypeRepository itemTypeRepository;

    @GetMapping("/")
    public Iterable<History> getItems() {
        Iterable<History> list = historyRepository.findAll();
        Iterator<History> iterator = list.iterator();
        while(iterator.hasNext()) {
            History history = iterator.next();
            history.setTypeName(itemTypeRepository.findById(history.getTypeId()).get().getName());
            history.setTypeEmoji(itemTypeRepository.findById(history.getTypeId()).get().toItemType().getEmoji());
        }
        return list;
    }

    @GetMapping("/{id}")
    public Optional<History> getItem(@PathVariable int id) {
        Optional<History> historyOptional = historyRepository.findById(id);
        historyOptional.get().setTypeName(itemTypeRepository.findById(historyOptional.get().getTypeId()).get().getName());
        historyOptional.get().setTypeEmoji(itemTypeRepository.findById(historyOptional.get().getTypeId()).get().toItemType().getEmoji());
        return historyOptional;
    }
    
    @GetMapping("/byRequesterId/{requesterId}")
    public Iterable<History> getItemsByRequesterId(@PathVariable int requesterId) {
        Iterable<History> list = historyRepository.findByRequesterId(requesterId);
        Iterator<History> iterator = list.iterator();
        while(iterator.hasNext()) {
            History history = iterator.next();
            history.setTypeName(itemTypeRepository.findById(history.getTypeId()).get().getName());
            history.setTypeEmoji(itemTypeRepository.findById(history.getTypeId()).get().toItemType().getEmoji());
        }
        return list;
    }

    @PostMapping("/")
    public History createItem(@RequestBody History item) {
        item.setManagerId(0);
        item.setManagerName("");
        item.setRequestTimeStampNow();
        item.setResponseTimeStampZero();
        item.setReturnedTimeStampZero();
        item.setCanceledTimeStampZero();
        item.setTypeName(itemTypeRepository.findById(item.getTypeId()).get().getName());
        item.setTypeEmoji(itemTypeRepository.findById(item.getTypeId()).get().toItemType().getEmoji());
        History result = null;

        Item requestedItem = null;
        List<Item> items = itemRepository.findByTypeId(item.getTypeId());
        for(int i = 0; i < items.size(); i++) {
            addInfo(items.get(i));
            if (items.get(i).getStatus().equals("USABLE")) {
                requestedItem = items.get(i);
                break;
            }
        }

        if(requestedItem != null) {
            List<History> histories = historyRepository.findByTypeIdAndItemNum(item.getTypeId(), requestedItem.getNum());
            for(int i = 0; i < histories.size(); i++) {
                switch (histories.get(i).getStatus()) {
                    case "REQUESTED" :
                    case "USING" :
                    case "DELAYED" :
                        return null;
                }
            }
            item.setItemNum(requestedItem.getNum());
            result = historyRepository.save(item);
            requestedItem.setLastHistoryId(result.getId());
            itemRepository.save(requestedItem);
        }
        return result;
    }

    @PutMapping("/cancel/{id}")
    public String cancelItem(@PathVariable int id) {
        Optional<History> itemBeforeUpdate = historyRepository.findById(id);
        if(itemBeforeUpdate.isPresent()) {
            History tmp = itemBeforeUpdate.get();
            if(tmp.getStatus().equals("REQUESTED")) {
                tmp.setCanceledTimeStampNow();
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
                tmp.setManagerId(history.getManagerId());
                tmp.setManagerName(history.getManagerName());
                historyRepository.save(tmp);
                return "true";
            }
        }
        return "false";
    }

    @PutMapping("/return/{id}")
    public String returnItem(@PathVariable int id) {
        Optional<History> itemBeforeUpdate = historyRepository.findById(id);
        if(itemBeforeUpdate.isPresent()) {
            History tmp = itemBeforeUpdate.get();
            if(tmp.getStatus().equals("USING") || tmp.getStatus().equals("DELAYED")) {
                tmp.setReturnedTimeStampNow();
                historyRepository.save(tmp);
                return "true";
            }
        }
        return "false";
    }

    private void addInfo(Item item) {
        Optional<History> lastHistory = historyRepository.findById(item.getLastHistoryId());
        if(lastHistory.isPresent()) {
            String lastHistoryStatus = lastHistory.get().getStatus();
            if(lastHistoryStatus.equals("EXPIRED")||lastHistoryStatus.equals("RETURNED")) {
                item.usableStatus();
            }
            else {
                item.unusableStatus();
            }
        }
        else {
            item.usableStatus();
        }
        Optional<ItemTypeDB> itemType = itemTypeRepository.findById(item.getTypeId());

        if(itemType.isPresent()) {
            item.setTypeName(itemType.get().getName());
            item.setTypeEmoji(itemType.get().toItemType().getEmoji());
        }
        else {
            item.setTypeName("");
            item.setTypeEmoji("");
        }
    }
}