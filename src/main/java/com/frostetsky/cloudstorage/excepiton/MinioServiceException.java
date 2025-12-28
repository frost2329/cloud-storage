package com.frostetsky.cloudstorage.excepiton;

import org.springframework.http.HttpStatus;

public class MinioServiceException extends BaseException {

    public MinioServiceException(String message) {
        this(message, null);
    }

    public MinioServiceException(String message, Exception e) {
        super(HttpStatus.INTERNAL_SERVER_ERROR.value(), message, e);
    }
}
