package com.frostetsky.cloudstorage.excepiton;

import org.springframework.http.HttpStatus;

public class UserServiceException extends BaseException {
    private static final int STATUS_CODE = HttpStatus.INTERNAL_SERVER_ERROR.value();

    public UserServiceException(String message) {
        this(message, null);
    }

    public UserServiceException(String message, Exception e) {
        super(STATUS_CODE, message, e);
    }
}
