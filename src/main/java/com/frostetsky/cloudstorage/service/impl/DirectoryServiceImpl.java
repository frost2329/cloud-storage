package com.frostetsky.cloudstorage.service.impl;

import com.frostetsky.cloudstorage.dto.ResourceDto;
import com.frostetsky.cloudstorage.excepiton.ResourceAlreadyExistException;
import com.frostetsky.cloudstorage.excepiton.ResourceNotFoundException;
import com.frostetsky.cloudstorage.excepiton.DirectoryServiceException;
import com.frostetsky.cloudstorage.excepiton.InvalidPathException;
import com.frostetsky.cloudstorage.mapper.ResourceMapper;
import com.frostetsky.cloudstorage.service.DirectoryService;
import com.frostetsky.cloudstorage.service.UserService;
import com.frostetsky.cloudstorage.constants.MinioConstants;
import com.frostetsky.cloudstorage.service.props.MinioProperties;
import com.frostetsky.cloudstorage.util.ResourceUtil;
import io.minio.*;
import io.minio.errors.ErrorResponseException;
import io.minio.messages.Item;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DirectoryServiceImpl implements DirectoryService {
    private final MinioClient minioClient;
    private final MinioProperties minioProperties;
    private final UserService userService;
    private final ResourceMapper resourceMapper;

    private String BUCKET_NAME;

    @PostConstruct
    public void init() {
        this.BUCKET_NAME = minioProperties.getBucket();
    }

    public List<ResourceDto> getDirectoryFiles(String username, String path) {
        final String BUCKET_NAME = minioProperties.getBucket();
        if (path == null) {
            throw new InvalidPathException("Не передан path");
        }
        String userBasePath = MinioConstants.USER_BASE_PATH_PATTERN.formatted(
                userService.getUserIdByUsername(username));
        String fullPath = userBasePath + path;

        if (!checkExistDirectory(fullPath)) {
            throw new ResourceNotFoundException("Папка не существует");
        }

        List<ResourceDto> files = new ArrayList<>();
        try {
            Iterable<Result<Item>> results = minioClient.listObjects(
                    ListObjectsArgs.builder()
                            .bucket(BUCKET_NAME)
                            .prefix(fullPath.endsWith("/") ? fullPath : fullPath + "/")
                            .build());

            for (Result<Item> result : results) {
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

    public void createBaseDirectory(String username) {
        Long userId = userService.getUserIdByUsername(username);
        String path = MinioConstants.USER_BASE_PATH_PATTERN.formatted(userId);
        try {
            createEmptyObject(path);
        } catch (Exception e) {
            throw new DirectoryServiceException("Произошла ошибка при создании папки", e);
        }
    }

    public ResourceDto createDirectory(String username, String path) {
        if (path == null || path.isEmpty()) {
            throw new InvalidPathException("Не передан path");
        }
        String userBasePath = MinioConstants.USER_BASE_PATH_PATTERN
                .formatted(userService.getUserIdByUsername(username));
        String fullPath = userBasePath + path;

        if (!checkExistDirectory(ResourceUtil.getParentDirectoryPath(fullPath))) {
            throw new ResourceNotFoundException("Родительская папка не существует");
        }

        if (checkExistDirectory(fullPath)) {
            throw new ResourceAlreadyExistException("Папка уже существует");
        }

        try {
            ObjectWriteResponse response = createEmptyObject(fullPath);
            String resourceName = response.object();
            return new ResourceDto(
                    ResourceUtil.getParentDirectoryPath(resourceName),
                    ResourceUtil.getResourceName(resourceName),
                    null,
                    ResourceUtil.getResourceType(resourceName));
        } catch (Exception e) {
            throw new DirectoryServiceException("Произошла ошибка при создании папки", e);
        }
    }

    @SneakyThrows
    private ObjectWriteResponse createEmptyObject(String path) {
        return minioClient.putObject(PutObjectArgs.builder()
                .bucket(BUCKET_NAME)
                .object(path)
                .stream(new ByteArrayInputStream(new byte[0]), 0, -1)
                .build());
    }

    @SneakyThrows
    private boolean checkExistDirectory(String path) {
        try {
            minioClient.statObject(StatObjectArgs.builder()
                    .bucket(BUCKET_NAME)
                    .object(path)
                    .build());
        } catch (ErrorResponseException e) {
            if (e.errorResponse().code().equals("NoSuchKey")) {
                return false;
            }
            throw e;
        }
        return true;
    }


}
