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
    public List<Item> getObjectsInDirectory(String path, boolean recursive) {
        log.info("Получение объектов в директории: path={}, recursive={}", path, recursive);
        try {
            List<Item> resultItems = new ArrayList<>();
            Iterable<Result<Item>> results =  minioClient.listObjects(
                    ListObjectsArgs.builder()
                            .bucket(BUCKET_NAME)
                            .prefix(path)
                            .recursive(recursive)
                            .build());
            for (Result<Item> result : results) {
                Item item = result.get();
                resultItems.add(item);
                log.debug("Получен объект: item={}, size={}", item.objectName(), item.size());
            }
            log.info("Успешно получено {} объектов из директории {}", resultItems.size(), path);
            return resultItems;
        } catch (Exception e) {
            log.error("Ошибка при получении файлов из хранилища: path={}", path, e);
            throw new MinioServiceException("Ошибка при получении файлов из хранилища", e);
        }
    }

    @Override
    @SneakyThrows
    public ObjectWriteResponse createEmptyDir(String path) {
        log.info("Создание директории: path={}", path);
        try {
            ObjectWriteResponse response = minioClient.putObject(PutObjectArgs.builder()
                    .bucket(BUCKET_NAME)
                    .object(path)
                    .stream(EMPTY_DIR_BYTEARRAY_STREAM, EMPTY_DIR_SIZE, PART_SIZE)
                    .build());
            log.info("Директория успешно создана path={}", path);
            return response;
        } catch (Exception e) {
            log.error("Ошибка при создании директории в хранилище: path={}", path, e);
            throw new MinioServiceException("Ошибка при создании директории в хранилище", e);
        }
    }

    @Override
    public ObjectWriteResponse putObject(String path, MultipartFile file) {
        log.info("Отправка файла в хранилище: path={}, file={}", path, file.getOriginalFilename());
        try {
            ObjectWriteResponse response = minioClient.putObject(PutObjectArgs.builder()
                    .bucket(BUCKET_NAME)
                    .object(path)
                    .stream(file.getInputStream(), file.getSize(), PART_SIZE)
                    .build());
            log.info("Файл успешно загружен в хранилище: path={}, file={}", path, response.object());
            return response;
        } catch (Exception e) {
            log.error("Ошибка при загрузки файла в хранилище: path={}", path, e);
            throw new MinioServiceException("Ошибка при загрузки файла в хранилище", e);
        }
    }

    @Override
    public void deleteObjects(List<DeleteObject> objectsToDelete) {
        log.info("Удаление файлов из хранилища: count={}", objectsToDelete.size());
        try {
            Iterable<Result<DeleteError>> deleteResults = minioClient.removeObjects(
                    RemoveObjectsArgs.builder()
                            .bucket(BUCKET_NAME)
                            .objects(objectsToDelete)
                            .build());
            for (Result<DeleteError> result : deleteResults) {
                log.debug("Файл успешно удален: path={}", result.get());
            }
            log.info("Файлы успешно удалены: count={}", objectsToDelete.size());
        } catch (Exception e) {
            log.error("Ошибка при удалении файлов из хранилища", e);
            throw new MinioServiceException("Произошла непредвиденная ошибка при удалении объекта", e);
        }
    }

    @Override
    public boolean checkExistObject(String path) {
        log.info("Проверка наличия объекта в хранилище: path={}", path);
        try {
            minioClient.statObject(StatObjectArgs.builder()
                    .bucket(BUCKET_NAME)
                    .object(path)
                    .build());
            log.info("Проверка завершена, файл найден: path={}", path);
            return true;
        } catch (ErrorResponseException e) {
            if (e.errorResponse().code().equals("NoSuchKey")) {
                log.info("Проверка завершена, файл не найден: path={}", path);
                return false;
            }
            log.error("Ошибка при удалении проверки наличия объекта в хранилище", e);
            throw new MinioServiceException("Ошибка при удалении проверки наличия объекта в хранилище", e);
        } catch (Exception e) {
            log.error("Ошибка при удалении проверки наличия объекта в хранилище: path={}", path, e);
            throw new MinioServiceException("Ошибка при удалении проверки наличия объекта в хранилище", e);
        }
    }

    @Override
    public InputStream downloadObject(String path) {
        log.info("Загрузка файла из хранилища: path={}", path);
        try {
            GetObjectResponse object = minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(BUCKET_NAME)
                            .object(path)
                            .build());
            log.info("Файл успешно загружен: path={}", path);
            return object;
        } catch (Exception e) {
            log.error("Ошибка при загрузки файла из хранилища: path={}", path, e);
            throw new MinioServiceException("Ошибка при загрузки файла из хранилища", e);
        }
    }

    @Override
    public ObjectWriteResponse copyObject(String pathFrom, String pathTo) {
        log.info("Копирование файла: pathFrom={}, pathTo={}", pathFrom, pathTo);
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
            log.info("Объект успешно скопирован: pathFrom={}, pathTo={}", pathFrom, pathTo);
            return response;
        } catch (Exception e) {
            log.error("Ошибка при копировании файла: pathFrom={}, pathTo={}", pathFrom, pathTo, e);
            throw new MinioServiceException("Ошибка при копировании файла", e);
        }
    }

    @Override
    public StatObjectResponse getObjectInfo(String path) {
        log.info("Получение информации об объекте: path={}", path);
        try {
            StatObjectResponse response = minioClient.statObject(StatObjectArgs.builder()
                    .bucket(BUCKET_NAME)
                    .object(path)
                    .build());
            log.info("Информация успешно получение: path={}", path);
            return response;
        } catch (ErrorResponseException e) {
            if (e.errorResponse().code().equals("NoSuchKey")) {
                log.error("Объект не найден: path={}", path, e);
                throw new ResourceNotFoundException("Ресурс не найден", e);
            }
            log.error("Ошибка при получении информации об объекте: path={}", path, e);
            throw new MinioServiceException("Ошибка при получении информации об объекте", e);
        } catch (Exception e) {
            log.error("Ошибка при получении информации об объекте: path={}", path, e);
            throw new MinioServiceException("Ошибка при получении информации об объекте", e);
        }
    }
}
