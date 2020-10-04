package com.hanyang.belieme.demoserver.exception;

import org.springframework.http.HttpStatus;

public class WrongInDataBaseException extends InternalServerException {
    public WrongInDataBaseException(String message) {
        super();
        setHttpStatus(HttpStatus.INTERNAL_SERVER_ERROR);
        setCode(ErrorCodes.WRONG_IN_DATA_BASE_EXCEPTION);
        setName("Wrong In Data Base");
        setDesc("There is something wrong in database.");
        setMessage(message);
    }
}