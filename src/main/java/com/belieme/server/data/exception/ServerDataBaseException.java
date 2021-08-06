package com.belieme.server.data.exception;

public abstract class ServerDataBaseException extends Exception {
    public abstract String getMessage();
    public abstract String getPinPoint();
}