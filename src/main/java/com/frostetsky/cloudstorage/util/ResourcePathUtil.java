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

    private static final String BASE_PATH_PREFIX_MASK = "^user-\\d+-files/";

    public static String extractResourceName(String path) {
        path = removeBasePathPrefix(path);
        if(path.isEmpty()) {
            return "";
        }
        String withoutSlash = StringUtils.removeEnd(path, "/");
        if (!withoutSlash.contains("/")) {
            return path;
        }
        int lastSlash = withoutSlash.lastIndexOf('/');
        return path.substring(lastSlash + 1);
    }

    public static String extractParentDirectoryPath(String path) {
        path = removeBasePathPrefix(path);
        String withoutSlash = StringUtils.removeEnd(path, "/");
        if (!withoutSlash.contains("/")) {
            return "";
        }
        int lastSlash = withoutSlash.lastIndexOf('/');
        return withoutSlash.substring(0, lastSlash + 1);
    }

    public static String removeBasePathPrefix(String path) {
        return path.replaceFirst(BASE_PATH_PREFIX_MASK, "");
    }

    public static String buildZipArchiveName(String path) {
        if (!path.endsWith("/")) {
            throw new IllegalArgumentException("Переданный путь не является папкой");
        }
        String zipName = extractResourceName(path);
        if (zipName.isEmpty()) {
            return "folder.zip";
        }
        return  zipName.substring(0, zipName.length() - 1) + ".zip";
    }

    public static String getResourceType(String path) {
        if (path.isEmpty()) {
            throw new IllegalArgumentException("Передан пустой путь");
        }
        return path.endsWith("/")
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
