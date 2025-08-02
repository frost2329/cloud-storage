package com.frostetsky.cloudstorage.controller;

import com.frostetsky.cloudstorage.dto.DownloadResultDto;
import com.frostetsky.cloudstorage.dto.ResourceResponse;
import com.frostetsky.cloudstorage.model.CustomUserDetails;
import com.frostetsky.cloudstorage.service.ResourceService;
import com.frostetsky.cloudstorage.validation.File;
import com.frostetsky.cloudstorage.validation.Path;
import com.frostetsky.cloudstorage.validation.Query;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.util.List;

@RestController
@RequestMapping("/api/resource")
@RequiredArgsConstructor
@Validated
public class ResourceController {

    private final ResourceService resourceService;

    @GetMapping()
    public ResponseEntity<ResourceResponse> getResourceInfo(@RequestParam @Path String path) {
        CustomUserDetails userDetails = (CustomUserDetails) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();
        ResourceResponse resource = resourceService.getResourceInfo(userDetails.getUser().getId(), path);
        return ResponseEntity.status(HttpStatus.OK).body(resource);
    }

    @PostMapping()
    public ResponseEntity<List<ResourceResponse>> uploadResource(@RequestParam @Path String path,
                                                                 @RequestParam("object") @File MultipartFile[] files) {
        CustomUserDetails userDetails = (CustomUserDetails) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();
        List<ResourceResponse> resources = resourceService.uploadResource(userDetails.getUser().getId(), path, files);
        return ResponseEntity.status(HttpStatus.CREATED).body(resources);
    }

    @DeleteMapping()
    public ResponseEntity<Void> deleteResource(@RequestParam
                                               @Path
                                               @NotBlank(message = "Путь From не может быть пустым")
                                               String path) {
        CustomUserDetails userDetails = (CustomUserDetails) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();
        resourceService.deleteResource(userDetails.getUser().getId(), path);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @GetMapping("/download")
    public ResponseEntity<StreamingResponseBody> downloadResource(@RequestParam
                                                                  @Path
                                                                  @NotBlank(message = "Путь не может быть пустым")
                                                                  String path) {
        CustomUserDetails userDetails = (CustomUserDetails) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();
        DownloadResultDto result = resourceService.downloadResource(userDetails.getUser().getId(), path);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + result.fileName() + "\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(result.body());
    }

    @GetMapping("/move")
    public ResponseEntity<ResourceResponse> downloadResource(@RequestParam("from")
                                                             @Path
                                                             @NotBlank(message = "Путь From не может быть пустым")
                                                             String pathFrom,
                                                             @RequestParam("to")
                                                             @Path
                                                             @NotBlank(message = "Путь To не может быть пустым")
                                                             String pathTo) {
        CustomUserDetails userDetails = (CustomUserDetails) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();
        ResourceResponse result = resourceService.moveResource(userDetails.getUser().getId(), pathFrom, pathTo);
        return ResponseEntity.ok().body(result);
    }

    @GetMapping("/search")
    public ResponseEntity<List<ResourceResponse>> searchResource(@RequestParam("query") @Query String query){
        CustomUserDetails userDetails = (CustomUserDetails) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();
        List<ResourceResponse> resources = resourceService.searchResources(userDetails.getUser().getId(), query);
        return ResponseEntity.ok().body(resources);
    }
}
