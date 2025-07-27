package com.frostetsky.cloudstorage.excepiton;

import org.springframework.http.HttpStatus;

public class MinioServiceException extends BaseException {

    private static final int STATUS_CODE = HttpStatus.INTERNAL_SERVER_ERROR.value();

    public MinioServiceException(String message) {
        this(message, null);
    }

    public MinioServiceException(String message, Exception e) {
        super(STATUS_CODE, message, e);
    }
}
