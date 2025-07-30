package com.frostetsky.cloudstorage.integration.service;

import com.frostetsky.cloudstorage.dto.DownloadResultDto;
import com.frostetsky.cloudstorage.dto.ResourceResponse;
import com.frostetsky.cloudstorage.excepiton.ResourceAlreadyExistException;
import com.frostetsky.cloudstorage.excepiton.ResourceNotFoundException;
import com.frostetsky.cloudstorage.integration.config.TestConfig;
import com.frostetsky.cloudstorage.service.ResourceService;
import com.frostetsky.cloudstorage.service.S3Service;
import com.frostetsky.cloudstorage.util.ResourcePathUtil;
import io.minio.messages.DeleteObject;
import io.minio.messages.Item;
import org.junit.jupiter.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.multipart.MultipartFile;
import org.testcontainers.junit.jupiter.Testcontainers;
import java.util.List;

import static com.frostetsky.cloudstorage.integration.util.FileUtil.createTestFile;
import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
@SpringBootTest(classes = TestConfig.class)
public class ResourceServiceTest {
    @Autowired
    private ResourceService resourceService;
    @Autowired
    private S3Service s3Service;

    private static final Logger log = LoggerFactory.getLogger(ResourceServiceTest.class);
    private static final Long TEST_USER_ID = 10L;
    private static final String BASE_PATCH = ResourcePathUtil.buildBasePath(TEST_USER_ID);


    @BeforeAll
    static void setup(@Autowired S3Service s3Service) {
        log.info("===== BEFORE_ALL =====");
        try {
            if (!s3Service.checkBaseBucketExists()) {
                s3Service.createBaseBucket();
            }
            s3Service.createEmptyDir(BASE_PATCH);
        } catch (Exception e) {
            throw new RuntimeException("Failed to setup bucket", e);
        }
    }

    @BeforeEach
    void logTestStart(TestInfo testInfo) {
        log.info("==========>  STARTING TEST: {}", testInfo.getDisplayName());
    }


    @Test
    void uploadResource_Test() {
        // Загрузка файла
        MultipartFile file = createTestFile("dir1/test.txt", "Hello Test");
        ResourceResponse response = resourceService.uploadResource(TEST_USER_ID, "", new MultipartFile[]{file})
                .get(0);
        List<Item> objects = s3Service.getObjectsInDirectory(ResourcePathUtil.buildBasePath(TEST_USER_ID), true);
        assertTrue(objects.stream()
                .anyMatch(
                item -> item.objectName().equals(BASE_PATCH + response.path() + response.name())));

        // Загрузка файлов в несуществующую директорию
        MultipartFile[] files = {
                createTestFile("dir1/test.txt", "Hello Test"),
                createTestFile("dir1/test2.txt", "Hello Test 2"),
        };
        assertThrows(ResourceNotFoundException.class,() ->
                resourceService.uploadResource(TEST_USER_ID, "dir0/", files));

        // Загрузка файлов с одинаковым именем
        MultipartFile[] doubleFiles = {
                createTestFile("dir1/file.txt", "Hello Test"),
                createTestFile("dir1/file.txt", "Hello Test 2"),
        };
        assertThrows(ResourceAlreadyExistException.class,() ->
                resourceService.uploadResource(TEST_USER_ID, "", doubleFiles));
    }

    @Test
    void deleteResource_Test() {
        //  Файл
        MultipartFile file = createTestFile("dir1/test.txt", "Hello Test");
        resourceService.uploadResource(TEST_USER_ID, "", new MultipartFile[]{file});
        resourceService.deleteResource(TEST_USER_ID, "dir1/test.txt");
        assertFalse(s3Service.checkExistObject(BASE_PATCH + "dir1/test.txt"));

        // Папка
        s3Service.createEmptyDir(BASE_PATCH + "dir/");
        MultipartFile[] files = {
                createTestFile("file.txt", "Hello Test"),
                createTestFile("dir1/file1.txt", "Hello Test 2"),
                createTestFile("dir1/file2.txt", "Hello Test 2"),
                createTestFile("dir1/file3.txt", "Hello Test 2"),
        };
        resourceService.uploadResource(TEST_USER_ID, "dir/", files);
        resourceService.deleteResource(TEST_USER_ID, "dir/");
        List<Item> foundFiles = s3Service.getObjectsInDirectory(BASE_PATCH + "dir/", true);
        assertEquals(0, foundFiles.size());

        // Не существующий файл
        assertThrows(ResourceNotFoundException.class,() ->
                resourceService.deleteResource(TEST_USER_ID, "dir1/dir2/test.txt"));
    }

    @Test
    void getResourceInfo_Test() {
        MultipartFile file = createTestFile("dir1/test.txt", "Hello Test");
        resourceService.uploadResource(TEST_USER_ID, "", new MultipartFile[]{file});
        var resp = resourceService.getResourceInfo(TEST_USER_ID, "dir1/test.txt");
        assertEquals(resp.name(), "test.txt");
        assertEquals(resp.size(), file.getSize());
        // Не существующий файл
        assertThrows(ResourceNotFoundException.class, () ->
                resourceService.getResourceInfo(TEST_USER_ID, "dir1/dir2/test.txt"));
    }

    @Test
    void downloadResource_Test() {
        // Файл
        MultipartFile file = createTestFile("dir1/test.txt", "Hello Test");
        resourceService.uploadResource(TEST_USER_ID, "", new MultipartFile[]{file});
        DownloadResultDto dto = resourceService.downloadResource(TEST_USER_ID, "dir1/test.txt");
        assertEquals(dto.fileName(), "test.txt");

        // Папка
        s3Service.createEmptyDir(BASE_PATCH + "dir/");
        MultipartFile[] files = {
                createTestFile("file.txt", "Hello Test"),
                createTestFile("dir1/file1.txt", "Hello Test 2"),
                createTestFile("dir1/file2.txt", "Hello Test 2"),
                createTestFile("dir1/file3.txt", "Hello Test 2"),
        };
        resourceService.uploadResource(TEST_USER_ID, "", files);
        DownloadResultDto dtoDir = resourceService.downloadResource(TEST_USER_ID, "dir/");
        assertEquals(dtoDir.fileName(), "dir.zip");
        assertFalse(dtoDir.body().toString().isEmpty());

        // Не существующий файл
        assertThrows(ResourceNotFoundException.class, () ->
                resourceService.getResourceInfo(TEST_USER_ID, "dir1/dir2/test.txt"));
    }

    @Test
    void moveResource_Test() {
        // Перемещение файла в другую директорию
        s3Service.createEmptyDir(BASE_PATCH + "dir1/");
        s3Service.createEmptyDir(BASE_PATCH + "dir2/");
        MultipartFile file = createTestFile("file.txt", "Hello Test!");
        resourceService.uploadResource(TEST_USER_ID, "dir1/", new MultipartFile[]{file});
        resourceService.moveResource(TEST_USER_ID, "dir1/file.txt", "dir2/file.txt");
        List<Item> objectsDir1 = s3Service.getObjectsInDirectory(BASE_PATCH + "dir1/", true);
        List<Item> objectsDir2 = s3Service.getObjectsInDirectory(BASE_PATCH + "dir2/", true);
        assertFalse(objectsDir1.stream().anyMatch(object -> object.objectName().contains("file.txt")));
        assertTrue(objectsDir2.stream().anyMatch(object -> object.objectName().contains("file.txt")));

        // Перемещение файла в директорию, где лежит такой же файл
        resourceService.uploadResource(TEST_USER_ID, "dir1/", new MultipartFile[]{file});
        assertThrows(ResourceAlreadyExistException.class, () ->
                resourceService.moveResource(TEST_USER_ID, "dir1/file.txt", "dir2/file.txt"));

        // Не существующий файл
        assertThrows(ResourceNotFoundException.class, () ->
                resourceService.moveResource(TEST_USER_ID, "dir1/file10/file.txt", "dir2/file.txt"));
    }

    @Test
    void searchResources_Test() {
        s3Service.createEmptyDir(BASE_PATCH + "dir/");
        MultipartFile[] files = {
                createTestFile("file.txt", "Hello Test"),
                createTestFile("dir1/file1.txt", "Hello Test 2"),
                createTestFile("dir1/file2.txt", "Hello Test 2"),
                createTestFile("dir1/file3.txt", "Hello Test 2"),
        };
        resourceService.uploadResource(TEST_USER_ID, "dir/", files);
        List<ResourceResponse> files1 = resourceService.searchResources(TEST_USER_ID, "file");
        List<ResourceResponse> files2 = resourceService.searchResources(TEST_USER_ID, "dir");
        List<ResourceResponse> files3 = resourceService.searchResources(TEST_USER_ID, "file1");
        List<ResourceResponse> files4 = resourceService.searchResources(TEST_USER_ID, "file100");
        assertEquals(4, files1.size());
        assertEquals(2, files2.size());
        assertEquals(1, files3.size());
        assertEquals(0, files4.size());
    }


    @AfterEach
    void cleanUp() {
        log.info("=========>  CLEANUP");
        List<DeleteObject> objectsToDelete = s3Service.getObjectsInDirectory(BASE_PATCH, true)
                .stream()
                .filter(item -> !item.objectName().equals(BASE_PATCH))
                .map(item -> new DeleteObject(item.objectName()))
                .toList();
        if (!objectsToDelete.isEmpty()) {
            s3Service.deleteObjects(objectsToDelete);
        }
    }
}
