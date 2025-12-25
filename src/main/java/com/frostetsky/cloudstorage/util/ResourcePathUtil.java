package com.frostetsky.cloudstorage.util;

import com.frostetsky.cloudstorage.constants.MinioConstants;
import com.frostetsky.cloudstorage.dto.ResourceType;
import org.apache.commons.lang3.StringUtils;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Utility methods for working with resource paths in MinIO.
 * <p>
 * A directory is represented by a path ending with "/".
 * User files are stored under a user base prefix like {@code user-<id>-files/}.
 * Most methods can accept paths with or without this prefix.
 */
public class ResourcePathUtil {

    private static final String BASE_PATH_PREFIX_MASK = "^user-\\d+-files/";

    /**
     * Extracts the last path segment (resource name) from the given path.
     * If a path contains user base prefix, it is removed before extraction.
     * <p>
     * Examples:
     * {@code "user-1-files/folder/file.txt" -> "file.txt"}
     * {@code "user-1-files/folder/" -> "folder/"}
     * {@code "user-1-files/" -> ""}
     *
     * @param path resource path (may include user base prefix)
     * @return resource name, or empty string for root
     */
    public static String extractResourceName(String path) {
        path = removeBasePathPrefix(path);
        if (path.isEmpty()) {
            return "";
        }
        String withoutSlash = StringUtils.removeEnd(path, "/");
        if (!withoutSlash.contains("/")) {
            return path;
        }
        int lastSlash = withoutSlash.lastIndexOf('/');
        return path.substring(lastSlash + 1);
    }

    /**
     * Extracts parent directory path for the given path (always ends with "/").
     * If a path contains user base prefix, it is removed before extraction.
     * <p>
     * Examples:
     * {@code "user-1-files/folder/file.txt" -> "folder/"}
     * {@code "file.txt" -> ""}
     *
     * @param path resource path (may include user base prefix)
     * @return parent directory path, or empty string if parent is root
     */
    public static String extractParentDirectoryPath(String path) {
        path = removeBasePathPrefix(path);
        String withoutSlash = StringUtils.removeEnd(path, "/");
        if (!withoutSlash.contains("/")) {
            return "";
        }
        int lastSlash = withoutSlash.lastIndexOf('/');
        return withoutSlash.substring(0, lastSlash + 1);
    }

    /**
     * Removes user base prefix {@code user-<id>-files/} from the path if present.
     *
     * @param path path with or without user base prefix
     * @return path without user base prefix
     */
    public static String removeBasePathPrefix(String path) {
        return path.replaceFirst(BASE_PATH_PREFIX_MASK, "");
    }

    /**
     * Builds zip archive file name for a directory path.
     *
     * @param path directory path (must end with "/")
     * @return zip name (for root returns {@code "folder.zip"})
     * @throws IllegalArgumentException if path is not a directory
     */
    public static String buildZipArchiveName(String path) {
        if (!path.endsWith("/")) {
            throw new IllegalArgumentException("Переданный путь не является папкой");
        }
        String zipName = extractResourceName(path);
        if (zipName.isEmpty()) {
            return "folder.zip";
        }
        return zipName.substring(0, zipName.length() - 1) + ".zip";
    }

    /**
     * Detects resource type based on path ending: directory ends with "/", file does not.
     *
     * @param path non-empty resource path
     * @return {@link ResourceType#DIRECTORY} or {@link ResourceType#FILE} name
     * @throws IllegalArgumentException if path is empty
     */
    public static String getResourceType(String path) {
        if (path.isEmpty()) {
            throw new IllegalArgumentException("Передан пустой путь");
        }
        return path.endsWith("/")
                ? ResourceType.DIRECTORY.name()
                : ResourceType.FILE.name();
    }

    /**
     * Returns all parent directory paths for the given path.
     * Result paths are normalized to MinIO format ("/" separators) and end with "/".
     * <p>
     * Example:
     * {@code "user-1-files/folder/file.txt" -> ["user-1-files/", "user-1-files/folder/"]}
     *
     * @param dirPath path to file or directory
     * @return ordered list from top-level parent to the nearest parent
     */
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

    /**
     * Converts OS-specific separators to MinIO/S3 key format.
     *
     * @param path input path
     * @return path with "/" separators
     */
    public static String convertPathToMinioFormat(String path) {
        return path.replace("\\", "/");
    }

    /**
     * Builds user base prefix in MinIO bucket.
     *
     * @param userId user id
     * @return base prefix like {@code user-<id>-files/}
     */
    public static String buildBasePath(Long userId) {
        return MinioConstants.USER_BASE_PATH_PATTERN.formatted(userId);
    }

    /**
     * Checks whether path represents a directory (ends with "/").
     *
     * @param path resource path
     * @return true if directory
     */
    public static boolean isDirectory(String path) {
        return path.endsWith("/");
    }
}
