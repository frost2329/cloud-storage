package com.frostetsky.cloudstorage.excepiton;

import org.springframework.http.HttpStatus;

public class InvalidPathException extends BaseException {
    private static final int STATUS_CODE = HttpStatus.BAD_REQUEST.value();
    private static final String MESSAGE = "Некорректный путь папки";

    public InvalidPathException() {
        this(null);
    }

    public InvalidPathException(String message) {
        this(null, message);
    }

    public InvalidPathException(Exception e, String message) {
        super(STATUS_CODE, message != null ? message : MESSAGE, e);
    }
}
