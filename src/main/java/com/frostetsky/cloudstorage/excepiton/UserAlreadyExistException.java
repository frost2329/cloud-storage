package com.frostetsky.cloudstorage.excepiton;


import org.springframework.http.HttpStatus;

public class UserAlreadyExistException extends BaseException {
    private static final int STATUS_CODE = HttpStatus.CONFLICT.value();
    private static final String MESSAGE = "Пользователь с этим логином  уже существует";

    public UserAlreadyExistException() {
        this(null);
    }

    public UserAlreadyExistException(Exception e) {
        super(STATUS_CODE, MESSAGE, e);
    }
}