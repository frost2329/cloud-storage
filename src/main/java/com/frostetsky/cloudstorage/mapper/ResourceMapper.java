package com.frostetsky.cloudstorage.mapper;

import com.frostetsky.cloudstorage.dto.ResourceDto;

import com.frostetsky.cloudstorage.util.MinioPathUtil;
import io.minio.ObjectWriteResponse;
import io.minio.StatObjectResponse;
import io.minio.messages.Item;
import org.springframework.stereotype.Component;

@Component
public class ResourceMapper {
    public ResourceDto toDto(Item object) {
        String objectName = object.objectName();
        return new ResourceDto(
                MinioPathUtil.getParentDirectoryPath(objectName),
                MinioPathUtil.getResourceName(objectName),
                objectName.endsWith("/") ?  null :object.size(),
                MinioPathUtil.getResourceType(objectName));
    }

    public ResourceDto toDto(ObjectWriteResponse response, Long fileSize) {
        String resourceName = response.object();
        return new ResourceDto(
                MinioPathUtil.getParentDirectoryPath(resourceName),
                MinioPathUtil.getResourceName(resourceName),
                fileSize,
                MinioPathUtil.getResourceType(resourceName));
    }

    public ResourceDto toDto(StatObjectResponse info) {
        String object = info.object();
        return new ResourceDto(
                MinioPathUtil.getParentDirectoryPath(object),
                MinioPathUtil.getResourceName(object),
                info.size(),
                MinioPathUtil.getResourceType(object));
    }
}
