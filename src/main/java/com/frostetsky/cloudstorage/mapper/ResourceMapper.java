package com.frostetsky.cloudstorage.mapper;

import com.frostetsky.cloudstorage.dto.ResourceDto;

import com.frostetsky.cloudstorage.util.ResourceUtil;
import io.minio.messages.Item;
import org.springframework.stereotype.Component;

@Component
public class ResourceMapper {
    public ResourceDto toDto(Item object) {
        String objectName = object.objectName();
        return new ResourceDto(
                ResourceUtil.getParentDirectoryPath(objectName),
                ResourceUtil.getResourceName(objectName),
                objectName.endsWith("/") ?  null :object.size(),
                ResourceUtil.getResourceType(objectName));
    }
}
