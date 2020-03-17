package com.hanyang.belieme.demoserver;

public class ResponseWrapper<T> {
    private ResponseHeader header;
    private T body;

    public ResponseWrapper(ResponseHeader header, T body) {
        this.header = header;
        this.body = body;
    }

    public ResponseHeader getHeader() {
        return header;
    }

    public T getBody() {
        return body;
    }
}
