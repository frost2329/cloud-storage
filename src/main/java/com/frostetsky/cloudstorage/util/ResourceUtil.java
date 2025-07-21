package com.frostetsky.cloudstorage.util;

import com.frostetsky.cloudstorage.dto.ResourceType;
import org.apache.commons.lang3.StringUtils;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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

    public static List<String> getParentDirectories(Path filePath) {
        Path current = filePath.getParent();
        List<String> parents = new ArrayList<>();
        while (current != null) {
            parents.add(current.toString() + (current.toString().endsWith("\\") ? "" : "\\"));
            current = current.getParent();
        }
        Collections.reverse(parents);
        return parents;
    }

}
