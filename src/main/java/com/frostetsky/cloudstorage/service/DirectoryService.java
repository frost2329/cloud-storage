package com.frostetsky.cloudstorage.service;

import com.frostetsky.cloudstorage.dto.FileDto;
import com.frostetsky.cloudstorage.excepiton.FileServiceException;
import com.frostetsky.cloudstorage.excepiton.InvalidPathException;
import com.frostetsky.cloudstorage.service.props.MinioProperties;
import io.minio.ListObjectsArgs;
import io.minio.MinioClient;
import io.minio.Result;
import io.minio.messages.Item;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DirectoryService {
    private final MinioClient minioClient;
    private final MinioProperties minioProperties;
    private final UserService userService;

    public List<FileDto> getDirectoryFiles(String username, String path) {
        List<FileDto> files = new ArrayList<>();
        if (path == null) {
            throw new InvalidPathException();
        }
        String bucketName = minioProperties.getBucket();
        Long userId = userService.getUserIdByUsername(username);
        String userBasePath = getUserBasePath(userId);
        String fullPath = userBasePath + path;

        try {
            Iterable<Result<Item>> results = minioClient.listObjects(
                    ListObjectsArgs.builder()
                            .bucket(bucketName)
                            .prefix(fullPath.endsWith("/") ? fullPath : fullPath + "/")
                            .build());

            for (Result<Item> result : results) {
                files.add(getFileDto(result));
            }
            return files;
        } catch (Exception e) {
            throw new FileServiceException(e);
        }
    }

    @SneakyThrows
    private FileDto getFileDto(Result<Item> result) {
        Item object = result.get();
        String fullName = object.objectName();
        String fullNameWithoutSlash = fullName.endsWith("/")
                ? fullName.substring(0, fullName.length() - 1)
                : fullName;
        int lastSlashIndex = fullNameWithoutSlash.lastIndexOf('/');
        String objectName = fullNameWithoutSlash.substring(lastSlashIndex + 1);
        String objectPath = fullNameWithoutSlash.substring(0, lastSlashIndex + 1);
        return new FileDto(
                objectPath,
                objectName,
                object.size(),
                fullName.endsWith("/") ? "DIRECTORY" : "FILE");
    }

    private String getUserBasePath(Long userId) {
        return "user-%s-files/".formatted(userId);
    }
}
