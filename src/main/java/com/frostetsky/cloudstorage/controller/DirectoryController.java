package com.frostetsky.cloudstorage.controller;

import com.frostetsky.cloudstorage.dto.ResourceResponse;
import com.frostetsky.cloudstorage.model.CustomUserDetails;
import com.frostetsky.cloudstorage.service.DirectoryService;
import com.frostetsky.cloudstorage.service.ValidationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/directory")
@RequiredArgsConstructor
public class DirectoryController {

    private final DirectoryService directoryService;
    private final ValidationService validationService;

    @GetMapping()
    public ResponseEntity<List<ResourceResponse>> getDirectoryContent(@RequestParam String path) {
        validationService.validatePath(path);
        CustomUserDetails userDetails = (CustomUserDetails) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();
        List<ResourceResponse> files = directoryService.getDirectoryFiles(userDetails.getUser().getId(), path);
        return ResponseEntity.status(HttpStatus.OK).body(files);
    }

    @PostMapping()
    public ResponseEntity<ResourceResponse> createDirectory(@RequestParam String path) {
        validationService.validatePath(path);
        CustomUserDetails userDetails = (CustomUserDetails) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();
        ResourceResponse directory = directoryService.createDirectory(userDetails.getUser().getId(), path);
        return ResponseEntity.status(HttpStatus.OK).body(directory);
    }
}
