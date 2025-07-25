package com.frostetsky.cloudstorage.mapper;

import com.frostetsky.cloudstorage.dto.ResourceDto;

import com.frostetsky.cloudstorage.util.ResourcePathUtil;
import io.minio.StatObjectResponse;
import io.minio.messages.Item;
import org.springframework.stereotype.Component;

@Component
public class ResourceMapper {
    public ResourceDto toDto(Item object) {
        String objectName = object.objectName();
        return new ResourceDto(
                ResourcePathUtil.getParentDirectoryPath(objectName),
                ResourcePathUtil.getResourceName(objectName),
                objectName.endsWith("/") ?  null :object.size(),
                ResourcePathUtil.getResourceType(objectName));
    }

    public ResourceDto toDto(String resourcePathName, Long fileSize) {
        return new ResourceDto(
                ResourcePathUtil.getParentDirectoryPath(resourcePathName),
                ResourcePathUtil.getResourceName(resourcePathName),
                fileSize,
                ResourcePathUtil.getResourceType(resourcePathName));
    }

    public ResourceDto toDto(StatObjectResponse info) {
        String object = info.object();
        return new ResourceDto(
                ResourcePathUtil.getParentDirectoryPath(object),
                ResourcePathUtil.getResourceName(object),
                info.size(),
                ResourcePathUtil.getResourceType(object));
    }
}
