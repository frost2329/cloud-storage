package com.frostetsky.cloudstorage.service.impl;

import com.frostetsky.cloudstorage.excepiton.MinioServiceException;
import com.frostetsky.cloudstorage.excepiton.ResourceNotFoundException;
import com.frostetsky.cloudstorage.service.S3Service;
import io.minio.*;
import io.minio.errors.ErrorResponseException;
import io.minio.messages.DeleteError;
import io.minio.messages.DeleteObject;
import io.minio.messages.Item;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import static com.frostetsky.cloudstorage.constants.MinioConstants.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class MinioService implements S3Service {
    private final MinioClient minioClient;

    @Override
    public boolean checkBaseBucketExists() {
        log.debug("Checking bucket existence: bucket={}", BUCKET_NAME);
        try {
            log.info("Base bucket checked successfully: bucket={}", BUCKET_NAME);
            return minioClient.bucketExists(BucketExistsArgs.builder().bucket(BUCKET_NAME).build());
        } catch (Exception e) {
            log.error("Failed to check bucket: {}", BUCKET_NAME);
            throw new MinioServiceException("Failed to check bucket", e);
        }
    }

    @Override
    public void createBaseBucket() {
        log.debug("Creating base bucket: bucket={}", BUCKET_NAME);
        try {
            minioClient.makeBucket(MakeBucketArgs.builder()
                    .bucket(BUCKET_NAME)
                    .build());
            log.info("Base bucket created successfully: bucket={}", BUCKET_NAME);
        } catch (Exception e) {
            log.error("Failed to create base bucket: {}", BUCKET_NAME);
            throw new MinioServiceException("Failed to create base bucket", e);
        }
    }

    @Override
    public List<Item> getObjectsInDirectory(String path, boolean recursive) {
        log.debug("Fetching  objects: bucket={}, prefix={}, recursive={}", BUCKET_NAME, path, recursive);
        try {
            List<Item> resultItems = new ArrayList<>();
            Iterable<Result<Item>> results = minioClient.listObjects(
                    ListObjectsArgs.builder()
                            .bucket(BUCKET_NAME)
                            .prefix(path)
                            .recursive(recursive)
                            .build());
            for (Result<Item> result : results) {
                Item item = result.get();
                resultItems.add(item);
            }
            log.info("Objects fetched successfully: prefix={}, count={}", path, resultItems.size());
            return resultItems;
        } catch (Exception e) {
            log.error("Failed to fetch objects: prefix={}, recursive={}", path, recursive);
            throw new MinioServiceException("Failed to list objects", e);
        }
    }

    @Override
    public ObjectWriteResponse createEmptyDir(String path) {
        log.debug("Creating empty directory: path={}", path);
        try {
            ObjectWriteResponse response = minioClient.putObject(PutObjectArgs.builder()
                    .bucket(BUCKET_NAME)
                    .object(path)
                    .stream(EMPTY_DIR_BYTEARRAY_STREAM, EMPTY_DIR_SIZE, PART_SIZE)
                    .build());
            log.info("Empty directory created: path={}", path);
            return response;
        } catch (Exception e) {
            log.error("Failed to create empty directory: path={}", path);
            throw new MinioServiceException("Failed to create empty directory", e);
        }
    }

    @Override
    public ObjectWriteResponse putObject(String path, MultipartFile file) {
        log.debug("Uploading object: path={}, filename={}, size={}", path, file.getOriginalFilename(), file.getSize());
        try {
            ObjectWriteResponse response = minioClient.putObject(PutObjectArgs.builder()
                    .bucket(BUCKET_NAME)
                    .object(path)
                    .stream(file.getInputStream(), file.getSize(), PART_SIZE)
                    .build());
            log.info("Object uploaded successfully: path={}, object={}", path, response.object());
            return response;
        } catch (Exception e) {
            log.error("Failed to upload object: path={}, filename={}", path, file.getOriginalFilename());
            throw new MinioServiceException("Failed to upload object", e);
        }
    }

    @Override
    public void deleteObjects(List<DeleteObject> objectsToDelete) {
        log.debug("Deleting objects: count={}", objectsToDelete.size());
        try {
            Iterable<Result<DeleteError>> errors = minioClient.removeObjects(
                    RemoveObjectsArgs.builder()
                            .bucket(BUCKET_NAME)
                            .objects(objectsToDelete)
                            .build());
            for (Result<DeleteError> error : errors) {
                DeleteError err = error.get();
                throw new RuntimeException("Failed to delete object: " + err.objectName() + " - " + err.message());
            }
            log.info("Delete request completed: requestedCount={}", objectsToDelete.size());
        } catch (Exception e) {
            log.error("Failed to delete objects: count={}", objectsToDelete.size());
            throw new MinioServiceException("Failed to delete objects", e);
        }
    }

    @Override
    public boolean checkExistObject(String path) {
        log.debug("Checking object existence: path={}", path);
        try {
            minioClient.statObject(StatObjectArgs.builder()
                    .bucket(BUCKET_NAME)
                    .object(path)
                    .build());
            log.info("Object exists: path={}", path);
            return true;
        } catch (ErrorResponseException e) {
            if (e.errorResponse().code().equals("NoSuchKey")) {
                log.info("Object does not exist: path={}", path);
                return false;
            }
            throw new MinioServiceException("Failed to check object existence", e);
        } catch (Exception e) {
            log.error("Failed to check object existence: path={}", path);
            throw new MinioServiceException("Failed to check object existence", e);
        }
    }

    @Override
    public InputStream downloadObject(String path) {
        log.debug("Downloading object: path={}", path);
        try {
            GetObjectResponse object = minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(BUCKET_NAME)
                            .object(path)
                            .build());
            log.info("Object download stream opened: path={}", path);
            return object;
        } catch (Exception e) {
            log.error("Failed to download object: path={}", path);
            throw new MinioServiceException("Failed to download object from storage", e);
        }
    }

    @Override
    public ObjectWriteResponse copyObject(String pathFrom, String pathTo) {
        log.debug("Copying object: from={}, to={}", pathFrom, pathTo);
        try {
            ObjectWriteResponse response = minioClient.copyObject(
                    CopyObjectArgs.builder()
                            .bucket(BUCKET_NAME)
                            .object(pathTo)
                            .source(CopySource.builder()
                                    .bucket(BUCKET_NAME)
                                    .object(pathFrom)
                                    .build())
                            .build());
            log.info("Object copied successfully: from={}, to={}", pathFrom, pathTo);
            return response;
        } catch (Exception e) {
            log.error("Failed to copy object: from={}, to={}", pathFrom, pathTo);
            throw new MinioServiceException("Failed to copy object", e);
        }
    }

    @Override
    public StatObjectResponse getObjectInfo(String path) {
        log.debug("Fetching object info: path={}", path);
        try {
            StatObjectResponse response = minioClient.statObject(StatObjectArgs.builder()
                    .bucket(BUCKET_NAME)
                    .object(path)
                    .build());
            log.debug("Object info fetched: path={}", path);
            return response;
        } catch (ErrorResponseException e) {
            if (e.errorResponse().code().equals("NoSuchKey")) {
                log.debug("Object not found: path={}", path);
                throw new ResourceNotFoundException("Resource not found", e);
            }
            log.error("Failed to fetch object info: path={}, code={}", path, e.errorResponse().code());
            throw new MinioServiceException("Failed to fetch object info", e);
        } catch (Exception e) {
            log.error("Failed to fetch object info: path={}", path);
            throw new MinioServiceException("Failed to fetch object info", e);
        }
    }
}