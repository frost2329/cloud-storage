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
import lombok.SneakyThrows;
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
public class MinioS3ServiceImpl implements S3Service {
    private final MinioClient minioClient;

    @Override
    public boolean checkBaseBucketExists() {
        log.debug("Checking bucket existence: bucket={}", BUCKET_NAME);
        try {
            return minioClient.bucketExists(BucketExistsArgs.builder().bucket(BUCKET_NAME).build());
        } catch (Exception e) {
            log.error("Failed to check bucket existence: bucket={}", BUCKET_NAME, e);
            throw new MinioServiceException("Ошибка при наличия бакета", e);
        }
    }

    @Override
    public void createBaseBucket() {
        log.info("Creating base bucket: bucket={}", BUCKET_NAME);
        try {
            minioClient.makeBucket(MakeBucketArgs.builder()
                    .bucket(BUCKET_NAME)
                    .build());
            log.info("Base bucket created successfully: bucket={}", BUCKET_NAME);
        } catch (Exception e) {
            log.error("Failed to create base bucket: bucket={}", BUCKET_NAME, e);
            throw new MinioServiceException("Ошибка при создании базового бакета", e);
        }
    }

    @Override
    public List<Item> getObjectsInDirectory(String path, boolean recursive) {
        log.debug("Listing objects: bucket={}, prefix={}, recursive={}", BUCKET_NAME, path, recursive);
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
                log.trace("Listed object: object={}, size={}", item.objectName(), item.size());
            }
            log.debug("Objects listed successfully: prefix={}, count={}", path, resultItems.size());
            return resultItems;
        } catch (Exception e) {
            log.error("Failed to list objects: prefix={}, recursive={}", path, recursive, e);
            throw new MinioServiceException("Ошибка при получении файлов из хранилища", e);
        }
    }

    @Override
    @SneakyThrows
    public ObjectWriteResponse createEmptyDir(String path) {
        log.debug("Creating empty directory marker: path={}", path);
        try {
            ObjectWriteResponse response = minioClient.putObject(PutObjectArgs.builder()
                    .bucket(BUCKET_NAME)
                    .object(path)
                    .stream(EMPTY_DIR_BYTEARRAY_STREAM, EMPTY_DIR_SIZE, PART_SIZE)
                    .build());
            log.debug("Empty directory marker created: path={}", path);
            return response;
        } catch (Exception e) {
            log.error("Failed to create empty directory marker: path={}", path, e);
            throw new MinioServiceException("Ошибка при создании директории в хранилище", e);
        }
    }

    @Override
    public ObjectWriteResponse putObject(String path, MultipartFile file) {
        log.info("Uploading object: path={}, filename={}, size={}", path, file.getOriginalFilename(), file.getSize());
        try {
            ObjectWriteResponse response = minioClient.putObject(PutObjectArgs.builder()
                    .bucket(BUCKET_NAME)
                    .object(path)
                    .stream(file.getInputStream(), file.getSize(), PART_SIZE)
                    .build());
            log.info("Object uploaded successfully: path={}, object={}", path, response.object());
            return response;
        } catch (Exception e) {
            log.error("Failed to upload object: path={}, filename={}", path, file.getOriginalFilename(), e);
            throw new MinioServiceException("Ошибка при загрузки файла в хранилище", e);
        }
    }

    @Override
    public void deleteObjects(List<DeleteObject> objectsToDelete) {
        log.info("Deleting objects: count={}", objectsToDelete.size());
        try {
            Iterable<Result<DeleteError>> deleteResults = minioClient.removeObjects(
                    RemoveObjectsArgs.builder()
                            .bucket(BUCKET_NAME)
                            .objects(objectsToDelete)
                            .build());
            for (Result<DeleteError> result : deleteResults) {
                // MinIO returns DeleteError objects on failures; consuming results forces request execution
                DeleteError error = result.get();
                log.warn("Failed to delete object: object={}, message={}", error.objectName(), error.message());
            }
            log.info("Delete request completed: requestedCount={}", objectsToDelete.size());
        } catch (Exception e) {
            log.error("Failed to delete objects: requestedCount={}", objectsToDelete.size(), e);
            throw new MinioServiceException("Произошла непредвиденная ошибка при удалении объекта", e);
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
            log.debug("Object exists: path={}", path);
            return true;
        } catch (ErrorResponseException e) {
            if (e.errorResponse().code().equals("NoSuchKey")) {
                log.debug("Object does not exist: path={}", path);
                return false;
            }
            log.error("Failed to check object existence: path={}, code={}", path, e.errorResponse().code(), e);
            throw new MinioServiceException("Ошибка при удалении проверки наличия объекта в хранилище", e);
        } catch (Exception e) {
            log.error("Failed to check object existence: path={}", path, e);
            throw new MinioServiceException("Ошибка при проверке наличия объекта в хранилище", e);
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
            log.debug("Object download stream opened: path={}", path);
            return object;
        } catch (Exception e) {
            log.error("Failed to download object: path={}", path, e);
            throw new MinioServiceException("Ошибка при загрузки файла из хранилища", e);
        }
    }

    @Override
    public ObjectWriteResponse copyObject(String pathFrom, String pathTo) {
        log.info("Copying object: from={}, to={}", pathFrom, pathTo);
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
            log.error("Failed to copy object: from={}, to={}", pathFrom, pathTo, e);
            throw new MinioServiceException("Ошибка при копировании файла", e);
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
                log.debug("Object not found while fetching info: path={}", path);
                throw new ResourceNotFoundException("Ресурс не найден", e);
            }
            log.error("Failed to fetch object info: path={}, code={}", path, e.errorResponse().code(), e);
            throw new MinioServiceException("Ошибка при получении информации об объекте", e);
        } catch (Exception e) {
            log.error("Failed to fetch object info: path={}", path, e);
            throw new MinioServiceException("Ошибка при получении информации об объекте", e);
        }
    }
}