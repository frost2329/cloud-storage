package com.frostetsky.cloudstorage.dto;

public record ResourceDto(String path,
                          String name,
                          Long size,
                          String type) {
}
