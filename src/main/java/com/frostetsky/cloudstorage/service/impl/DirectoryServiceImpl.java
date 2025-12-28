package com.frostetsky.cloudstorage.service.impl;

import com.frostetsky.cloudstorage.dto.ResourceResponse;
import com.frostetsky.cloudstorage.excepiton.ResourceAlreadyExistException;
import com.frostetsky.cloudstorage.excepiton.ResourceNotFoundException;
import com.frostetsky.cloudstorage.mapper.ResourceMapper;
import com.frostetsky.cloudstorage.service.DirectoryService;
import com.frostetsky.cloudstorage.service.S3Service;
import com.frostetsky.cloudstorage.service.UserService;
import com.frostetsky.cloudstorage.util.ResourcePathUtil;
import io.minio.*;
import io.minio.messages.Item;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;


@Slf4j
@Service
@RequiredArgsConstructor
public class DirectoryServiceImpl implements DirectoryService {

    private final UserService userService;
    private final ResourceMapper resourceMapper;
    private final S3Service s3Service;

    @Override
    public List<ResourceResponse> getDirectoryFiles(Long userId, String path) {
        String fullPath = ResourcePathUtil.buildBasePath(userId) + path;
        if (!s3Service.checkExistObject(fullPath)) {
            throw new ResourceNotFoundException("Directory is not exist");
        }
        log.debug("Listing directory content: userId={}, path={}", userId, path);
        List<Item> items = s3Service.getObjectsInDirectory(fullPath, false);
        return items.stream()
                .filter(item -> !fullPath.equals(item.objectName()))
                .map(resourceMapper::toDto)
                .toList();
    }

    @Override
    public ResourceResponse createDirectory(Long userId, String path) {
        String basePath = ResourcePathUtil.buildBasePath(userId);
        String fullPath = basePath + path;
        if (!s3Service.checkExistObject(basePath + ResourcePathUtil.extractParentDirectoryPath(fullPath))) {
            throw new ResourceNotFoundException("Parent directory is not exist");
        }
        if (s3Service.checkExistObject(fullPath)) {
            throw new ResourceAlreadyExistException("Directory is not exist");
        }
        ObjectWriteResponse response = s3Service.createEmptyDir(fullPath);
        log.info("Directory created successfully: userId={}, path={}", userId, path);
        return resourceMapper.toDto(response.object(), null);
    }

    public void createBaseDirectory(String username) {
        String basePath = ResourcePathUtil.buildBasePath(userService.getUserIdByUsername(username));
        if (!s3Service.checkExistObject(basePath)) {
            s3Service.createEmptyDir(basePath);
            log.info("Base directory created for user: username={}, basePath={}", username, basePath);
        } else {
            log.debug("Base directory already exists for user: username={}, basePath={}", username, basePath);
        }
    }
}
