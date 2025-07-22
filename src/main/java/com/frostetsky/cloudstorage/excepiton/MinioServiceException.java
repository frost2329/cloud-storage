package com.frostetsky.cloudstorage.excepiton;

import org.springframework.http.HttpStatus;

public class MinioServiceException extends BaseException {

    private static final int STATUS_CODE = HttpStatus.INTERNAL_SERVER_ERROR.value();

    private static final String MESSAGE = "Возникла непредвиденная ошибка при работе с хранилищем S3";

    public MinioServiceException() {
        this(null, null);
    }

    public MinioServiceException(String message) {
        this(message, null);
    }

    public MinioServiceException(Exception e) {
        this(null, e);
    }

    public MinioServiceException(String message, Exception e) {
        super(STATUS_CODE, message != null ? message : MESSAGE, e);
    }
}
