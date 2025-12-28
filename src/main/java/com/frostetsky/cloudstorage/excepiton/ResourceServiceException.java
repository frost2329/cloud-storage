package com.frostetsky.cloudstorage.excepiton;

import org.springframework.http.HttpStatus;

public class ResourceServiceException extends BaseException {

    public ResourceServiceException(String message) {
        this(message, null);
    }

    public ResourceServiceException(String message, Exception e) {
        super(HttpStatus.INTERNAL_SERVER_ERROR.value(), message, e);
    }
}
