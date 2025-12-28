package com.frostetsky.cloudstorage.excepiton;

import lombok.Getter;

@Getter
public class BaseException extends RuntimeException {

    private final int statusCode;
    private final String massage;

    public BaseException(int statusCode, String message, Exception e) {
        super(message, e);
        this.massage = message;
        this.statusCode = statusCode;
    }
}
