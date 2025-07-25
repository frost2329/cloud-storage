package com.frostetsky.cloudstorage.service.impl;

import com.frostetsky.cloudstorage.dto.DownloadResultDto;
import com.frostetsky.cloudstorage.dto.ResourceDto;
import com.frostetsky.cloudstorage.excepiton.InvalidPathException;
import com.frostetsky.cloudstorage.excepiton.ResourceAlreadyExistException;
import com.frostetsky.cloudstorage.excepiton.ResourceNotFoundException;
import com.frostetsky.cloudstorage.excepiton.ResourceServiceException;
import com.frostetsky.cloudstorage.mapper.ResourceMapper;
import com.frostetsky.cloudstorage.service.ResourceService;
import com.frostetsky.cloudstorage.service.S3Service;
import com.frostetsky.cloudstorage.service.UserService;
import com.frostetsky.cloudstorage.util.ResourcePathUtil;
import io.minio.*;
import io.minio.messages.DeleteObject;
import io.minio.messages.Item;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;


@Service
@RequiredArgsConstructor
public class ResourceServiceImpl implements ResourceService {

    private final UserService userService;
    private final ResourceMapper resourceMapper;
    private final S3Service s3Service;
    private final MinioS3ServiceImpl minioS3ServiceImpl;

    @Override
    public List<ResourceDto> upload(String username, String path, MultipartFile[] object) {
        try {
            String basePath = ResourcePathUtil.buildBasePath(userService.getUserIdByUsername(username));
            List<ResourceDto> resources = new ArrayList<>();
            for (MultipartFile file : object) {
                String dirPath = basePath + path + file.getOriginalFilename();
                if (s3Service.checkExistObject(dirPath)) {
                    throw new ResourceAlreadyExistException("Файл уже существует");
                }
                createParentDirectories(dirPath);
                ObjectWriteResponse response = s3Service.putObject(dirPath, file);
                resources.add(resourceMapper.toDto(response.object(), file.getSize()));
            }
            return resources;
        } catch (ResourceAlreadyExistException | ResourceServiceException e) {
            throw e;
        } catch (Exception e) {
            throw new ResourceServiceException("Непредвиденная ошибка при загрузки файлов", e);
        }
    }


    private void createParentDirectories(String dirPath) {
        List<String> parentDirectories = ResourcePathUtil.getParentDirectories(dirPath);
        for (String parentDirPath : parentDirectories) {
            if (!s3Service.checkExistObject(parentDirPath)) {
                try {
                    s3Service.createEmptyDir(parentDirPath);
                } catch (Exception e) {
                    throw new ResourceServiceException("Непредвиденная ошибка создании родительской папки");
                }
            }
        }
    }

    @Override
    public void deleteResource(String path) {
        if (path == null || path.isEmpty()) {
            throw new InvalidPathException("Не передан path");
        }
        if (!s3Service.checkExistObject(path)) {
            throw new ResourceNotFoundException("Ресурс не найден");
        }
        try {
            List<DeleteObject> objectsToDelete = new ArrayList<>();
            var results = s3Service.getObjectsInDirectory(path, true);
            for (Result<Item> result : results) {
                objectsToDelete.add(new DeleteObject(result.get().objectName()));
            }
            if (!objectsToDelete.isEmpty()) {
                s3Service.deleteObjects(objectsToDelete);
            }
        } catch (Exception e) {
            throw new ResourceServiceException("Непредвиденная ошибка при удалении файла", e);
        }
    }

    @Override
    public ResourceDto getResourceInfo(String path) {
        if (path == null || path.isEmpty()) {
            throw new InvalidPathException("Не передан path");
        }
        try {
            StatObjectResponse info = s3Service.getObjectInfo(path);
            return resourceMapper.toDto(info);
        } catch (ResourceNotFoundException e) {
            throw e;
        } catch (Exception e) {
            throw new ResourceServiceException("Непредвиденная ошибка при получении информации файла", e);
        }
    }

    @Override
    public DownloadResultDto downloadResource(String path) {
        if (path == null || path.isEmpty()) {
            throw new InvalidPathException("Не передан path");
        }
        if (!s3Service.checkExistObject(path)) {
            throw new ResourceNotFoundException("Ресурс не найден");
        }

        if (ResourcePathUtil.isDirectory(path)) {
            return new DownloadResultDto(
                    ResourcePathUtil.buildZipArchiveName(path),
                    downloadDirectory(path));
        } else {
            return new DownloadResultDto(
                    ResourcePathUtil.getResourceName(path),
                    downloadFile(path));
        }
    }

    private StreamingResponseBody downloadFile(String path) {
        InputStream stream = s3Service.downloadObject(path);
        return outputStream -> {
            try (stream) {
                byte[] buffer = new byte[8192];
                int bytesRead;
                while ((bytesRead = stream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
            } catch (Exception e) {
                throw new ResourceServiceException("Ошибка при потоковой передаче файла", e);
            }
        };
    }


    private StreamingResponseBody downloadDirectory(String folderPath) {
        try {
            Iterable<Result<Item>> results = s3Service.getObjectsInDirectory(folderPath, true);
            List<String> objectsToDownload = new ArrayList<>();
            for (Result<Item> result : results) {
                objectsToDownload.add(result.get().objectName());
            }
            return outputStream -> createZipArchive(folderPath, objectsToDownload, outputStream);
        } catch (Exception e) {
            throw new ResourceServiceException("Непредвиденная ошибка при загрузки папки", e);
        }
    }

    @SneakyThrows
    private void createZipArchive(String folderPath, List<String> objectsToDownload, OutputStream outputStream) {
        try (ZipOutputStream zipOut = new ZipOutputStream(outputStream)) {
            if (!folderPath.endsWith("/")) {
                folderPath += "/";
            }
            for (String objectPath : objectsToDownload) {
                if (ResourcePathUtil.isDirectory(objectPath)) {
                    continue;
                }
                String fileName = objectPath.substring(folderPath.length());
                zipOut.putNextEntry(new ZipEntry(fileName));
                try (InputStream stream = s3Service.downloadObject(objectPath)) {
                    int bytesRead;
                    byte[] buffer = new byte[8192];
                    while ((bytesRead = stream.read(buffer)) != -1) {
                        zipOut.write(buffer, 0, bytesRead);
                    }
                }
                zipOut.closeEntry();
            }
            zipOut.finish();
        }
    }

    @Override
    public ResourceDto moveResource(String pathFrom, String pathTo) {
        if (pathFrom == null || pathFrom.isEmpty()) {
            throw new InvalidPathException("Не передан path");
        }
        if (!s3Service.checkExistObject(pathFrom)) {
            throw new ResourceNotFoundException("Ресурс не найден");
        }

        if (s3Service.checkExistObject(pathTo)) {
            throw new ResourceNotFoundException("Ресурс, лежащий по пути %s уже существует".formatted(pathTo));
        }
        if (ResourcePathUtil.isDirectory(pathTo)) {
            return moveDirectory(pathFrom, pathTo);
        } else {
            return moveFile(pathFrom, pathTo);
        }
    }

    private ResourceDto moveFile(String pathFrom, String pathTo) {
        try {
            StatObjectResponse info = s3Service.getObjectInfo(pathFrom);
            minioS3ServiceImpl.copyObject(pathFrom, pathTo);
            List<DeleteObject> deleteObjects = new ArrayList<>(List.of(new DeleteObject(pathFrom)));
            s3Service.deleteObjects(deleteObjects);
            return resourceMapper.toDto(pathTo,info.size());
        } catch (Exception e) {
            throw new ResourceServiceException("Непредвиденная ошибка при перемещении файла", e);
        }
    }

    private ResourceDto moveDirectory(String pathFrom, String pathTo) {
        try {
            List<DeleteObject> objectsToDelete = new ArrayList<>();
            Iterable<Result<Item>> results = s3Service.getObjectsInDirectory(pathFrom, true);
            for (Result<Item> result : results) {
                String objectPathFrom = result.get().objectName();
                String objectPathTo = pathTo + objectPathFrom.substring(pathFrom.length());
                minioS3ServiceImpl.copyObject(objectPathFrom, objectPathTo);
                objectsToDelete.add(new DeleteObject(objectPathFrom));
            }
            if (!objectsToDelete.isEmpty()) {
                s3Service.deleteObjects(objectsToDelete);
            }
            return resourceMapper.toDto(pathTo,null);
        } catch (Exception e) {
            throw new ResourceServiceException("Непредвиденная ошибка при перемещении папки", e);
        }
    }
}
