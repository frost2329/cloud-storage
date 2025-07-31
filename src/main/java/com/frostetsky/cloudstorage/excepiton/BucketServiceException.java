package com.frostetsky.cloudstorage.excepiton;

import org.springframework.http.HttpStatus;

public class BucketServiceException extends BaseException {

    private static final int STATUS_CODE = HttpStatus.INTERNAL_SERVER_ERROR.value();

    public BucketServiceException(String message) {
        this(message, null);
    }

    public BucketServiceException(String message, Exception e) {
        super(STATUS_CODE, message, e);
    }
}
