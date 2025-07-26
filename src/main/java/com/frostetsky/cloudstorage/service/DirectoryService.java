package com.frostetsky.cloudstorage.service;

import com.frostetsky.cloudstorage.dto.ResourceResponse;

import java.util.List;

public interface DirectoryService {

    List<ResourceResponse> getDirectoryFiles(String username, String path);

    void createBaseDirectory(String username);

    ResourceResponse createDirectory(String username, String path);
}
