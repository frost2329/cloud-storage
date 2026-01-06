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
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static com.frostetsky.cloudstorage.integration.util.FileUtil.createTestFile;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;


@Testcontainers
@SpringBootTest(classes = TestConfig.class)
public class ResourceServiceTest {
    @Autowired
    private ResourceService resourceService;
    @Autowired
    private S3Service s3Service;

    private static final Logger log = LoggerFactory.getLogger(ResourceServiceTest.class);
    private static final Long TEST_USER_ID = 10L;
    private static final String BASE_PATH = ResourcePathUtil.buildBasePath(TEST_USER_ID);

    private static final String NOT_EXISTING_DIRECTORY = "dir99/";
    private static final String DIR = "dir/";
    private static final String DIR_1 = "dir1/";
    private static final String DIR_2 = "dir2/";
    private static final String FILE_NAME = "file.txt";
    private static final String FILE_NAME_2 = "file1.txt";
    private static final String FILE_NAME_3 = "file3.txt";
    private static final String TEXT = "Hello Test";
    private static final String TEXT_2 = "Hello Test 2";



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
    @DisplayName("Upload file to directory")
    void uploadsFileWhenTargetDirExists() {
        MultipartFile file = createTestFile(DIR_1 + FILE_NAME, TEXT);

        resourceService.uploadResource(TEST_USER_ID, "", new MultipartFile[]{file}).get(0);

        List<Item> objects = s3Service.getObjectsInDirectory(BASE_PATH, true);
        assertThat(objects).anySatisfy(item -> {
            assertThat(item.objectName()).contains(DIR_1 + FILE_NAME);
        });
    }

    @Test
    @DisplayName("Upload file to not-existing directory throws ResourceNotFoundException")
    void uploadResourceToNotExistingDirectory() {
        MultipartFile[] files = {
                createTestFile(DIR_1 + FILE_NAME, TEXT),
                createTestFile(DIR_1 + FILE_NAME_2, TEXT_2),
        };

        assertThrows(ResourceNotFoundException.class, () ->
                resourceService.uploadResource(TEST_USER_ID, NOT_EXISTING_DIRECTORY, files)
        );
    }

    @Test
    @DisplayName("Upload files to directory with same names")
    void uploadFilesWithSameName() {
        MultipartFile[] filesWithSameName = {
                createTestFile(DIR_1 + FILE_NAME, TEXT),
                createTestFile(DIR_1 + FILE_NAME, TEXT_2),
        };

        assertThrows(ResourceAlreadyExistException.class, () ->
                resourceService.uploadResource(TEST_USER_ID, "", filesWithSameName)
        );
    }

    @Test
    @DisplayName("Deleting file")
    void deleteFile_WhenFileExist() {
        MultipartFile file = createTestFile(DIR_1 + FILE_NAME, TEXT);
        resourceService.uploadResource(TEST_USER_ID, "", new MultipartFile[]{file});

        resourceService.deleteResource(TEST_USER_ID, DIR_1 + FILE_NAME);

        assertThat(s3Service.checkExistObject(BASE_PATH + DIR_1 + FILE_NAME)).isFalse();
    }

    @Test
    @DisplayName("Deleting directory removes all nested files")
    void deleteDirectoryWithFiles() {
        s3Service.createEmptyDir(BASE_PATH + DIR);
        MultipartFile[] files = {
                createTestFile(FILE_NAME, TEXT),
                createTestFile(DIR_1 + FILE_NAME, TEXT_2),
                createTestFile(DIR_1 + FILE_NAME_2, TEXT_2),
                createTestFile(DIR_1 + FILE_NAME_3, TEXT_2),
        };
        resourceService.uploadResource(TEST_USER_ID, DIR, files);

        resourceService.deleteResource(TEST_USER_ID, DIR);

        List<Item> foundFiles = s3Service.getObjectsInDirectory(BASE_PATH + DIR, true);
        assertThat(foundFiles).isEmpty();
    }

    @Test
    @DisplayName("Deleting non-existing resource throws ResourceNotFoundException")
    void deleteNotExistingFile() {
        assertThrows(ResourceNotFoundException.class, () ->
                resourceService.deleteResource(TEST_USER_ID, DIR_1 + DIR_2 + FILE_NAME));
    }



    @Test
    @DisplayName("Fetching Resource Info")
    void getResourceInfoTest() {
        MultipartFile file = createTestFile(DIR_1 + FILE_NAME, TEXT);
        resourceService.uploadResource(TEST_USER_ID, "", new MultipartFile[]{file});

        var resp = resourceService.getResourceInfo(TEST_USER_ID, DIR_1 + FILE_NAME);

        assertThat(resp).satisfies(r -> {
            assertThat(r.name()).isEqualTo(FILE_NAME);
            assertThat(r.size()).isEqualTo(file.getSize());
        });
    }

    @Test
    @DisplayName("Fetching Resource Info not-existing file throws ResourceNotFoundException")
    void getResourceInfoTestNotExistingFile_ThrowsResourceNotFoundException() {
        assertThrows(ResourceNotFoundException.class, () ->
                resourceService.getResourceInfo(TEST_USER_ID, DIR_1 + DIR_2 + FILE_NAME));
    }


    private static String readBodyText(StreamingResponseBody body) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            body.writeTo(baos);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return baos.toString(StandardCharsets.UTF_8);
    }

    @Test
    @DisplayName("Download file with text")
    void downloadFileTest() {
        MultipartFile file = createTestFile(DIR_1 + FILE_NAME, TEXT);
        resourceService.uploadResource(TEST_USER_ID, "", new MultipartFile[]{file});

        DownloadResultDto downloadFile = resourceService.downloadResource(TEST_USER_ID, DIR_1 + FILE_NAME);

        assertThat(downloadFile).satisfies( f -> {
            assertThat(readBodyText(f.body())).isEqualTo(TEXT);
            assertThat(f.fileName()).isEqualTo(FILE_NAME);
        });
    }

    @Test
    @DisplayName("Download directory with files")
    void downloadDirectoryWithFiles() {
        s3Service.createEmptyDir(BASE_PATH + DIR);
        MultipartFile[] files = {
                createTestFile(FILE_NAME, TEXT),
                createTestFile(DIR_1 + FILE_NAME, TEXT_2),
                createTestFile(DIR_1 + FILE_NAME_2, TEXT_2),
                createTestFile(DIR_1 + FILE_NAME_3, TEXT_2),
        };
        resourceService.uploadResource(TEST_USER_ID, DIR, files);

        DownloadResultDto dtoDir = resourceService.downloadResource(TEST_USER_ID, DIR);

        assertThat(dtoDir.fileName()).isEqualTo("dir.zip");
    }

    @Test
    @DisplayName("Download not-existing file throw ResourceNotFoundException")
    void downloadNotExistingFile_ThrowsResourceNotFoundException() {
        assertThrows(ResourceNotFoundException.class, () ->
                resourceService.downloadResource(TEST_USER_ID, DIR_1 + DIR_2 + FILE_NAME));
    }

    @Test
    @DisplayName("Move file to another directory")
    void moveFileToAnotherDirectory() {
        s3Service.createEmptyDir(BASE_PATH + DIR_1);
        s3Service.createEmptyDir(BASE_PATH + DIR_2);
        MultipartFile file = createTestFile(FILE_NAME, TEXT);
        resourceService.uploadResource(TEST_USER_ID, DIR_1, new MultipartFile[]{file});

        resourceService.moveResource(TEST_USER_ID, DIR_1 + FILE_NAME, DIR_2 + FILE_NAME);

        List<Item> objectsDir1 = s3Service.getObjectsInDirectory(BASE_PATH + DIR_1, true);
        List<Item> objectsDir2 = s3Service.getObjectsInDirectory(BASE_PATH + DIR_2, true);
        assertThat(objectsDir1).noneSatisfy( item -> {
            assertThat(item.objectName()).contains(FILE_NAME);
        });
        assertThat(objectsDir2).anySatisfy( item -> {
            assertThat(item.objectName()).contains(FILE_NAME);
        });
    }

    @Test
    @DisplayName("Move file to directory with same file")
    void moveFileToDirectoryWithSameFile() {
        s3Service.createEmptyDir(BASE_PATH + DIR_1);
        s3Service.createEmptyDir(BASE_PATH + DIR_2);
        MultipartFile file = createTestFile(FILE_NAME, TEXT);
        resourceService.uploadResource(TEST_USER_ID, DIR_1, new MultipartFile[]{file});
        resourceService.uploadResource(TEST_USER_ID, DIR_2, new MultipartFile[]{file});

        assertThrows(ResourceAlreadyExistException.class, () ->
                resourceService.moveResource(TEST_USER_ID, DIR_1 + FILE_NAME, DIR_2 + FILE_NAME));
    }

    @Test
    @DisplayName("Move not-existing file throws ResourceNotFoundException")
    void moveNotExistingFile() {
        assertThrows(ResourceNotFoundException.class, () ->
                resourceService.moveResource(
                        TEST_USER_ID,
                        NOT_EXISTING_DIRECTORY + FILE_NAME,
                        DIR_2 + FILE_NAME));
    }

    @Test
    @DisplayName("Search resources by different query")
    void searchResources() {
        s3Service.createEmptyDir(BASE_PATH + "dir/");
        MultipartFile[] files = {
                createTestFile(FILE_NAME, TEXT),
                createTestFile(DIR_1 + FILE_NAME, TEXT_2),
                createTestFile(DIR_1 + FILE_NAME_2, TEXT_2),
                createTestFile(DIR_1 + FILE_NAME_3, TEXT_2),
        };
        resourceService.uploadResource(TEST_USER_ID, "dir/", files);

        List<ResourceResponse> searchByFile = resourceService.searchResources(TEST_USER_ID, "file");
        List<ResourceResponse> searchByDir = resourceService.searchResources(TEST_USER_ID, "dir");
        List<ResourceResponse> searchByFile1 = resourceService.searchResources(TEST_USER_ID, "file1");
        List<ResourceResponse> searchByFile100 = resourceService.searchResources(TEST_USER_ID, "file100");

        assertThat(searchByFile).hasSize(4);
        assertThat(searchByDir).hasSize(2);
        assertThat(searchByFile1).hasSize(1);
        assertThat(searchByFile100).isEmpty();
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
