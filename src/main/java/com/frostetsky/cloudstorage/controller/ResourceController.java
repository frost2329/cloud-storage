package com.frostetsky.cloudstorage.controller;

import com.frostetsky.cloudstorage.dto.DownloadResultDto;
import com.frostetsky.cloudstorage.dto.ResourceResponse;
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
    public ResponseEntity<ResourceResponse> getResourceInfo(@RequestParam String path) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        ResourceResponse resource = resourceService.getResourceInfo(authentication.getName(), path);
        return ResponseEntity.status(HttpStatus.OK).body(resource);
    }

    @PostMapping()
    public ResponseEntity<List<ResourceResponse>> uploadResource(@RequestParam String path,
                                                                 @RequestParam MultipartFile[] object) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        List<ResourceResponse> resources = resourceService.upload(authentication.getName(), path, object);
        return ResponseEntity.status(HttpStatus.CREATED).body(resources);
    }

    @DeleteMapping()
    public ResponseEntity<Void> deleteResource(@RequestParam String path) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        resourceService.deleteResource(authentication.getName(), path);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @GetMapping("/download")
    public ResponseEntity<StreamingResponseBody> downloadResource(@RequestParam String path) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        DownloadResultDto result = resourceService.downloadResource(authentication.getName(), path);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + result.fileName() + "\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(result.body());
    }

    @GetMapping("/move")
    public ResponseEntity<ResourceResponse> downloadResource(@RequestParam("from") String pathFrom,
                                                             @RequestParam("to") String pathTo) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        ResourceResponse result = resourceService.moveResource(authentication.getName(), pathFrom, pathTo);
        return ResponseEntity.ok().body(result);
    }

    @GetMapping("/search")
    public ResponseEntity<List<ResourceResponse>> searchResource(@RequestParam("query") String query){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        List<ResourceResponse> resources = resourceService.searchResources(authentication.getName(), query);
        return ResponseEntity.ok().body(resources);
    }
}
