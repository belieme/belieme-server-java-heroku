package com.hanyang.belieme.demoserver.item;

import javax.persistence.*;

import java.util.Optional;

import com.hanyang.belieme.demoserver.thing.*;
import com.hanyang.belieme.demoserver.event.*;


@Entity
public class Item {

    @EmbeddedId
    private ItemPK pk;
    
    private int lastHistoryId;
    private boolean inactive;

    @Transient
    private String status;

    @Transient
    private ItemTypeNestedToItem itemType;
    
    @Transient
    private HistoryNestedToItem lastHistory;

    public Item() {
    }

    public Item(int typeId, int num) {
        pk = new ItemPK(typeId,num);
        this.lastHistoryId = -1;
        this.inactive = false;
    }

    public int getNum() {
        return pk.getNum();
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

    public boolean isInactive() {
        return inactive;
    }

    public void setTypeId(int typeId) {
        this.pk.setTypeId(typeId);
    }

    public void setNum(int num) {
        this.pk.setNum(num);
    }

    public void setLastHistoryId(int lastHistoryId) {
        this.lastHistoryId = lastHistoryId;
    }

    public void setItemType(ItemTypeDB itemType) {
        if(itemType == null) {
            this.itemType = null;
        } else {
            this.itemType = new ItemTypeNestedToItem(itemType);
        }
    }
    
    public void setLastHistory(History history) {
        if(history == null) {
            this.lastHistory = null;
        } else {
            this.lastHistory = new HistoryNestedToItem(history);
        }
    }

    public void deactivate() {
        inactive = true;
    }

    public void activate() {
        inactive = false;
    }
    
    public int typeIdGetter() {
        return pk.getTypeId();
    }
    
    public int lastHistoryIdGetter() {
        return lastHistoryId;
    }

    //대상이 저장된 정보 뿐만 아니라 다른 table로부터 derived 된 정보까 추가 하는 메소드(ex status ... )
    public void addInfo(ItemTypeRepository itemTypeRepository, HistoryRepository historyRepository) {
        Optional<History> lastHistory = historyRepository.findById(lastHistoryId);
        if(lastHistory.isPresent()) {
            String lastHistoryStatus = lastHistory.get().getStatus();
            if(lastHistoryStatus.equals("EXPIRED")||lastHistoryStatus.equals("RETURNED")) {
                status = "USABLE";
            }
            else {
                status = "UNUSABLE";
            }
            setLastHistory(lastHistory.get());
        }
        else {
            status = "USABLE";
            setLastHistory(null);
        }
        if(isInactive())
        {
            status = "INACTIVE";
        }

        Optional<ItemTypeDB> itemType = itemTypeRepository.findById(typeIdGetter());

        setItemType(itemType.get());
    }
}
