package com.frostetsky.cloudstorage.integration.util;

import org.springframework.mock.web.MockMultipartFile;

import java.nio.charset.StandardCharsets;

public class FileUtil {
    public static MockMultipartFile createTestFile(String filename, String content) {
        return new MockMultipartFile(
                "file",
                filename,
                "text/plain",
                content.getBytes(StandardCharsets.UTF_8)
        );
    }
}
