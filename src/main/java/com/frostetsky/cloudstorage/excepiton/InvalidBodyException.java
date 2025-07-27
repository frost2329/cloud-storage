package com.frostetsky.cloudstorage.excepiton;

import org.springframework.http.HttpStatus;

public class InvalidBodyException extends BaseException {

    private static final int STATUS_CODE = HttpStatus.BAD_REQUEST.value();

    public InvalidBodyException(String message) {
        this(message, null);
    }

    public InvalidBodyException(String message, Exception e) {
        super(STATUS_CODE, message, e);
    }
}
