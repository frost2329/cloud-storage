package com.frostetsky.cloudstorage.service.impl;

import com.frostetsky.cloudstorage.dto.DownloadResultDto;
import com.frostetsky.cloudstorage.dto.ResourceResponse;
import com.frostetsky.cloudstorage.excepiton.ResourceAlreadyExistException;
import com.frostetsky.cloudstorage.excepiton.ResourceNotFoundException;
import com.frostetsky.cloudstorage.excepiton.ResourceServiceException;
import com.frostetsky.cloudstorage.mapper.ResourceMapper;
import com.frostetsky.cloudstorage.service.ResourceService;
import com.frostetsky.cloudstorage.service.S3Service;
import com.frostetsky.cloudstorage.util.ResourcePathUtil;
import io.minio.*;
import io.minio.messages.DeleteObject;
import io.minio.messages.Item;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;


@Slf4j
@Service
@RequiredArgsConstructor
public class ResourceServiceImpl implements ResourceService {

    private final ResourceMapper resourceMapper;
    private final S3Service s3Service;

    @Override
    public List<ResourceResponse> uploadResource(Long userId, String path, MultipartFile[] files) {
        try {
            String basePath = ResourcePathUtil.buildBasePath(userId);
            List<ResourceResponse> resources = new ArrayList<>();
            if (!s3Service.checkExistObject(basePath + path)) {
                log.debug("Upload failed: parent directory does not exist: userId={}, path={}", userId, path);
                throw new ResourceNotFoundException("Родительская папка не существует");
            }
            log.info("Uploading resources: userId={}, path={}, filesCount={}", userId, path, files == null ? 0 : files.length);
            for (MultipartFile file : files) {
                String fullPath = basePath + path + file.getOriginalFilename();
                ObjectWriteResponse response = uploadFile(fullPath, file);
                resources.add(resourceMapper.toDto(response.object(), file.getSize()));
            }
            log.info("Resources uploaded successfully: userId={}, path={}, uploadedCount={}", userId, path, resources.size());
            return resources;
        } catch (ResourceAlreadyExistException | ResourceServiceException | ResourceNotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error while uploading resources: userId={}, path={}", userId, path, e);
            throw new ResourceServiceException("Непредвиденная ошибка при загрузки файлов", e);
        }
    }

    private ObjectWriteResponse uploadFile(String fullPath, MultipartFile file) {
        if (s3Service.checkExistObject(fullPath)) {
            log.debug("Upload failed: resource already exists: fullPath={}", fullPath);
            throw new ResourceAlreadyExistException("Файл уже существует");
        }
        createParentDirectories(fullPath);
        return s3Service.putObject(fullPath, file);
    }


    private void createParentDirectories(String dirPath) {
        List<String> parentDirectories = ResourcePathUtil.getParentDirectories(dirPath);
        for (String parentDirPath : parentDirectories) {
            if (!s3Service.checkExistObject(parentDirPath)) {
                try {
                    log.debug("Creating missing parent directory: path={}", parentDirPath);
                    s3Service.createEmptyDir(parentDirPath);
                } catch (Exception e) {
                    log.error("Failed to create parent directory: path={}", parentDirPath, e);
                    throw new ResourceServiceException("Непредвиденная ошибка создании родительской папки");
                }
            }
        }
    }

    @Override
    public void deleteResource(Long userId, String path) {
        String fullPath = ResourcePathUtil.buildBasePath(userId) + path;
        if (!s3Service.checkExistObject(fullPath)) {
            log.debug("Delete failed: resource not found: userId={}, path={}, fullPath={}", userId, path, fullPath);
            throw new ResourceNotFoundException("Ресурс не найден");
        }
        try {
            log.info("Deleting resource: userId={}, path={}, isDirectory={}", userId, path, ResourcePathUtil.isDirectory(fullPath));
            if (ResourcePathUtil.isDirectory(fullPath)) {
                deleteDirectory(fullPath);
            } else {
                s3Service.deleteObjects(List.of(new DeleteObject(fullPath)));
            }
            log.info("Resource deleted successfully: userId={}, path={}", userId, path);
        } catch (Exception e) {
            log.error("Unexpected error while deleting resource: userId={}, path={}", userId, path, e);
            throw new ResourceServiceException("Непредвиденная ошибка при удалении ресурса", e);
        }
    }

    private void deleteDirectory(String path) {
        List<DeleteObject> objectsToDelete = s3Service.getObjectsInDirectory(path, true)
                .stream()
                .map(item -> new DeleteObject(item.objectName()))
                .toList();
        if (!objectsToDelete.isEmpty()) {
            log.debug("Deleting directory objects: path={}, objectsCount={}", path, objectsToDelete.size());
            s3Service.deleteObjects(objectsToDelete);
        } else {
            log.debug("Directory is empty, nothing to delete: path={}", path);
        }
    }

    @Override
    public ResourceResponse getResourceInfo(Long userId, String path) {
        String fullPath = ResourcePathUtil.buildBasePath(userId) + path;
        try {
            log.debug("Fetching resource info: userId={}, path={}", userId, path);
            StatObjectResponse info = s3Service.getObjectInfo(fullPath);
            return resourceMapper.toDto(info);
        } catch (ResourceNotFoundException e) {
            log.debug("Resource not found while fetching info: userId={}, path={}", userId, path);
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error while fetching resource info: userId={}, path={}", userId, path, e);
            throw new ResourceServiceException("Непредвиденная ошибка при получении информации файла", e);
        }
    }

    @Override
    public DownloadResultDto downloadResource(Long userId, String path) {
        String fullPath = ResourcePathUtil.buildBasePath(userId) + path;
        if (!s3Service.checkExistObject(fullPath)) {
            log.debug("Download failed: resource not found: userId={}, path={}, fullPath={}", userId, path, fullPath);
            throw new ResourceNotFoundException("Ресурс не найден");
        }

        boolean isDir = ResourcePathUtil.isDirectory(fullPath);
        log.info("Downloading resource: userId={}, path={}, isDirectory={}", userId, path, isDir);

        if (isDir) {
            return new DownloadResultDto(
                    ResourcePathUtil.buildZipArchiveName(fullPath),
                    downloadDirectory(fullPath));
        } else {
            return new DownloadResultDto(
                    ResourcePathUtil.extractResourceName(fullPath),
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
                log.error("Streaming download failed: path={}", path, e);
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
            log.debug("Preparing directory download as zip: folderPath={}, objectsCount={}", folderPath, objectsToDownload.size());
            return outputStream -> createZipArchive(folderPath, objectsToDownload, outputStream);
        } catch (Exception e) {
            log.error("Unexpected error while preparing directory download: folderPath={}", folderPath, e);
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
    public ResourceResponse moveResource(Long userId, String pathFrom, String pathTo) {
        pathFrom = ResourcePathUtil.buildBasePath(userId) + pathFrom;
        pathTo = ResourcePathUtil.buildBasePath(userId) + pathTo;
        if (!s3Service.checkExistObject(pathFrom)) {
            log.debug("Move failed: source not found: userId={}, pathFrom={}", userId, pathFrom);
            throw new ResourceNotFoundException("Ресурс не найден");
        }
        if (s3Service.checkExistObject(pathTo)) {
            log.debug("Move failed: target already exists: userId={}, pathTo={}", userId, pathTo);
            throw new ResourceAlreadyExistException("Ресурс, лежащий по пути %s уже существует".formatted(pathTo));
        }

        boolean isDir = ResourcePathUtil.isDirectory(pathTo);
        log.info("Moving resource: userId={}, from={}, to={}, isDirectory={}", userId, pathFrom, pathTo, isDir);

        if (isDir) {
            return moveDirectory(pathFrom, pathTo);
        } else {
            return moveFile(pathFrom, pathTo);
        }
    }

    private ResourceResponse moveDirectory(String pathFrom, String pathTo) {
        try {
            List<Item> items = s3Service.getObjectsInDirectory(pathFrom, true);
            log.debug("Moving directory objects: from={}, to={}, objectsCount={}", pathFrom, pathTo, items.size());
            for (Item item : items) {
                String objectPathFrom = item.objectName();
                String objectPathTo = pathTo + objectPathFrom.substring(pathFrom.length());
                moveFile(objectPathFrom, objectPathTo);
            }
            return resourceMapper.toDto(pathTo, null);
        } catch (Exception e) {
            log.error("Unexpected error while moving directory: from={}, to={}", pathFrom, pathTo, e);
            throw new ResourceServiceException("Непредвиденная ошибка при перемещении папки", e);
        }
    }

    private ResourceResponse moveFile(String pathFrom, String pathTo) {
        try {
            StatObjectResponse info = s3Service.getObjectInfo(pathFrom);
            s3Service.copyObject(pathFrom, pathTo);
            s3Service.deleteObjects(List.of(new DeleteObject(pathFrom)));
            return resourceMapper.toDto(pathTo, info.size());
        } catch (Exception e) {
            log.error("Unexpected error while moving file: from={}, to={}", pathFrom, pathTo, e);
            throw new ResourceServiceException("Непредвиденная ошибка при перемещении файла", e);
        }
    }

    @Override
    public List<ResourceResponse> searchResources(Long userId, String query) {
        try {
            String basePath = ResourcePathUtil.buildBasePath(userId);
            log.debug("Searching resources: userId={}, query={}", userId, query);
            List<Item> items = s3Service.getObjectsInDirectory(basePath, true);
            return items.stream()
                    .filter(item -> {
                        String itemName = ResourcePathUtil.extractResourceName(item.objectName());
                        return itemName.contains(query);
                    })
                    .map(resourceMapper::toDto)
                    .toList();
        } catch (Exception e) {
            log.error("Unexpected error while searching resources: userId={}, query={}", userId, query, e);
            throw new ResourceServiceException("Непредвиденная ошибка при получении данных", e);
        }
    }
}

