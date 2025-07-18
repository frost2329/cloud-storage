package com.frostetsky.cloudstorage.excepiton;

import org.springframework.http.HttpStatus;

public class FileServiceException extends BaseException {

    private static final int STATUS_CODE = HttpStatus.INTERNAL_SERVER_ERROR.value();
    private static final String MESSAGE = "Возникла непредвиденная ошибка при работе с файлами";

    public FileServiceException() {
        this(null, null);
    }

    public FileServiceException(String message) {
        this(message, null);
    }

    public FileServiceException(Exception e) {
        this(null, e);
    }

    public FileServiceException(String message, Exception e) {
        super(STATUS_CODE, message != null ? message : MESSAGE, e);
    }
}
