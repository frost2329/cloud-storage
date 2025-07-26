package com.frostetsky.cloudstorage.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ResourceResponse(String path,
                               String name,
                               Long size,
                               String type) {
}
