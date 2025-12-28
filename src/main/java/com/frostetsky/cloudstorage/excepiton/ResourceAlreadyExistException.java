package com.frostetsky.cloudstorage.excepiton;

import org.springframework.http.HttpStatus;

public class ResourceAlreadyExistException extends BaseException {

    public ResourceAlreadyExistException(String message) {
        this(message, null);
    }

    public ResourceAlreadyExistException(String message, Exception e) {
        super(HttpStatus.CONFLICT.value(), message, e);
    }
}
