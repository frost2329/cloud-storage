package com.frostetsky.cloudstorage.excepiton;

import org.springframework.http.HttpStatus;

public class ResourceNotFoundException extends BaseException {

    private static final int STATUS_CODE = HttpStatus.NOT_FOUND.value();
    private static final String MESSAGE = "Папка не существует";

    public ResourceNotFoundException() {
        this(null, null);
    }

    public ResourceNotFoundException(String message) {
        this(message, null);
    }

    public ResourceNotFoundException(Exception e) {
        this(null, e);
    }

    public ResourceNotFoundException(String message, Exception e) {
        super(STATUS_CODE, message != null ? message : MESSAGE, e);
    }
}
