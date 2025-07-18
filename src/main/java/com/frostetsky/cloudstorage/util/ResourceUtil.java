package com.frostetsky.cloudstorage.util;

import com.frostetsky.cloudstorage.dto.ResourceType;
import org.apache.commons.lang3.StringUtils;

public class ResourceUtil {

    public static String getResourceName(String objectName) {
        String objectNameWithoutSlash = StringUtils.removeEnd(objectName, "/");
        int lastSlashIndex = objectNameWithoutSlash.lastIndexOf('/');
        return objectName.substring(lastSlashIndex + 1);
    }

    public static String getParentDirectoryPath(String objectName) {
        String objectNameWithoutSlash = StringUtils.removeEnd(objectName, "/");
        int lastSlashIndex = objectNameWithoutSlash.lastIndexOf('/');
        return objectNameWithoutSlash.substring(0, lastSlashIndex + 1);
    }

    public static String getResourceType(String objectName) {
        return objectName.endsWith("/")
                ? ResourceType.DIRECTORY.name()
                : ResourceType.FILE.name();
    }

}
