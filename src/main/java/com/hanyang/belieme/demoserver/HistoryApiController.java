package com.hanyang.belieme.demoserver;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

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
        return historyRepository.findAll();
    }

    @GetMapping("/{id}")
    public Optional<History> getItem(@PathVariable int id) {
        return historyRepository.findById(id);
    }
    
    @GetMapping("/byRequesterId/{requesterId}")
    public Iterable<History> getItemsByRequesterId(@PathVariable int requesterId) {
        return historyRepository.findByRequesterId(requesterId);
    }

    @PostMapping("/")
    public History createItem(@RequestBody History item) {
        item.setManagerId(0);
        item.setManagerName("");
        item.setRequestTimeStamp(System.currentTimeMillis() / 1000);
        item.setResponseTimeStamp(0);
        item.setReturnedTimeStamp(0);
        item.setStatus("REQUESTED");
        History result = null;

        Item requestedItem = null;
        List<Item> items = itemRepository.findByTypeId(item.getTypeId());
        for(int i = 0; i < items.size(); i++) {
            if (items.get(i).getStatus().equals("USABLE")) {
                requestedItem = items.get(i);
                break;
            }
        }

        ItemType requestedItemType = itemTypeRepository.findById(item.getTypeId()).get();
        if(requestedItem != null && requestedItemType != null) {
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
            requestedItem.setStatus("UNUSABLE");
            itemRepository.save(requestedItem);
            requestedItemType.setCount(requestedItemType.getCount() - 1);
            itemTypeRepository.save(requestedItemType);
        }
        return result;
    }

    @PutMapping("/")
    public String updateItem(@RequestBody History item){
        Optional<History> itemBeforeUpdate = historyRepository.findById(item.getId());
        if(!itemBeforeUpdate.isEmpty()) {
            History tmp = itemBeforeUpdate.get();
            tmp.setRequesterName(item.getRequesterName());
            if(tmp.getStatus().equals("REQUESTED")) {
                if (item.getStatus().equals("USING")) {
                    tmp.setManagerId(item.getManagerId());
                    tmp.setManagerName(item.getManagerName());
                    tmp.setResponseTimeStamp(System.currentTimeMillis() / 1000);
                    tmp.setStatus(item.getStatus());
                } else if (item.getStatus().equals("EXPIRED")) {
                    tmp.setResponseTimeStamp(System.currentTimeMillis() / 1000);
                    tmp.setStatus(item.getStatus());

                    Item requestedItem = getRequestedItem(tmp);
                    ItemType requestedItemType = itemTypeRepository.findById(tmp.getTypeId()).get();

                    if (requestedItem != null && requestedItemType != null) {
                        requestedItem.setStatus("USABLE");
                        itemRepository.save(requestedItem);
                        requestedItemType.setCount(requestedItemType.getCount() + 1);
                        itemTypeRepository.save(requestedItemType);
                    }
                    else {
                        return "false";
                    }
                }
            }
            else if(tmp.getStatus().equals("USING")) {
                if(item.getStatus().equals("DELAYED")) {
                    tmp.setStatus(item.getStatus());
                }
                else if(item.getStatus().equals("RETURNED")) {
                    tmp.setReturnedTimeStamp(System.currentTimeMillis() / 1000);
                    tmp.setStatus(item.getStatus());

                    Item requestedItem = getRequestedItem(tmp);
                    ItemType requestedItemType = itemTypeRepository.findById(tmp.getTypeId()).get();

                    if (requestedItem != null && requestedItemType != null) {
                        requestedItem.setStatus("USABLE");
                        itemRepository.save(requestedItem);
                        requestedItemType.setCount(requestedItemType.getCount() + 1);
                        itemTypeRepository.save(requestedItemType);
                    }
                    else {
                        return "false";
                    }
                }
            }
            else if(tmp.getStatus().equals("DELAYED")) {
                if(item.getStatus().equals("RETURNED")) {
                    tmp.setReturnedTimeStamp(System.currentTimeMillis() / 1000);
                    tmp.setStatus(item.getStatus());

                    Item requestedItem = getRequestedItem(tmp);
                    ItemType requestedItemType = itemTypeRepository.findById(item.getTypeId()).get();

                    if (requestedItem != null && requestedItemType != null) {
                        requestedItem.setStatus("USABLE");
                        itemRepository.save(requestedItem);
                        requestedItemType.setCount(requestedItemType.getCount() + 1);
                        itemTypeRepository.save(requestedItemType);
                    }
                    else {
                        return "false";
                    }
                }
            }
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
}