package com.frostetsky.cloudstorage.controller;

import com.frostetsky.cloudstorage.dto.DownloadResultDto;
import com.frostetsky.cloudstorage.dto.ResourceDto;
import com.frostetsky.cloudstorage.service.ResourceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.util.List;

@RestController
@RequestMapping("/api/resource")
@RequiredArgsConstructor
public class ResourceController {

    private final ResourceService resourceService;

    @GetMapping()
    public ResponseEntity<ResourceDto> getResourceInfo(@RequestParam String path) {
        ResourceDto resource = resourceService.getResourceInfo(path);
        return ResponseEntity.status(HttpStatus.OK).body(resource);
    }

    @PostMapping()
    public ResponseEntity<List<ResourceDto>> upload(@RequestParam String path,
                                 @RequestParam MultipartFile[] object) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        List<ResourceDto> resources = resourceService.upload(authentication.getName(), path, object);
        return ResponseEntity.status(HttpStatus.CREATED).body(resources);
    }

    @DeleteMapping()
    public ResponseEntity<Void> delete(@RequestParam String path) {
        resourceService.deleteResource(path);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @GetMapping("/download")
    public ResponseEntity<StreamingResponseBody> downloadResource(@RequestParam String path) {
        DownloadResultDto result = resourceService.downloadResource(path);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + result.fileName() + "\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(result.body());
    }

    @GetMapping("/move")
    public ResponseEntity<ResourceDto> downloadResource(@RequestParam("from") String pathFrom,
                                                        @RequestParam("to") String pathTo) {
        ResourceDto result = resourceService.moveResource(pathFrom, pathTo);
        return ResponseEntity.ok().body(result);
    }

    @GetMapping("/search")
    public ResponseEntity<List<ResourceDto>> searchResource(@RequestParam("query") String query){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        List<ResourceDto> resources = resourceService.searchResources(authentication.getName(), query);
        return ResponseEntity.ok().body(resources);
    }
}
