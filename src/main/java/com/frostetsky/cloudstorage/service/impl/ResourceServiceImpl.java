package com.frostetsky.cloudstorage.service.impl;

import com.frostetsky.cloudstorage.dto.DownloadResultDto;
import com.frostetsky.cloudstorage.dto.ResourceResponse;
import com.frostetsky.cloudstorage.excepiton.InvalidParamException;
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
    public List<ResourceResponse> upload(String username, String path, MultipartFile[] objects) {
        try {
            String basePath = ResourcePathUtil.buildBasePath(userService.getUserIdByUsername(username));
            List<ResourceResponse> resources = new ArrayList<>();
            for (MultipartFile file : objects) {
                String fullPath = basePath + path + file.getOriginalFilename();
                if (s3Service.checkExistObject(fullPath)) {
                    throw new ResourceAlreadyExistException("Файл уже существует");
                }
                createParentDirectories(fullPath);
                ObjectWriteResponse response = s3Service.putObject(fullPath, file);
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
    public void deleteResource(String username, String path) {
        if (path == null || path.isEmpty()) {
            throw new InvalidParamException("Не передан path");
        }
        String basePath = ResourcePathUtil.buildBasePath(userService.getUserIdByUsername(username));
        String fullPath = basePath + path;
        if (!s3Service.checkExistObject(fullPath)) {
            throw new ResourceNotFoundException("Ресурс не найден");
        }
        try {
            List<DeleteObject> objectsToDelete = s3Service.getObjectsInDirectory(fullPath, true)
                    .stream()
                    .map(item -> new DeleteObject(item.objectName()))
                    .toList();
            if (!objectsToDelete.isEmpty()) {
                s3Service.deleteObjects(objectsToDelete);
            }
        } catch (Exception e) {
            throw new ResourceServiceException("Непредвиденная ошибка при удалении файла", e);
        }
    }

    @Override
    public ResourceResponse getResourceInfo(String username, String path) {
        if (path == null || path.isEmpty()) {
            throw new InvalidParamException("Не передан path");
        }
        String basePath = ResourcePathUtil.buildBasePath(userService.getUserIdByUsername(username));
        String fullPath = basePath + path;
        try {
            StatObjectResponse info = s3Service.getObjectInfo(fullPath);
            return resourceMapper.toDto(info);
        } catch (ResourceNotFoundException e) {
            throw e;
        } catch (Exception e) {
            throw new ResourceServiceException("Непредвиденная ошибка при получении информации файла", e);
        }
    }

    @Override
    public DownloadResultDto downloadResource(String username, String path) {
        if (path == null || path.isEmpty()) {
            throw new InvalidParamException("Не передан path");
        }
        String basePath = ResourcePathUtil.buildBasePath(userService.getUserIdByUsername(username));
        String fullPath = basePath + path;
        if (!s3Service.checkExistObject(fullPath)) {
            throw new ResourceNotFoundException("Ресурс не найден");
        }

        if (ResourcePathUtil.isDirectory(fullPath)) {
            return new DownloadResultDto(
                    ResourcePathUtil.buildZipArchiveName(fullPath),
                    downloadDirectory(fullPath));
        } else {
            return new DownloadResultDto(
                    ResourcePathUtil.getResourceName(fullPath),
                    downloadFile(fullPath));
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
            List<Item> items = s3Service.getObjectsInDirectory(folderPath, true);
            List<String> objectsToDownload = items.stream()
                    .map(Item::objectName)
                    .toList();
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
    public ResourceResponse moveResource(String username, String pathFrom, String pathTo) {
        if (pathFrom == null || pathFrom.isEmpty()) {
            throw new InvalidParamException("Не передан pathFrom");
        }
        if (pathTo == null || pathTo.isEmpty()) {
            throw new InvalidParamException("Не передан pathTo");
        }
        String basePath = ResourcePathUtil.buildBasePath(userService.getUserIdByUsername(username));
        pathFrom = basePath + pathFrom;
        pathTo = basePath + pathTo;

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

    private ResourceResponse moveDirectory(String pathFrom, String pathTo) {
        try {
            List<Item> items = s3Service.getObjectsInDirectory(pathFrom, true);
            for (Item item : items) {
                String objectPathFrom = item.objectName();
                String objectPathTo = pathTo + objectPathFrom.substring(pathFrom.length());
                moveFile(objectPathFrom, objectPathTo);
            }
            return resourceMapper.toDto(pathTo,null);
        } catch (Exception e) {
            throw new ResourceServiceException("Непредвиденная ошибка при перемещении папки", e);
        }
    }

    private ResourceResponse moveFile(String pathFrom, String pathTo) {
        try {
            StatObjectResponse info = s3Service.getObjectInfo(pathFrom);
            minioS3ServiceImpl.copyObject(pathFrom, pathTo);
            s3Service.deleteObjects(List.of(new DeleteObject(pathFrom)));
            return resourceMapper.toDto(pathTo, info.size());
        } catch (Exception e) {
            throw new ResourceServiceException("Непредвиденная ошибка при перемещении файла", e);
        }
    }

    @Override
    public List<ResourceResponse> searchResources(String username, String query) {
        if (query == null || query.isEmpty()) {
            throw new InvalidParamException("Невалидный или отсутствующий поисковый запрос");
        }
        try {
            String basePath = ResourcePathUtil.buildBasePath(userService.getUserIdByUsername(username));
            List<Item> items = minioS3ServiceImpl.getObjectsInDirectory(basePath, true);
            return items.stream()
                    .filter(item -> ResourcePathUtil.getResourceName(item.objectName()).contains(query))
                    .map(resourceMapper::toDto)
                    .toList();
        } catch (Exception e) {
            throw new ResourceServiceException("Непредвиденная ошибка при получении данных", e);
        }
    }
}
