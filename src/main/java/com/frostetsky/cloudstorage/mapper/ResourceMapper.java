package com.frostetsky.cloudstorage.mapper;

import com.frostetsky.cloudstorage.dto.ResourceDto;

import com.frostetsky.cloudstorage.dto.ResourceType;
import io.minio.messages.Item;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

@Component
public class ResourceMapper {
    public ResourceDto toDto(Item object) {
        String objectName = object.objectName();
        return new ResourceDto(
                getResourcePath(objectName),
                getResourceName(objectName),
                object.size(),
                objectName.endsWith("/") ? ResourceType.DIRECTORY.name() : ResourceType.FILE.name());
    }

    private String getResourceName(String objectName) {
        String objectNameWithoutSlash = StringUtils.removeEnd(objectName, "/");
        int lastSlashIndex = objectNameWithoutSlash.lastIndexOf('/');
        return objectName.substring(lastSlashIndex + 1);
    }

    private String getResourcePath(String objectName) {
        String objectNameWithoutSlash = StringUtils.removeEnd(objectName, "/");
        int lastSlashIndex = objectNameWithoutSlash.lastIndexOf('/');
        return objectNameWithoutSlash.substring(0, lastSlashIndex + 1);
    }
}
