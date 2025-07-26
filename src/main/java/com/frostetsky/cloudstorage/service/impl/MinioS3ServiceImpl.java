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
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import static com.frostetsky.cloudstorage.constants.MinioConstants.*;

@Service
@RequiredArgsConstructor
public class MinioS3ServiceImpl implements S3Service {
    private final MinioClient minioClient;

    @Override
    public List<Item> getObjectsInDirectory(String path, boolean recursive) {
        try {
            List<Item> resultItems = new ArrayList<>();
            Iterable<Result<Item>> results =  minioClient.listObjects(
                    ListObjectsArgs.builder()
                            .bucket(BUCKET_NAME)
                            .prefix(path)
                            .recursive(recursive)
                            .build());
            for (Result<Item> result : results) {
                resultItems.add(result.get());
            }
            return resultItems;
        } catch (Exception e) {
            throw new MinioServiceException("Произошла непредвиденная ошибка получении файлов", e);
        }
    }

    @Override
    @SneakyThrows
    public ObjectWriteResponse createEmptyDir(String path) {
        try {
            return minioClient.putObject(PutObjectArgs.builder()
                    .bucket(BUCKET_NAME)
                    .object(path)
                    .stream(EMPTY_DIR_BYTEARRAY_STREAM, EMPTY_DIR_SIZE, PART_SIZE)
                    .build());
        } catch (Exception e) {
            throw new MinioServiceException("Произошла непредвиденная ошибка при создании директории", e);
        }
    }

    @Override
    public ObjectWriteResponse putObject(String path, MultipartFile file) {
        try {
            return minioClient.putObject(PutObjectArgs.builder()
                    .bucket(BUCKET_NAME)
                    .object(path)
                    .stream(file.getInputStream(), file.getSize(), PART_SIZE)
                    .build());
        } catch (Exception e) {
            throw new MinioServiceException("Произошла непредвиденная ошибка при загрузки файла", e);
        }
    }

    @Override
    public void deleteObjects(List<DeleteObject> objectsToDelete) {
        try {
            Iterable<Result<DeleteError>> deleteResults = minioClient.removeObjects(
                    RemoveObjectsArgs.builder()
                            .bucket(BUCKET_NAME)
                            .objects(objectsToDelete)
                            .build());
            for (Result<DeleteError> result : deleteResults) {
                System.out.println(result.get().objectName());
            }
        } catch (Exception e) {
            throw new MinioServiceException("Произошла непредвиденная ошибка при удалении объекта", e);
        }
    }

    @Override
    public boolean checkExistObject(String path) {
        try {
            minioClient.statObject(StatObjectArgs.builder()
                    .bucket(BUCKET_NAME)
                    .object(path)
                    .build());
            return true;
        } catch (ErrorResponseException e) {
            if (e.errorResponse().code().equals("NoSuchKey")) {
                return false;
            }
            throw new MinioServiceException("Произошла непредвиденная ошибка при получении информации о файле", e);
        } catch (Exception e) {
            throw new MinioServiceException("Произошла непредвиденная ошибка при получении информации о файле", e);
        }
    }

    @Override
    public InputStream downloadObject(String path) {
        try {
            return minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(BUCKET_NAME)
                            .object(path)
                            .build());
        } catch (Exception e) {
            throw new MinioServiceException("Произошла ошибка при загрузки файла", e);
        }
    }

    @Override
    public ObjectWriteResponse copyObject(String pathFrom, String pathTo) {
        try {
            return minioClient.copyObject(
                    CopyObjectArgs.builder()
                            .bucket(BUCKET_NAME)
                            .object(pathTo)
                            .source(CopySource.builder()
                                    .bucket(BUCKET_NAME)
                                    .object(pathFrom)
                                    .build())
                            .build());
        } catch (Exception e) {
            throw new MinioServiceException("Произошла ошибка при загрузки файла", e);
        }
    }

    @Override
    public StatObjectResponse getObjectInfo(String path) {
        try {
            return minioClient.statObject(StatObjectArgs.builder()
                    .bucket(BUCKET_NAME)
                    .object(path)
                    .build());
        } catch (ErrorResponseException e) {
            if (e.errorResponse().code().equals("NoSuchKey")) {
                throw new ResourceNotFoundException("Ресурс не найден", e);
            }
            throw new MinioServiceException("Произошла непредвиденная ошибка при получении информации о файле", e);
        } catch (Exception e) {
            throw new MinioServiceException("Произошла непредвиденная ошибка при получении информации о файле", e);
        }
    }
}
