package com.frostetsky.cloudstorage.excepiton;


import org.springframework.http.HttpStatus;

public class UserAlreadyExistException extends BaseException {

    public UserAlreadyExistException(String message, Exception e) {
        super(HttpStatus.CONFLICT.value(), message, e);
    }
}