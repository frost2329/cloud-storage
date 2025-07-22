package com.frostetsky.cloudstorage.service.impl;

import com.frostetsky.cloudstorage.dto.ResourceDto;
import com.frostetsky.cloudstorage.excepiton.InvalidPathException;
import com.frostetsky.cloudstorage.excepiton.ResourceAlreadyExistException;
import com.frostetsky.cloudstorage.excepiton.ResourceNotFoundException;
import com.frostetsky.cloudstorage.excepiton.ResourceServiceException;
import com.frostetsky.cloudstorage.mapper.ResourceMapper;
import com.frostetsky.cloudstorage.service.ResourceService;
import com.frostetsky.cloudstorage.service.S3Service;
import com.frostetsky.cloudstorage.service.UserService;
import com.frostetsky.cloudstorage.util.MinioPathUtil;
import io.minio.*;
import io.minio.messages.DeleteObject;
import io.minio.messages.Item;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;


@Service
@RequiredArgsConstructor
public class ResourceServiceImpl implements ResourceService {

    private final UserService userService;
    private final ResourceMapper resourceMapper;
    private final S3Service s3Service;

    @Override
    public List<ResourceDto> upload(String username, String path, MultipartFile[] object) {
        try {
            String basePath = MinioPathUtil.buildBasePath(userService.getUserIdByUsername(username));
            List<ResourceDto> resources = new ArrayList<>();
            for (MultipartFile file : object) {
                String dirPath = basePath + path + file.getOriginalFilename();
                if (s3Service.checkExistObject(dirPath)) {
                    throw new ResourceAlreadyExistException("Файл уже существует");
                }
                createParentDirectories(dirPath);
                ObjectWriteResponse response = s3Service.putObject(dirPath, file);
                resources.add(resourceMapper.toDto(response, file.getSize()));
            }
            return resources;
        } catch (ResourceAlreadyExistException | ResourceServiceException e) {
            throw e;
        } catch (Exception e) {
            throw new ResourceServiceException("Непредвиденная ошибка при загрузки файлов", e);
        }
    }


    private void createParentDirectories(String dirPath) {
        List<String> parentDirectories = MinioPathUtil.getParentDirectories(dirPath);
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
    public void delete(String path) {
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
}
