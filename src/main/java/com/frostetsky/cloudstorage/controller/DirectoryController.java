package com.frostetsky.cloudstorage.controller;

import com.frostetsky.cloudstorage.dto.ResourceResponse;
import com.frostetsky.cloudstorage.model.CustomUserDetails;
import com.frostetsky.cloudstorage.service.DirectoryService;
import com.frostetsky.cloudstorage.validation.Path;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/directory")
@RequiredArgsConstructor
@Validated
public class DirectoryController {

    private final DirectoryService directoryService;

    @GetMapping()
    public ResponseEntity<List<ResourceResponse>> getDirectoryContent(@RequestParam @Path String path,
                                                                      @AuthenticationPrincipal CustomUserDetails userDetails) {
        List<ResourceResponse> files = directoryService.getDirectoryFiles(userDetails.getUser().getId(), path);
        return ResponseEntity.status(HttpStatus.OK).body(files);
    }

    @PostMapping()
    public ResponseEntity<ResourceResponse> createDirectory(@RequestParam
                                                            @Path
                                                            @NotBlank(message = "Путь не может быть пустым")
                                                            String path,
                                                            @AuthenticationPrincipal CustomUserDetails userDetails) {
        ResourceResponse directory = directoryService.createDirectory(userDetails.getUser().getId(), path);
        return ResponseEntity.status(HttpStatus.OK).body(directory);
    }
}
