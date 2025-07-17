package com.frostetsky.cloudstorage.excepiton;

import org.springframework.http.HttpStatus;

public class DirectoryServiceException extends BaseException {

    private static final int STATUS_CODE = HttpStatus.INTERNAL_SERVER_ERROR.value();
    private static final String MESSAGE = "Возникла непредвиденная ошибка при работе с файлами";

    public DirectoryServiceException() {
        this(null, null);
    }

    public DirectoryServiceException(String message) {
        this(message, null);
    }

    public DirectoryServiceException(Exception e) {
        this(null, e);
    }

    public DirectoryServiceException(String message, Exception e) {
        super(STATUS_CODE, message != null ? message : MESSAGE, e);
    }
}
