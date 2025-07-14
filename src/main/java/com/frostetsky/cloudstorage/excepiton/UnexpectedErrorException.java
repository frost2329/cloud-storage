package com.frostetsky.cloudstorage.excepiton;

import org.springframework.http.HttpStatus;

public class UnexpectedErrorException extends BaseException {
    private static final int STATUS_CODE = HttpStatus.INTERNAL_SERVER_ERROR.value();
    private static final String MESSAGE = "Неизвестная ошибка";

    public UnexpectedErrorException() {
        this(null);
    }

    public UnexpectedErrorException(Exception e) {
        super(STATUS_CODE, MESSAGE, e);
    }
}
