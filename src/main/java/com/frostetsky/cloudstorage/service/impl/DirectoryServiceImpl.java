package com.frostetsky.cloudstorage.service.impl;

import com.frostetsky.cloudstorage.dto.ResourceDto;
import com.frostetsky.cloudstorage.excepiton.ResourceAlreadyExistException;
import com.frostetsky.cloudstorage.excepiton.ResourceNotFoundException;
import com.frostetsky.cloudstorage.excepiton.DirectoryServiceException;
import com.frostetsky.cloudstorage.excepiton.InvalidPathException;
import com.frostetsky.cloudstorage.mapper.ResourceMapper;
import com.frostetsky.cloudstorage.service.DirectoryService;
import com.frostetsky.cloudstorage.service.S3Service;
import com.frostetsky.cloudstorage.service.UserService;
import com.frostetsky.cloudstorage.util.ResourcePathUtil;
import io.minio.*;
import io.minio.messages.Item;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;


@Service
@RequiredArgsConstructor
public class DirectoryServiceImpl implements DirectoryService {
    private final UserService userService;
    private final ResourceMapper resourceMapper;
    private final S3Service s3Service;


    public List<ResourceDto> getDirectoryFiles(String username, String path) {
        if (path == null) {
            throw new InvalidPathException("Не передан path");
        }
        String fullPath = ResourcePathUtil.buildBasePath(userService.getUserIdByUsername(username)) + path;
        if (!s3Service.checkExistObject(fullPath)) {
            throw new ResourceNotFoundException("Папка не существует");
        }
        List<ResourceDto> files = new ArrayList<>();
        try {
            var results = s3Service.getObjectsInDirectory(fullPath, false);
            for (var result : results) {
                try {
                    Item item = result.get();
                    if (fullPath.equals(item.objectName())) {
                        continue;
                    }
                    files.add(resourceMapper.toDto(item));
                } catch (Exception e) {
                    throw new DirectoryServiceException("Failed to map item to DTO", e);
                }
            }
            return files;
        } catch (Exception e) {
            throw new DirectoryServiceException("Произошла ошибка при получении содержимого папки", e);
        }
    }

    public ResourceDto createDirectory(String username, String path) {
        if (path == null || path.isEmpty()) {
            throw new InvalidPathException("Не передан path");
        }
        String fullPath = ResourcePathUtil.buildBasePath(userService.getUserIdByUsername(username)) + path;
        if (!s3Service.checkExistObject(ResourcePathUtil.getParentDirectoryPath(fullPath))) {
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
            s3Service.createEmptyDir(basePath);
        } catch (Exception e) {
            throw new DirectoryServiceException("Произошла ошибка при создании папки", e);
        }
    }
}
