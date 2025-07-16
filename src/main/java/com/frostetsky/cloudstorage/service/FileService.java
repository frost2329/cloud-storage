package com.frostetsky.cloudstorage.service;

import com.frostetsky.cloudstorage.excepiton.FileServiceException;
import com.frostetsky.cloudstorage.service.props.MinioProperties;
import io.minio.*;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;


@Service
@RequiredArgsConstructor
public class FileService {
    private final MinioClient minioClient;
    private final MinioProperties minioProperties;
    private final UserService userService;

    public String upload(String path, MultipartFile file) {
        String bucketName = minioProperties.getBucket();
        try {
            createBucketIfNotCreated(bucketName);
            String fileName = extractFileName(path);
            saveFile(bucketName, path, fileName, file);
        } catch (Exception e) {
            throw new FileServiceException(e);
        }
        return null;
    }



    @SneakyThrows
    private void createBucketIfNotCreated(String bucketName) {
        boolean found = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
        if (!found) {
            minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
        }
    }

    @SneakyThrows
    private void saveFile(String bucketName, String fullPath, String fileName, MultipartFile file) {
        minioClient.putObject(
                PutObjectArgs.builder()
                        .bucket(bucketName)
                        .object(fullPath)
                        .stream(file.getInputStream(), file.getSize(), -1)
                        .contentType(file.getContentType())
                        .build());
    }

    private String extractFileName(String path) {
        if (path == null || path.isEmpty()) {
            return "";
        }
        String normalizedPath = path.endsWith("/")
                ? path.substring(0, path.length() - 1)
                : path;

        int lastSlashIndex = normalizedPath.lastIndexOf('/');
        return lastSlashIndex >= 0
                ? normalizedPath.substring(lastSlashIndex + 1)
                : normalizedPath;
    }



}
