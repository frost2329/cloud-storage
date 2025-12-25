package com.frostetsky.cloudstorage.integration.service;

import com.frostetsky.cloudstorage.integration.config.TestConfig;
import com.frostetsky.cloudstorage.service.BucketService;
import com.frostetsky.cloudstorage.service.S3Service;
import io.minio.BucketExistsArgs;
import io.minio.MinioClient;
import io.minio.RemoveBucketArgs;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.testcontainers.junit.jupiter.Testcontainers;

import static com.frostetsky.cloudstorage.constants.MinioConstants.BUCKET_NAME;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Testcontainers
@SpringBootTest(classes = TestConfig.class)
public class BucketServiceTest {

    @Autowired
    private BucketService bucketService;
    @Autowired
    private S3Service s3Service;
    @Autowired
    private MinioClient minioClient;

    private static final Logger log = LoggerFactory.getLogger(BucketServiceTest.class);

    @BeforeEach
    void logTestStart(TestInfo testInfo) {
        log.info("==========>  STARTING TEST: {}", testInfo.getDisplayName());
    }

    @Test
    void createBucketTest() throws Exception {
        if (s3Service.checkBaseBucketExists()) {
            minioClient.removeBucket(RemoveBucketArgs.builder().bucket(BUCKET_NAME).build());
        }
        // Запуск когда бакета нет
        assertDoesNotThrow(() -> bucketService.createBaseBucket());
        assertTrue(minioClient.bucketExists(BucketExistsArgs.builder().bucket(BUCKET_NAME).build()));
        // Запуск когда бакет есть
        assertDoesNotThrow(() -> bucketService.createBaseBucket());
    }
}
