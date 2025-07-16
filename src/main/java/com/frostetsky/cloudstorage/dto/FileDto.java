package com.frostetsky.cloudstorage.dto;

public record FileDto(String path,
                      String name,
                      Long size,
                      String type) {
}
