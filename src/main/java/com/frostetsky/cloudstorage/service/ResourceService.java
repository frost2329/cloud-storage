package com.frostetsky.cloudstorage.service;

import com.frostetsky.cloudstorage.dto.ResourceDto;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ResourceService {
    List<ResourceDto> upload(String username, String path, MultipartFile[] file);

    void delete(String path);

    ResourceDto getResourceInfo(String path);
}
