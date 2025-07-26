package com.frostetsky.cloudstorage.service;

import com.frostetsky.cloudstorage.dto.DownloadResultDto;
import com.frostetsky.cloudstorage.dto.ResourceResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ResourceService {
    List<ResourceResponse> upload(String username, String path, MultipartFile[] objects);

    void deleteResource(String username, String path);

    ResourceResponse getResourceInfo(String username, String path);

    DownloadResultDto downloadResource(String username, String path);

    ResourceResponse moveResource(String username, String pathFrom, String pathTo);

    List<ResourceResponse> searchResources(String username, String query);
}
