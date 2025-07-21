package com.frostetsky.cloudstorage.excepiton;

import org.springframework.http.HttpStatus;

public class ResourceServiceException extends BaseException {

    private static final int STATUS_CODE = HttpStatus.INTERNAL_SERVER_ERROR.value();
    private static final String MESSAGE = "Возникла непредвиденная ошибка при работе с файлами";

    public ResourceServiceException() {
        this(null, null);
    }

    public ResourceServiceException(String message) {
        this(message, null);
    }

    public ResourceServiceException(Exception e) {
        this(null, e);
    }

    public ResourceServiceException(String message, Exception e) {
        super(STATUS_CODE, message != null ? message : MESSAGE, e);
    }
}
