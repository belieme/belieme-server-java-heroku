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

    @PutMapping("/")
    public String updateItem(@RequestBody History item){
        Optional<History> itemBeforeUpdate = historyRepository.findById(item.getId());
        if(itemBeforeUpdate.isPresent()) {
            History tmp = itemBeforeUpdate.get();
            tmp.setRequesterName(item.getRequesterName());
            if(tmp.getStatus().equals("REQUESTED")) {
                if (item.getStatus().equals("USING")) {
                    tmp.setManagerId(item.getManagerId());
                    tmp.setManagerName(item.getManagerName());
                    tmp.setResponseTimeStampNow();
                } else if (item.getStatus().equals("EXPIRED")) {
                    tmp.setResponseTimeStampNow();

                    Item requestedItem = getRequestedItem(tmp);

                    if (requestedItem != null) {
                        itemRepository.save(requestedItem);
                    }
                    else {
                        return "false";
                    }
                }
            }
            else if(tmp.getStatus().equals("USING")) {
                if(item.getStatus().equals("DELAYED")) {
                }
                else if(item.getStatus().equals("RETURNED")) {
                    tmp.setReturnedTimeStampNow();

                    Item requestedItem = getRequestedItem(tmp);

                    if (requestedItem != null) {
                        itemRepository.save(requestedItem);
                    }
                    else {
                        return "false";
                    }
                }
            }
            else if(tmp.getStatus().equals("DELAYED")) {
                if(item.getStatus().equals("RETURNED")) {
                    tmp.setReturnedTimeStampNow();

                    Item requestedItem = getRequestedItem(tmp);

                    if (requestedItem != null) {
                        itemRepository.save(requestedItem);
                    }
                    else {
                        return "false";
                    }
                }
            }
            tmp.setTypeName("");
            historyRepository.save(tmp);
            return "true";
        }
        return "false";
    }

    @DeleteMapping("/{id}")
    public void deleteItem(@PathVariable int id) {
        historyRepository.deleteById(id);
    }

    private Item getRequestedItem(History item) {
        List<Item> items = itemRepository.findByTypeId(item.getTypeId());
        Item requestedItem = null;

        for (int i = 0; i < items.size(); i++) {
            if (items.get(i).getNum() == item.getItemNum()) {
                requestedItem = items.get(i);
                break;
            }
        }
        return requestedItem;
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