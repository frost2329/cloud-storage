package com.frostetsky.cloudstorage.excepiton;

import org.springframework.http.HttpStatus;

public class UserServiceException extends BaseException {

    public UserServiceException(String message) {
        this(message, null);
    }

    public UserServiceException(String message, Exception e) {
        super(HttpStatus.INTERNAL_SERVER_ERROR.value(), message, e);
    }
}
