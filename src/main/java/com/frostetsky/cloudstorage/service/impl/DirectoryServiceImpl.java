package com.frostetsky.cloudstorage.service.impl;

import com.frostetsky.cloudstorage.dto.ResourceDto;
import com.frostetsky.cloudstorage.excepiton.ResourceAlreadyExistException;
import com.frostetsky.cloudstorage.excepiton.ResourceNotFoundException;
import com.frostetsky.cloudstorage.excepiton.DirectoryServiceException;
import com.frostetsky.cloudstorage.excepiton.InvalidParamException;
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


    public List<ResourceDto> getDirectoryFiles(String username, String path) {
        if (path == null) {
            throw new InvalidParamException("Не передан path");
        }
        String fullPath = ResourcePathUtil.buildBasePath(userService.getUserIdByUsername(username)) + path;
        if (!s3Service.checkExistObject(fullPath)) {
            throw new ResourceNotFoundException("Папка не существует");
        }
        try {
            List<Item> items = s3Service.getObjectsInDirectory(fullPath, false);
            List<ResourceDto> files = items.stream()
                    .filter(item -> !fullPath.equals(item.objectName()))
                    .map(resourceMapper::toDto)
                    .toList();
            return files;
        } catch (Exception e) {
            throw new DirectoryServiceException("Произошла ошибка при получении содержимого папки", e);
        }
    }

    public ResourceDto createDirectory(String username, String path) {
        if (path == null || path.isEmpty()) {
            throw new InvalidParamException("Не передан path");
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
