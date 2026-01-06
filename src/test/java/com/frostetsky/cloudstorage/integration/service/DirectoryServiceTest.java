package com.frostetsky.cloudstorage.integration.service;

import com.frostetsky.cloudstorage.dto.CreateUserRequest;
import com.frostetsky.cloudstorage.dto.ResourceResponse;
import com.frostetsky.cloudstorage.excepiton.ResourceAlreadyExistException;
import com.frostetsky.cloudstorage.excepiton.ResourceNotFoundException;
import com.frostetsky.cloudstorage.integration.config.TestConfig;
import com.frostetsky.cloudstorage.service.DirectoryService;
import com.frostetsky.cloudstorage.service.ResourceService;
import com.frostetsky.cloudstorage.service.S3Service;
import com.frostetsky.cloudstorage.service.UserService;
import com.frostetsky.cloudstorage.util.ResourcePathUtil;
import io.minio.messages.DeleteObject;
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
public class DirectoryServiceTest {

    @Autowired
    private ResourceService resourceService;
    @Autowired
    private DirectoryService directoryService;
    @Autowired
    private UserService userService;
    @Autowired
    private S3Service s3Service;

    private static final Logger log = LoggerFactory.getLogger(ResourceServiceTest.class);
    private static final Long TEST_USER_ID = 10L;
    private static final String BASE_PATH = ResourcePathUtil.buildBasePath(TEST_USER_ID);

    @BeforeAll
    static void setup(@Autowired S3Service s3Service) {
        log.info("===== BEFORE_ALL =====");
        try {
            if (!s3Service.checkBaseBucketExists()) {
                s3Service.createBaseBucket();
            }
            s3Service.createEmptyDir(BASE_PATH);
        } catch (Exception e) {
            throw new RuntimeException("Failed to setup bucket", e);
        }
    }

    @BeforeEach
    void logTestStart(TestInfo testInfo) {
        log.info("==========>  STARTING TEST: {}", testInfo.getDisplayName());
    }

    @Test
    void getResourceInfo_Test() {
        s3Service.createEmptyDir(BASE_PATH + "dir/");
        MultipartFile[] files = {
                createTestFile("file.txt", "Hello Test"),
                createTestFile("file2.txt", "Hello Test")
        };
        resourceService.uploadResource(TEST_USER_ID, "dir/", files);
        var dirFiles = directoryService.getDirectoryFiles(TEST_USER_ID, "dir/");
        assertEquals(2, dirFiles.size());

        // Получение файлов из несуществующей папки
        assertThrows(ResourceNotFoundException.class, () ->
                directoryService.getDirectoryFiles(TEST_USER_ID, "dir10/"));
    }

    @Test
    void createDirectory_Test() {
        ResourceResponse directory = directoryService.createDirectory(TEST_USER_ID, "dir/");
        assertEquals("dir/", directory.name());

        // Создание уже существующей папки
        assertThrows(ResourceAlreadyExistException.class, () ->
                directoryService.createDirectory(TEST_USER_ID, "dir/"));

        // Создание папки в несуществующей папке
        assertThrows(ResourceNotFoundException.class, () ->
                directoryService.createDirectory(TEST_USER_ID, "dir0/dir1/dir2"));
    }

    @Test
    void createBaseDirectory_Test() {
        s3Service.deleteObjects(List.of(new DeleteObject(BASE_PATH)));
        CreateUserRequest userDto = new CreateUserRequest("test_user", "password");
        userService.createUser(userDto);
        directoryService.createBaseDirectory(userDto.username());
        s3Service.checkExistObject("user-1-files/");
    }


    @AfterEach
    void cleanUp() {
        log.info("=========>  CLEANUP");
        List<DeleteObject> objectsToDelete = s3Service.getObjectsInDirectory(BASE_PATH, true)
                .stream()
                .filter(item -> !item.objectName().equals(BASE_PATH))
                .map(item -> new DeleteObject(item.objectName()))
                .toList();
        if (!objectsToDelete.isEmpty()) {
            s3Service.deleteObjects(objectsToDelete);
        }
    }
}
