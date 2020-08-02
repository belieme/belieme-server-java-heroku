package com.hanyang.belieme.demoserver;

import org.springframework.beans.factory.annotation.Autowired;

import javax.persistence.*;

import java.util.Optional;

@Entity
public class Item {

    @Id @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;
    private int typeId;
    private int num;
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
        this.typeId = typeId;
        this.num = num;
        this.lastHistoryId = -1;
        this.inactive = false;
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

    public boolean isInactive() {
        return inactive;
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

        Optional<ItemTypeDB> itemType = itemTypeRepository.findById(typeId);

        setItemType(itemType.get());
    }
}
