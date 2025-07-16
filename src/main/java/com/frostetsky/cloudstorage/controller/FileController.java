package com.frostetsky.cloudstorage.controller;

import com.frostetsky.cloudstorage.dto.FileDto;
import com.frostetsky.cloudstorage.service.DirectoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class FileController {

    private final DirectoryService directoryService;

    @GetMapping("/directory")
    public ResponseEntity getCurrentUser(@RequestParam String path) {
        Authentication authentication= SecurityContextHolder.getContext().getAuthentication();
        List<FileDto> files = directoryService.getDirectoryFiles(authentication.getName(), path);
        return ResponseEntity.status(HttpStatus.OK).body(files);
    }
}
