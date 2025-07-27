package com.frostetsky.cloudstorage.excepiton;

import org.springframework.http.HttpStatus;

public class ResourceNotFoundException extends BaseException {

    private static final int STATUS_CODE = HttpStatus.NOT_FOUND.value();

    public ResourceNotFoundException(String message) {
        this(message, null);
    }

    public ResourceNotFoundException(String message, Exception e) {
        super(STATUS_CODE, message, e);
    }
}
