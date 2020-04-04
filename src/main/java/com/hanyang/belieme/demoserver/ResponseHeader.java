package com.hanyang.belieme.demoserver;

public class ResponseHeader {
    public static final ResponseHeader OK = new ResponseHeader(0, "OK.");
    public static final ResponseHeader NOT_FOUND_EXCEPTION = new ResponseHeader(1, "Can not found it.");
    public static final ResponseHeader LACK_OF_REQUEST_BODY_EXCEPTION = new ResponseHeader(2, "Request body lacks information for request.");
    public static final ResponseHeader OVER_THREE_CURRENT_HISTORY_EXCEPTION = new ResponseHeader(3, "This requester have requested more than three items.");
    public static final ResponseHeader HISTORY_FOR_SAME_ITEM_TYPE_EXCEPTION = new ResponseHeader(4, "This requester have requested item with same item type.");
    public static final ResponseHeader ITEM_NOT_AVAILABLE_EXCEPTION = new ResponseHeader(5, "There is no available item.");
    public static final ResponseHeader WRONG_HISTORY_STATUS_EXCEPTION = new ResponseHeader(6, "Status of history is wrong.");
    public static final ResponseHeader WRONG_ADMIN_PERMISSION_EXCEPTION = new ResponseHeader(7, "Permission of admin is wrong.");

    private int code;
    private String message;

    public ResponseHeader(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
