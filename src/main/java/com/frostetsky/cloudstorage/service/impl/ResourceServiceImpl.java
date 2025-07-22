package com.frostetsky.cloudstorage.service.impl;

import com.frostetsky.cloudstorage.dto.ResourceDto;
import com.frostetsky.cloudstorage.excepiton.ResourceAlreadyExistException;
import com.frostetsky.cloudstorage.excepiton.ResourceServiceException;
import com.frostetsky.cloudstorage.mapper.ResourceMapper;
import com.frostetsky.cloudstorage.service.DirectoryService;
import com.frostetsky.cloudstorage.service.ResourceService;
import com.frostetsky.cloudstorage.service.UserService;
import com.frostetsky.cloudstorage.util.MinioPathUtil;
import io.minio.MinioClient;
import io.minio.ObjectWriteResponse;
import io.minio.PutObjectArgs;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static com.frostetsky.cloudstorage.constants.MinioConstants.*;

@Service
@RequiredArgsConstructor
public class ResourceServiceImpl implements ResourceService {

    private final MinioClient minioClient;
    private final UserService userService;
    private final DirectoryService directoryService;
    private final ResourceMapper resourceMapper;

    @Override
    public List<ResourceDto> upload(String username, String path, MultipartFile[] object) {
        try {
            String basePath = MinioPathUtil.buildBasePath(userService.getUserIdByUsername(username));
            List<ResourceDto> resources = new ArrayList<>();
            for (MultipartFile file : object) {
                Path fullPath = Paths.get(basePath, path, file.getOriginalFilename());
                if (directoryService.checkExistResource(MinioPathUtil.convertPathToMinioFormat(fullPath.toString()))) {
                    throw new ResourceAlreadyExistException("Файл уже существует");
                }
                createParentDirectories(fullPath);
                ObjectWriteResponse response = uploadToMinio(MinioPathUtil.convertPathToMinioFormat(fullPath.toString()), file);
                resources.add(resourceMapper.toDto(response, file.getSize()));
            }
            return resources;
        } catch (ResourceAlreadyExistException | ResourceServiceException e) {
            throw e;
        } catch (Exception e) {
            throw new ResourceServiceException("Непредвиденная ошибка при загрузки файлов", e);
        }
    }

    private ObjectWriteResponse uploadToMinio(String path, MultipartFile file) {
        try {
            return minioClient.putObject(PutObjectArgs.builder()
                    .bucket(BUCKET_NAME)
                    .object(MinioPathUtil.convertPathToMinioFormat(path))
                    .stream(file.getInputStream(), file.getSize(), PART_SIZE)
                    .build());
        } catch (Exception e) {
            throw new ResourceServiceException("Непредвиденная ошибка при загрузки файла", e);
        }
    }

    private void createParentDirectories(Path originalFilename) {
        List<String> parentDirectories = MinioPathUtil.getParentDirectories(originalFilename);
        for (String dir : parentDirectories) {
            String dirPath = MinioPathUtil.convertPathToMinioFormat(dir);
            if (!directoryService.checkExistResource(dirPath)) {
                try {
                    directoryService.createEmptyDirectory(dirPath);
                } catch (Exception e) {
                    throw new ResourceServiceException("Непредвиденная ошибка создании родительской папки");
                }

            }
        }
    }
}
