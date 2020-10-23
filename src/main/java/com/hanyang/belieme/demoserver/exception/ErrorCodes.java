package com.hanyang.belieme.demoserver.exception;

// TODO 필요한 exceptionCde로 바꾸기

public class ErrorCodes {
    public static final int NOT_FOUND_EXCEPTION = 1;
    
    public static final int WRONG_IN_DATA_BASE_EXCEPTION = 2;
    
    public static final int BAD_REQUEST_BODY_EXCEPTION = 3;
    public static final int BAD_REQUEST_HEADER_EXCEPTION = 4; // "Request header lacks information for request." // BAD_REQUEST로 합치자
    
    public static final int OVER_THREE_CURRENT_EVENT_EXCEPTION = 5; // "This requester have requested more than three items." // METHOD_NOT_ALLOWED로 합치자
    public static final int EVENT_FOR_SAME_THING_EXCEPTION = 6; // "This requester have requested item with same thing."
    public static final int ITEM_NOT_AVAILABLE_EXCEPTION = 7; // "There is no available item."
    public static final int WRONG_EVENT_STATUS_EXCEPTION = 8; // "Status of event is wrong."
    
    public static final int EXPIRED_USER_TOKEN_EXCEPTION = 9; // "This unversity is not registered." // Unauthorized로 바꾸기
    
    public static final int USER_PERMISSION_DENIED_EXCEPTION = 10; // "This user doesn't have permission on the endpoint." // Forbidden으로 바꾸기
    
    public static final int BAD_CONNECTION_EXCEPTION = 11; // "There is error on connection." // GateWayTimeOut으로 바꾸기
    
    
    /*
    public static final ResponseHeader DUPLICATE_CODE_EXCEPTION = new ResponseHeader(9, "There is same code in the table"); //TODO WrongInDataBaseException으로 합치기
    
    
    public static final ResponseHeader UNREGISTERED_UNIVERSITY_EXCEPTION = new ResponseHeader(14, "This unversity is not registered."); // TODO NOT_FOUND_Exception으로 합치기
    public static final ResponseHeader WRONG_PERMISSION_EXCEPTION = new ResponseHeader(17, "Given permission is wrong."); // TODO BAD_REQUEST로 합치기 아님 METHOD Not Allowed?
    */
}