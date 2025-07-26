package com.frostetsky.cloudstorage.mapper;

import com.frostetsky.cloudstorage.dto.ResourceResponse;

import com.frostetsky.cloudstorage.util.ResourcePathUtil;
import io.minio.StatObjectResponse;
import io.minio.messages.Item;
import org.springframework.stereotype.Component;

@Component
public class ResourceMapper {
    public ResourceResponse toDto(Item object) {
        return toDto(object.objectName(), object.size());
    }

    public ResourceResponse toDto(StatObjectResponse info) {
        return toDto(info.object(), info.size());
    }

    public ResourceResponse toDto(String path, Long fileSize) {
        return new ResourceResponse(
                ResourcePathUtil.getParentDirectoryPath(path),
                ResourcePathUtil.getResourceName(path),
                ResourcePathUtil.isDirectory(path) ? null : fileSize,
                ResourcePathUtil.getResourceType(path));
    }
}
