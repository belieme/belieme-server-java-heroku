package com.hanyang.belieme.demoserver.item;

import javax.persistence.*;

import java.util.Optional;

import com.hanyang.belieme.demoserver.thing.*;
import com.hanyang.belieme.demoserver.event.*;


@Entity
public class Item {
    @Id @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;
    private int typeId;
    private int num;
    
    private int lastHistoryId;

    @Transient
    private String status;

    @Transient
    private ItemTypeNestedToItem itemType;
    
    @Transient
    private HistoryNestedToItem lastHistory;

    public Item() {
    }

    public Item(int typeId, int num) {
        this.typeId = typeId;
        this.num = num;
        this.lastHistoryId = -1;
    }
    
    public int getId() {
        return id;
    }

    public int getNum() {
        return num;
    }

    public String getStatus() {
        return status;
    }

    public ItemTypeNestedToItem getItemType() {
        return itemType;
    }
    
    public HistoryNestedToItem getLastHistory() {
        return lastHistory;
    }

    public void setTypeId(int typeId) {
        this.typeId = typeId;
    }

    public void setNum(int num) {
        this.num = num;
    }

    public void setLastHistoryId(int lastHistoryId) {
        this.lastHistoryId = lastHistoryId;
    }

    public void setItemType(ItemType itemType) {
        if(itemType == null) {
            this.itemType = null;
        } else {
            this.itemType = itemType.toItemTypeNestedToItem();
        }
    }
    
    public void setLastHistory(History history) {
        if(history == null) {
            this.lastHistory = null;
        } else {
            this.lastHistory = history.toHistoryNestedToItem();
        }
    }
    
    public int typeIdGetter() {
        return typeId;
    }
    
    public int lastHistoryIdGetter() {
        return lastHistoryId;
    }

    //대상이 저장된 정보 뿐만 아니라 다른 table로부터 derived 된 정보까 추가 하는 메소드(ex status ... )
    public void addInfo(ItemTypeRepository itemTypeRepository, HistoryRepository historyRepository) {
        Optional<History> lastHistory = historyRepository.findById(lastHistoryId);
        if(lastHistory.isPresent()) {
            String lastHistoryStatus = lastHistory.get().getStatus();
            if(lastHistoryStatus.equals("EXPIRED")||lastHistoryStatus.equals("RETURNED")||lastHistoryStatus.equals("FOUND")||lastHistoryStatus.equals("FOUNDANDRETURNED")) {
                status = "USABLE";
            }
            else if (lastHistoryStatus.equals("LOST")){
                status = "INACTIVATE";
            } else {
                status = "UNUSABLE";
            }
            setLastHistory(lastHistory.get());
        }
        else {
            status = "USABLE";
            setLastHistory(null);
        }

        Optional<ItemTypeDB> itemTypeDB = itemTypeRepository.findById(typeIdGetter());
        ItemType itemType;
        if(itemTypeDB.isPresent()) {
            itemType = itemTypeDB.get().toItemType();
        } else {
            itemType = null;
        }
        setItemType(itemType);
    }
    
    public ItemNestedToItemType toItemNestedToItemType() {
        ItemNestedToItemType output = new ItemNestedToItemType();
        output.setNum(num);
        output.setLastHistory(lastHistory);
        output.setStatus(status);
        return output;
    }
    
    public ItemNestedToHistory toItemNestedToHistory() {
        ItemNestedToHistory output = new ItemNestedToHistory();
        output.setId(id);
        output.setNum(num);
        output.setItemType(itemType);
        
        return output;
    }
}
