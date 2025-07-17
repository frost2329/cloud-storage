package com.frostetsky.cloudstorage.service;

import com.frostetsky.cloudstorage.dto.ResourceDto;
import com.frostetsky.cloudstorage.excepiton.DirectoryServiceException;
import com.frostetsky.cloudstorage.excepiton.InvalidPathException;
import com.frostetsky.cloudstorage.mapper.ResourceMapper;
import com.frostetsky.cloudstorage.service.constants.MinioConstants;
import com.frostetsky.cloudstorage.service.props.MinioProperties;
import io.minio.ListObjectsArgs;
import io.minio.MinioClient;
import io.minio.Result;
import io.minio.messages.Item;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DirectoryService {
    private final MinioClient minioClient;
    private final MinioProperties minioProperties;
    private final UserService userService;
    private final ResourceMapper resourceMapper;

    public List<ResourceDto> getDirectoryFiles(String username, String path) {
        if (path == null) {
            throw new InvalidPathException("Не передан path");
        }
        String userBasePath = MinioConstants.USER_BASE_PATH_PATTERN.formatted(
                userService.getUserIdByUsername(username));
        String fullPath = userBasePath + path;
        List<ResourceDto> files = new ArrayList<>();
        try {
            Iterable<Result<Item>> results = minioClient.listObjects(
                    ListObjectsArgs.builder()
                            .bucket(minioProperties.getBucket())
                            .prefix(fullPath.endsWith("/") ? fullPath : fullPath + "/")
                            .build());

            for (Result<Item> result : results) {
                try {
                    files.add(resourceMapper.toDto(result.get()));
                } catch (Exception e) {
                    throw new DirectoryServiceException("Failed to map item to DTO", e);
                }
            }
            return files;
        } catch (Exception e) {
            throw new DirectoryServiceException("Произошла ошибка при получении содержимого папки", e);
        }
    }
}
