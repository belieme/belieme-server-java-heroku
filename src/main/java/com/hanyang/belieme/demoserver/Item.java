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


    @Transient
    private History lastHistory;

    private boolean inactive;

    @Transient
    private String status;

    @Transient
    private ItemType itemType;
    // @Transient
    // private String typeName;

    // @Transient
    // private String typeEmoji;

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

    public int getTypeId() {
        return typeId;
    }

    public int getNum() {
        return num;
    }

    public int getLastHistoryId() {
        return lastHistoryId;
    }

    public String getStatus() {
        return status;
    }

    public ItemType getItemType() {
        return itemType;
    }
    
    // public String getTypeName() {
    //     return typeName;
    // }

    // public String getTypeEmoji() {
    //     return typeEmoji;
    // }

    public int getRequesterId() {
        return (lastHistory != null) ? lastHistory.getRequesterId() : -1;
    }

    public String getRequesterName() {
        return (lastHistory != null) ? lastHistory.getRequesterName() : null;
    }

    public int getResponseManagerId() {
        return (lastHistory != null) ? lastHistory.getResponseManagerId() : -1;
    }

    public String getResponseManagerName() {
        return (lastHistory != null) ? lastHistory.getResponseManagerName() : null;
    }

    public int getReturnManagerId() {
        return (lastHistory != null) ? lastHistory.getReturnManagerId() : -1;
    }

    public String getReturnManagerName() {
        return (lastHistory != null) ? lastHistory.getReturnManagerName() : null;
    }

    public long getRequestTimeStamp() {
        return (lastHistory != null) ? lastHistory.getRequestTimeStamp() : 0;
    }

    public long getResponseTimeStamp() {
        return (lastHistory != null) ? lastHistory.getResponseTimeStamp() : 0;
    }

    public long getReturnTimeStamp() {
        return (lastHistory != null) ? lastHistory.getReturnTimeStamp() : 0;
    }

    public long getCancelTimeStamp() {
        return (lastHistory != null) ? lastHistory.getCancelTimeStamp() : 0;
    }

    public String getLastHistoryStatus() {
        return (lastHistory != null) ? lastHistory.getStatus() : "ERROR";
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

    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }

    public void setTypeEmoji(String typeEmoji) {
        this.typeEmoji = typeEmoji;
    }

    public void deactivate() {
        inactive = true;
    }

    public void activate() {
        inactive = false;
    }

    //대상이 저장된 정보 뿐만 아니라 다른 table로부터 derived 된 정보까 추가 하는 메소드(ex status ... )
    public void addInfo(ItemTypeRepository itemTypeRepository, HistoryRepository historyRepository) {
        Optional<History> lastHistory = historyRepository.findById(getLastHistoryId());
        if(lastHistory.isPresent()) {
            String lastHistoryStatus = lastHistory.get().getStatus();
            if(lastHistoryStatus.equals("EXPIRED")||lastHistoryStatus.equals("RETURNED")) {
                status = "USABLE";
            }
            else {
                status = "UNUSABLE";
            }
            this.lastHistory = lastHistory.get();
        }
        else {
            status = "USABLE";
        }
        if(isInactive())
        {
            status = "INACTIVE";
        }

        Optional<ItemTypeDB> itemType = itemTypeRepository.findById(getTypeId());

        if(itemType.isPresent()) {
            setTypeName(itemType.get().getName());
            setTypeEmoji(itemType.get().toItemType().getEmoji());
        }
        else {
            setTypeName("");
            setTypeEmoji("");
        }
    }
}
