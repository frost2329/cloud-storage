package com.frostetsky.cloudstorage.service;

import com.frostetsky.cloudstorage.dto.DownloadResultDto;
import com.frostetsky.cloudstorage.dto.ResourceResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ResourceService {
    List<ResourceResponse> upload(Long userId, String path, MultipartFile[] objects);

    void deleteResource(Long userId, String path);

    ResourceResponse getResourceInfo(Long userId, String path);

    DownloadResultDto downloadResource(Long userId, String path);

    ResourceResponse moveResource(Long userId, String pathFrom, String pathTo);

    List<ResourceResponse> searchResources(Long userId, String query);
}
