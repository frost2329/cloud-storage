package com.frostetsky.cloudstorage.controller;

import com.frostetsky.cloudstorage.dto.ResourceResponse;
import com.frostetsky.cloudstorage.service.impl.DirectoryServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/directory")
@RequiredArgsConstructor
public class DirectoryController {

    private final DirectoryServiceImpl directoryService;

    @GetMapping()
    public ResponseEntity<List<ResourceResponse>> getDirectoryContent(@RequestParam String path) {
        Authentication authentication= SecurityContextHolder.getContext().getAuthentication();
        List<ResourceResponse> files = directoryService.getDirectoryFiles(authentication.getName(), path);
        return ResponseEntity.status(HttpStatus.OK).body(files);
    }

    @PostMapping()
    public ResponseEntity<ResourceResponse> createDirectory(@RequestParam String path) {
        Authentication authentication= SecurityContextHolder.getContext().getAuthentication();
        ResourceResponse directory = directoryService.createDirectory(authentication.getName(), path);
        return ResponseEntity.status(HttpStatus.OK).body(directory);
    }
}
