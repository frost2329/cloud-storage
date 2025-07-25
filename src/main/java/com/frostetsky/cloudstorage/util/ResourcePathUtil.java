package com.frostetsky.cloudstorage.util;

import com.frostetsky.cloudstorage.constants.MinioConstants;
import com.frostetsky.cloudstorage.dto.ResourceType;
import org.apache.commons.lang3.StringUtils;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ResourcePathUtil {

    public static String getResourceName(String objectName) {
        String objectNameWithoutSlash = StringUtils.removeEnd(objectName, "/");
        int lastSlashIndex = objectNameWithoutSlash.lastIndexOf('/');
        return objectName.substring(lastSlashIndex + 1);
    }

    public static String buildZipArchiveName(String objectName) {
        String zipName = ResourcePathUtil.getResourceName(objectName);
        zipName = objectName.endsWith("/")
                ? objectName.substring(0, objectName.length() - 1)
                : objectName;
        return  zipName + ".zip";
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

    public static List<String> getParentDirectories(String dirPath) {
        Path currentPath = Paths.get(dirPath).getParent();
        List<String> parentsPaths = new ArrayList<>();
        while (currentPath != null) {
            parentsPaths.add(convertPathToMinioFormat(currentPath.toString()) + "/");
            currentPath = currentPath.getParent();
        }
        Collections.reverse(parentsPaths);
        return parentsPaths;
    }

    public static String convertPathToMinioFormat(String path) {
        return path.replace("\\", "/");
    }

    public static String buildBasePath(Long userId) {
        return MinioConstants.USER_BASE_PATH_PATTERN.formatted(userId);
    }

    public static boolean isDirectory(String path) {
        return path.endsWith("/");
    }
}
