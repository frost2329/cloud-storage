package com.frostetsky.cloudstorage.excepiton;

import org.springframework.http.HttpStatus;

public class ResourceAlreadyExistException extends BaseException {

    private static final int STATUS_CODE = HttpStatus.CONFLICT.value();
    private static final String MESSAGE = "Папка уже существует";

    public ResourceAlreadyExistException() {
        this(null, null);
    }

    public ResourceAlreadyExistException(String message) {
        this(message, null);
    }

    public ResourceAlreadyExistException(Exception e) {
        this(null, e);
    }

    public ResourceAlreadyExistException(String message, Exception e) {
        super(STATUS_CODE, message != null ? message : MESSAGE, e);
    }
}
