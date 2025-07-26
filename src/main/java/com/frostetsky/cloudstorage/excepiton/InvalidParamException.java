package com.frostetsky.cloudstorage.excepiton;

import org.springframework.http.HttpStatus;

public class InvalidParamException extends BaseException {
    private static final int STATUS_CODE = HttpStatus.BAD_REQUEST.value();
    private static final String MESSAGE = "Некорректный путь папки";

    public InvalidParamException() {
        this(null);
    }

    public InvalidParamException(String message) {
        this(null, message);
    }

    public InvalidParamException(Exception e, String message) {
        super(STATUS_CODE, message != null ? message : MESSAGE, e);
    }
}
