package com.frostetsky.cloudstorage.excepiton;

import org.springframework.http.HttpStatus;

public class DirectoryServiceException extends BaseException {

    private static final int STATUS_CODE = HttpStatus.INTERNAL_SERVER_ERROR.value();

    public DirectoryServiceException(String message) {
        this(message, null);
    }

    public DirectoryServiceException(String message, Exception e) {
        super(STATUS_CODE, message, e);
    }
}
