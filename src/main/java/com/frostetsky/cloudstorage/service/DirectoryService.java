package com.frostetsky.cloudstorage.service;

import com.frostetsky.cloudstorage.dto.ResourceDto;

import java.util.List;

public interface DirectoryService {

    List<ResourceDto> getDirectoryFiles(String username, String path);

    void createBaseDirectory(String username);

    ResourceDto createDirectory(String username, String path);
}
