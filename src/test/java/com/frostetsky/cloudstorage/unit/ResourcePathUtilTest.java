package com.frostetsky.cloudstorage.unit;

import com.frostetsky.cloudstorage.dto.ResourceType;
import com.frostetsky.cloudstorage.integration.config.TestConfig;
import com.frostetsky.cloudstorage.util.ResourcePathUtil;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;


@SpringBootTest(classes = TestConfig.class)
public class ResourcePathUtilTest {

    @Test
    void testExtractResourceName() {
        assertEquals("", ResourcePathUtil.extractResourceName(""));
        assertEquals("", ResourcePathUtil.extractResourceName("user-1-files/"));
        assertEquals("folder/", ResourcePathUtil.extractResourceName("user-1-files/folder/"));
        assertEquals("folder", ResourcePathUtil.extractResourceName("user-1-files/folder"));
        assertEquals("file.txt", ResourcePathUtil.extractResourceName("user-1-files/folder/file.txt"));
        assertEquals("file.txt", ResourcePathUtil.extractResourceName("folder/file.txt"));
        assertEquals("file.txt", ResourcePathUtil.extractResourceName("test/fsfs/test/folder/file.txt"));
        assertEquals("file.txt", ResourcePathUtil.extractResourceName("file.txt"));
        assertEquals("folder/", ResourcePathUtil.extractResourceName("folder/"));
        assertEquals("folder", ResourcePathUtil.extractResourceName("folder"));
    }

    @Test
    void testExtractParentDirectoryPath() {
        assertEquals("", ResourcePathUtil.extractParentDirectoryPath(""));
        assertEquals("", ResourcePathUtil.extractParentDirectoryPath("user-1-files/"));
        assertEquals("", ResourcePathUtil.extractParentDirectoryPath("user-1-files/folder/"));
        assertEquals("", ResourcePathUtil.extractParentDirectoryPath("user-1-files/folder"));
        assertEquals("folder/", ResourcePathUtil.extractParentDirectoryPath("user-1-files/folder/file.txt"));
        assertEquals("folder/", ResourcePathUtil.extractParentDirectoryPath("folder/file.txt"));
        assertEquals("test/fsfs/test/folder/", ResourcePathUtil.extractParentDirectoryPath("test/fsfs/test/folder/file.txt"));
        assertEquals("", ResourcePathUtil.extractParentDirectoryPath("file.txt"));
        assertEquals("", ResourcePathUtil.extractParentDirectoryPath("folder/"));
        assertEquals("", ResourcePathUtil.extractParentDirectoryPath("folder"));
    }

    @Test
    void testRemoveBasePathPrefix() {
        assertEquals("", ResourcePathUtil.removeBasePathPrefix(""));
        assertEquals("", ResourcePathUtil.removeBasePathPrefix("user-1-files/"));
        assertEquals("folder/", ResourcePathUtil.removeBasePathPrefix("user-1-files/folder/"));
        assertEquals("folder", ResourcePathUtil.removeBasePathPrefix("user-1-files/folder"));
        assertEquals("folder/file.txt", ResourcePathUtil.removeBasePathPrefix("user-1-files/folder/file.txt"));
        assertEquals("folder/file.txt", ResourcePathUtil.removeBasePathPrefix("folder/file.txt"));
        assertEquals("test/fsfs/test/folder/file.txt", ResourcePathUtil.removeBasePathPrefix("test/fsfs/test/folder/file.txt"));
        assertEquals("file.txt", ResourcePathUtil.removeBasePathPrefix("file.txt"));
        assertEquals("folder/", ResourcePathUtil.removeBasePathPrefix("folder/"));
        assertEquals("folder", ResourcePathUtil.removeBasePathPrefix("folder"));
    }

    @Test
    void testBuildZipArchiveName() {
        assertEquals("folder.zip", ResourcePathUtil.buildZipArchiveName("user-1-files/"));
        assertEquals("folder.zip", ResourcePathUtil.buildZipArchiveName("user-1-files/folder/"));
        assertEquals("folder.zip", ResourcePathUtil.buildZipArchiveName("folder/"));
        assertThrows(IllegalArgumentException.class, () -> ResourcePathUtil.buildZipArchiveName(""));
        assertThrows(IllegalArgumentException.class, () -> ResourcePathUtil.buildZipArchiveName("folder"));
        assertThrows(IllegalArgumentException.class, () -> ResourcePathUtil.buildZipArchiveName("file.txt"));
        assertThrows(IllegalArgumentException.class, () -> ResourcePathUtil.buildZipArchiveName("user-1-files/folder/file.txt"));
        assertThrows(IllegalArgumentException.class, () -> ResourcePathUtil.buildZipArchiveName("folder/file.txt"));
        assertThrows(IllegalArgumentException.class, () -> ResourcePathUtil.buildZipArchiveName("test/fsfs/test/folder/file.txt"));
        assertThrows(IllegalArgumentException.class, () -> ResourcePathUtil.buildZipArchiveName("user-1-files/folder"));
    }

    @Test
    void testGetResourceType() {
        assertThrows(IllegalArgumentException.class, () -> ResourcePathUtil.getResourceType(""));
        assertEquals(ResourceType.DIRECTORY.name(), ResourcePathUtil.getResourceType("user-1-files/"));
        assertEquals(ResourceType.DIRECTORY.name(), ResourcePathUtil.getResourceType("user-1-files/folder/"));
        assertEquals(ResourceType.FILE.name(), ResourcePathUtil.getResourceType("user-1-files/folder"));
        assertEquals(ResourceType.FILE.name(), ResourcePathUtil.getResourceType("user-1-files/folder/file.txt"));
        assertEquals(ResourceType.FILE.name(), ResourcePathUtil.getResourceType("folder/file.txt"));
        assertEquals(ResourceType.FILE.name(), ResourcePathUtil.getResourceType("test/fsfs/test/folder/file.txt"));
        assertEquals(ResourceType.FILE.name(), ResourcePathUtil.getResourceType("file.txt"));
        assertEquals(ResourceType.DIRECTORY.name(), ResourcePathUtil.getResourceType("folder/"));
        assertEquals(ResourceType.FILE.name(), ResourcePathUtil.getResourceType("folder"));
    }

    @Test
    void testGetParentDirectories() {
        List<String> parents = ResourcePathUtil.getParentDirectories("user-1-files/folder/file.txt");
        assertEquals("user-1-files/", parents.get(0));
        assertEquals("user-1-files/folder/", parents.get(1));
    }

    @Test
    void testConvertPathToMinioFormat() {
        assertEquals("user-1-files/", ResourcePathUtil.convertPathToMinioFormat("user-1-files\\"));
        assertEquals("user-1-files/folder/", ResourcePathUtil.convertPathToMinioFormat("user-1-files\\folder\\"));
        assertEquals("user-1-files/folder/", ResourcePathUtil.convertPathToMinioFormat("user-1-files/folder/"));
        assertEquals("file.txt", ResourcePathUtil.convertPathToMinioFormat("file.txt"));
        assertEquals("folder/", ResourcePathUtil.convertPathToMinioFormat("folder/"));
        assertEquals("folder", ResourcePathUtil.removeBasePathPrefix("folder"));
    }

    @Test
    void testBuildPath() {
        assertEquals("user-1-files/", ResourcePathUtil.buildBasePath(1L));
        assertEquals("user-2-files/", ResourcePathUtil.buildBasePath(2L));
    }

    @Test
    void isDirectory() {
        assertTrue(ResourcePathUtil.isDirectory("user-1-files/"));
        assertFalse(ResourcePathUtil.isDirectory("folder"));
        assertFalse(ResourcePathUtil.isDirectory("file.txt"));
        assertFalse(ResourcePathUtil.isDirectory(""));
    }
}
