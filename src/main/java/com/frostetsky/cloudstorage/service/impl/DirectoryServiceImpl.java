package com.frostetsky.cloudstorage.service.impl;

import com.frostetsky.cloudstorage.dto.ResourceDto;
import com.frostetsky.cloudstorage.excepiton.ResourceAlreadyExistException;
import com.frostetsky.cloudstorage.excepiton.ResourceNotFoundException;
import com.frostetsky.cloudstorage.excepiton.DirectoryServiceException;
import com.frostetsky.cloudstorage.excepiton.InvalidPathException;
import com.frostetsky.cloudstorage.mapper.ResourceMapper;
import com.frostetsky.cloudstorage.service.DirectoryService;
import com.frostetsky.cloudstorage.service.UserService;
import com.frostetsky.cloudstorage.util.MinioPathUtil;
import io.minio.*;
import io.minio.errors.ErrorResponseException;
import io.minio.messages.Item;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Service;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static com.frostetsky.cloudstorage.constants.MinioConstants.*;

@Service
@RequiredArgsConstructor
public class DirectoryServiceImpl implements DirectoryService {
    private final MinioClient minioClient;
    private final UserService userService;
    private final ResourceMapper resourceMapper;


    public List<ResourceDto> getDirectoryFiles(String username, String path) {
        if (path == null) {
            throw new InvalidPathException("Не передан path");
        }
        String basePath = MinioPathUtil.buildBasePath(userService.getUserIdByUsername(username));
        Path fullPath = Paths.get(basePath, path);
        String fullPathMinio = MinioPathUtil.convertPathToMinioFormat(fullPath.toString()) + "/";
        if (!checkExistResource(fullPathMinio)) {
            throw new ResourceNotFoundException("Папка не существует");
        }
        List<ResourceDto> files = new ArrayList<>();
        try {
            Iterable<Result<Item>> results = minioClient.listObjects(
                    ListObjectsArgs.builder()
                            .bucket(BUCKET_NAME)
                            .prefix(fullPathMinio)
                            .build());
            for (Result<Item> result : results) {
                try {
                    Item item = result.get();
                    if (fullPathMinio.equals(item.objectName())) {
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
        Path fullPath = Paths.get(MinioPathUtil.buildBasePath(userService.getUserIdByUsername(username)), path);
        if (!checkExistResource(MinioPathUtil.convertPathToMinioFormat(fullPath.getParent().toString()) + "/")) {
            throw new ResourceNotFoundException("Родительская папка не существует");
        }
        if (checkExistResource(MinioPathUtil.convertPathToMinioFormat(fullPath.toString()) + "/")) {
            throw new ResourceAlreadyExistException("Папка уже существует");
        }
        try {
            ObjectWriteResponse response = createEmptyDirectory(
                    MinioPathUtil.convertPathToMinioFormat(fullPath.toString()) + "/");
            return resourceMapper.toDto(response, null);
        } catch (Exception e) {
            throw new DirectoryServiceException("Произошла ошибка при создании папки", e);
        }
    }

    public void createBaseDirectory(String username) {
        String basePath = MinioPathUtil.buildBasePath(userService.getUserIdByUsername(username));
        try {
            createEmptyDirectory(basePath);
        } catch (Exception e) {
            throw new DirectoryServiceException("Произошла ошибка при создании папки", e);
        }
    }

    @SneakyThrows
    public ObjectWriteResponse createEmptyDirectory(String path) {
        return minioClient.putObject(PutObjectArgs.builder()
                .bucket(BUCKET_NAME)
                .object(path)
                .stream(EMPTY_DIR_BYTEARRAY_STREAM, EMPTY_DIR_SIZE, PART_SIZE)
                .build());
    }

    @SneakyThrows
    public boolean checkExistResource(String path) {
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
