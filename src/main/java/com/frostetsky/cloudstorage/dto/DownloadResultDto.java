package com.frostetsky.cloudstorage.dto;

import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

public record DownloadResultDto(String fileName,
                                StreamingResponseBody body) {
}
