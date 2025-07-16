package com.frostetsky.cloudstorage.excepiton;

import org.springframework.http.HttpStatus;

public class FileServiceException extends BaseException {
    private static final int STATUS_CODE = HttpStatus.INTERNAL_SERVER_ERROR.value();
    private static final String MESSAGE = "Возникла непредвиденная ошибка";

    public FileServiceException() {
        this(null);
    }

    public FileServiceException(Exception e) {
        super(STATUS_CODE, MESSAGE, e);
    }
}
