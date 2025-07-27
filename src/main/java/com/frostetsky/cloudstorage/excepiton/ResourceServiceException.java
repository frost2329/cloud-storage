package com.frostetsky.cloudstorage.excepiton;

import org.springframework.http.HttpStatus;

public class ResourceServiceException extends BaseException {

    private static final int STATUS_CODE = HttpStatus.INTERNAL_SERVER_ERROR.value();

    public ResourceServiceException(String message) {
        this(message, null);
    }

    public ResourceServiceException(String message, Exception e) {
        super(STATUS_CODE, message, e);
    }
}
