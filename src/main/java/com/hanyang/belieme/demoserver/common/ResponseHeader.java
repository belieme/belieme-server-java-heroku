package com.hanyang.belieme.demoserver.common;

public class ResponseHeader {
    public static final ResponseHeader OK = new ResponseHeader(0, "OK.");
    public static final ResponseHeader NOT_FOUND_EXCEPTION = new ResponseHeader(1, "Can not found it.");
    public static final ResponseHeader LACK_OF_REQUEST_BODY_EXCEPTION = new ResponseHeader(2, "Request body lacks information for request.");
    public static final ResponseHeader OVER_THREE_CURRENT_EVENT_EXCEPTION = new ResponseHeader(3, "This requester have requested more than three items.");
    public static final ResponseHeader EVENT_FOR_SAME_THING_EXCEPTION = new ResponseHeader(4, "This requester have requested item with same thing.");
    public static final ResponseHeader ITEM_NOT_AVAILABLE_EXCEPTION = new ResponseHeader(5, "There is no available item.");
    public static final ResponseHeader WRONG_EVENT_STATUS_EXCEPTION = new ResponseHeader(6, "Status of event is wrong.");
    public static final ResponseHeader WRONG_ADMIN_PERMISSION_EXCEPTION = new ResponseHeader(7, "Permission of admin is wrong.");
    public static final ResponseHeader WRONG_IN_DATABASE_EXCEPTION = new ResponseHeader(8, "There is something wrong in database.");
    public static final ResponseHeader DUPLICATE_CODE_EXCEPTION = new ResponseHeader(9, "There is same code in the table");

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
