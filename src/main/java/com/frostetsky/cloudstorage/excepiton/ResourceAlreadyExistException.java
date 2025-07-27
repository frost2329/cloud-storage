package com.frostetsky.cloudstorage.excepiton;

import org.springframework.http.HttpStatus;

public class ResourceAlreadyExistException extends BaseException {

    private static final int STATUS_CODE = HttpStatus.CONFLICT.value();

    public ResourceAlreadyExistException(String message) {
        this(message, null);
    }

    public ResourceAlreadyExistException(String message, Exception e) {
        super(STATUS_CODE, message, e);
    }
}
