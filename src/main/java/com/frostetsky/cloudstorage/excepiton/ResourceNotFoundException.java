package com.frostetsky.cloudstorage.excepiton;

import org.springframework.http.HttpStatus;

public class ResourceNotFoundException extends BaseException {

    public ResourceNotFoundException(String message) {
        this(message, null);
    }

    public ResourceNotFoundException(String message, Exception e) {
        super(HttpStatus.NOT_FOUND.value(), message, e);
    }
}
