package com.frostetsky.cloudstorage.service.impl;

import com.frostetsky.cloudstorage.constants.MinioConstants;
import com.frostetsky.cloudstorage.dto.ResourceDto;
import com.frostetsky.cloudstorage.excepiton.ResourceAlreadyExistException;
import com.frostetsky.cloudstorage.excepiton.ResourceServiceException;
import com.frostetsky.cloudstorage.service.DirectoryService;
import com.frostetsky.cloudstorage.service.ResourceService;
import com.frostetsky.cloudstorage.service.UserService;
import com.frostetsky.cloudstorage.service.props.MinioProperties;
import com.frostetsky.cloudstorage.util.ResourceUtil;
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


@Service
@RequiredArgsConstructor
public class ResourceServiceImpl implements ResourceService {
    private final MinioClient minioClient;
    private final MinioProperties minioProperties;
    private final UserService userService;
    private final DirectoryService directoryService;

    @Override
    public List<ResourceDto> upload(String username, String path, MultipartFile[] object) {
        String userBasePath = MinioConstants.USER_BASE_PATH_PATTERN.formatted(userService.getUserIdByUsername(username));
        List<ResourceDto> resources = new ArrayList<>();
        for (MultipartFile file : object) {
            Path fullPath = Paths.get(userBasePath, path, file.getOriginalFilename());
            if (directoryService.checkExistResource(fullPath.toString().replace("\\", "/"))) {
                throw new ResourceAlreadyExistException("Файл уже существует");
            }
            try {
                createParentDirectories(fullPath);
                ObjectWriteResponse response = minioClient.putObject(PutObjectArgs.builder()
                        .bucket(minioProperties.getBucket())
                        .object(fullPath.toString().replace("\\", "/"))
                        .stream(file.getInputStream(), file.getSize(), -1)
                        .build());
                String resourceName = response.object();
                ResourceDto resourceDto = new ResourceDto(
                        ResourceUtil.getParentDirectoryPath(resourceName),
                        ResourceUtil.getResourceName(resourceName),
                        file.getSize(),
                        ResourceUtil.getResourceType(resourceName));
                resources.add(resourceDto);

            } catch (Exception e) {
                throw new ResourceServiceException("Непредвиденная ошибка при загрузки файлов");
            }
        }
        return resources;
    }

    private void createParentDirectories(Path originalFilename) {
        List<String> parentDirectories = ResourceUtil.getParentDirectories(originalFilename);
        for (String dir : parentDirectories) {
            String dirPath = dir.replace("\\", "/");
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
