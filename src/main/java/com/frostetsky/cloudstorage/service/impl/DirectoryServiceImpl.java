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
import org.springframework.stereotype.Service;

import java.util.List;


@Service
@RequiredArgsConstructor
public class DirectoryServiceImpl implements DirectoryService {
    private final UserService userService;
    private final ResourceMapper resourceMapper;
    private final S3Service s3Service;

    @Override
    public List<ResourceResponse> getDirectoryFiles(Long userId, String path) {
        String fullPath = ResourcePathUtil.buildBasePath(userId)  + path;
        if (!s3Service.checkExistObject(fullPath)) {
            throw new ResourceNotFoundException("Папка не существует");
        }
        try {
            List<Item> items = s3Service.getObjectsInDirectory(fullPath, false);
            return items.stream()
                    .filter(item -> !fullPath.equals(item.objectName()))
                    .map(resourceMapper::toDto)
                    .toList();
        } catch (Exception e) {
            throw new DirectoryServiceException("Произошла ошибка при получении содержимого папки", e);
        }
    }
    @Override
    public ResourceResponse createDirectory(Long userId, String path) {
        String basePath = ResourcePathUtil.buildBasePath(userId);
        String fullPath = basePath  + path;
        if (!s3Service.checkExistObject(basePath + ResourcePathUtil.getParentDirectoryPath(fullPath))) {
            throw new ResourceNotFoundException("Родительская папка не существует");
        }
        if (s3Service.checkExistObject(fullPath)) {
            throw new ResourceAlreadyExistException("Папка уже существует");
        }
        try {
            ObjectWriteResponse response = s3Service.createEmptyDir(fullPath);
            return resourceMapper.toDto(response.object(), null);
        } catch (Exception e) {
            throw new DirectoryServiceException("Произошла ошибка при создании папки", e);
        }
    }

    public void createBaseDirectory(String username) {
        String basePath = ResourcePathUtil.buildBasePath(userService.getUserIdByUsername(username));
        try {
            if(!s3Service.checkExistObject(basePath)) {
                s3Service.createEmptyDir(basePath);
            }
        } catch (Exception e) {
            throw new DirectoryServiceException("Произошла ошибка при создании папки", e);
        }
    }
}
