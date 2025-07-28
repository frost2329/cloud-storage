package com.frostetsky.cloudstorage.service.impl;

import com.frostetsky.cloudstorage.excepiton.InvalidBodyException;
import com.frostetsky.cloudstorage.excepiton.InvalidParamException;
import com.frostetsky.cloudstorage.service.ValidationService;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class ValidationServiceImpl implements ValidationService {
    private static final String PATH_REGEX = "^$|^[a-zA-Z0-9_\\-./]+$";
    private static final String QUERY_REGEX = "^[a-zA-Z0-9_\\-.]+$";
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024;

    public void validatePath(String path) {
        if (path == null) {
           throw new InvalidParamException("Path cannot be empty");
        }
        if (!path.matches(PATH_REGEX)) {
            throw new InvalidParamException("Path can only contain: a-z, A-Z, 0-9, _, -, ., /");
        }
    }

    public void validateFiles(MultipartFile[] files) {
        if (files == null || files.length == 0) {
            throw new InvalidBodyException("Файлы не переданы");
        }
        for (MultipartFile file : files) {
            if (file == null) {
                throw new InvalidBodyException("Один из файлов пуст");
            }
            if (file.getOriginalFilename() == null || file.getOriginalFilename().isBlank()) {
                throw new InvalidBodyException("Имя файла не указано");
            }
            if (file.getSize() >= MAX_FILE_SIZE) {
                throw new InvalidBodyException("Файл '" + file.getOriginalFilename()
                                                + "' слишком большой размер должен быть не больше 100 MB");
            }
        }
    }

    public void validateSearchQuery(String query) {
        if (query == null || query.isEmpty()) {
            throw new InvalidParamException("Query cannot be empty");
        }
        if (!query.matches(QUERY_REGEX)) {
            throw new InvalidParamException("Query can only contain: a-z, A-Z, 0-9, _, -, .");
        }
    }
}
