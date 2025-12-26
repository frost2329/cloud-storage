package com.frostetsky.cloudstorage.service.impl;

import com.frostetsky.cloudstorage.dto.ResourceResponse;
import com.frostetsky.cloudstorage.excepiton.ResourceAlreadyExistException;
import com.frostetsky.cloudstorage.excepiton.ResourceNotFoundException;
import com.frostetsky.cloudstorage.excepiton.DirectoryServiceException;
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
            log.debug("Directory not found: userId={}, path={}, fullPath={}", userId, path, fullPath);
            throw new ResourceNotFoundException("Папка не существует");
        }
        try {
            log.debug("Listing directory content: userId={}, path={}", userId, path);
            List<Item> items = s3Service.getObjectsInDirectory(fullPath, false);
            return items.stream()
                    .filter(item -> !fullPath.equals(item.objectName()))
                    .map(resourceMapper::toDto)
                    .toList();
        } catch (Exception e) {
            log.error("Failed to list directory content: userId={}, path={}, fullPath={}", userId, path, fullPath, e);
            throw new DirectoryServiceException("Произошла ошибка при получении содержимого папки", e);
        }
    }

    @Override
    public ResourceResponse createDirectory(Long userId, String path) {
        String basePath = ResourcePathUtil.buildBasePath(userId);
        String fullPath = basePath + path;
        if (!s3Service.checkExistObject(basePath + ResourcePathUtil.extractParentDirectoryPath(fullPath))) {
            log.debug("Create directory failed: parent directory not found: userId={}, path={}, fullPath={}", userId, path, fullPath);
            throw new ResourceNotFoundException("Родительская папка не существует");
        }
        if (s3Service.checkExistObject(fullPath)) {
            log.debug("Create directory failed: directory already exists: userId={}, path={}, fullPath={}", userId, path, fullPath);
            throw new ResourceAlreadyExistException("Папка уже существует");
        }
        try {
            log.info("Creating directory: userId={}, path={}", userId, path);
            ObjectWriteResponse response = s3Service.createEmptyDir(fullPath);
            log.info("Directory created successfully: userId={}, path={}", userId, path);
            return resourceMapper.toDto(response.object(), null);
        } catch (Exception e) {
            log.error("Failed to create directory: userId={}, path={}, fullPath={}", userId, path, fullPath, e);
            throw new DirectoryServiceException("Произошла ошибка при создании папки", e);
        }
    }

    public void createBaseDirectory(String username) {
        String basePath = ResourcePathUtil.buildBasePath(userService.getUserIdByUsername(username));
        try {
            if (!s3Service.checkExistObject(basePath)) {
                log.info("Creating base directory for user: username={}, basePath={}", username, basePath);
                s3Service.createEmptyDir(basePath);
                log.info("Base directory created for user: username={}, basePath={}", username, basePath);
            } else {
                log.debug("Base directory already exists for user: username={}, basePath={}", username, basePath);
            }
        } catch (Exception e) {
            log.error("Failed to create base directory for user: username={}, basePath={}", username, basePath, e);
            throw new DirectoryServiceException("Произошла ошибка при создании папки", e);
        }
    }
}
