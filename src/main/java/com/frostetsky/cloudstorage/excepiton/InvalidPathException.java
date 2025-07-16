package com.frostetsky.cloudstorage.excepiton;

import org.springframework.http.HttpStatus;

public class InvalidPathException extends BaseException {
    private static final int STATUS_CODE = HttpStatus.BAD_REQUEST.value();
    private static final String MESSAGE = "Невалидный путь";

    public InvalidPathException() {
        this(null);
    }

    public InvalidPathException(Exception e) {
        super(STATUS_CODE, MESSAGE, e);
    }
}
