package com.frostetsky.cloudstorage.service;

import org.springframework.web.multipart.MultipartFile;


public interface ValidationService {

    void validatePath(String path);

    void validateFiles(MultipartFile[] files);

    void validateSearchQuery(String query);
}
