package com.frostetsky.cloudstorage.service;

import com.frostetsky.cloudstorage.dto.DownloadResultDto;
import com.frostetsky.cloudstorage.dto.ResourceDto;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ResourceService {
    List<ResourceDto> upload(String username, String path, MultipartFile[] file);

    void deleteResource(String path);

    ResourceDto getResourceInfo(String path);

    DownloadResultDto downloadResource(String path);

    ResourceDto moveResource(String pathFrom, String pathTo);

    List<ResourceDto> searchResources(String username, String query);
}
