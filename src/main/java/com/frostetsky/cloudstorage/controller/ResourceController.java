package com.frostetsky.cloudstorage.controller;

import com.frostetsky.cloudstorage.dto.ResourceDto;
import com.frostetsky.cloudstorage.service.ResourceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

@RestController
@RequestMapping("/api/resource")
@RequiredArgsConstructor
public class ResourceController {

    private final ResourceService resourceService;

    @PostMapping()
    public ResponseEntity upload(@RequestParam String path,
                                 @RequestParam MultipartFile[] object) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        List<ResourceDto> resources = resourceService.upload(authentication.getName(), path, object);
        return ResponseEntity.status(HttpStatus.CREATED).body(resources);
    }

    @DeleteMapping()
    public ResponseEntity delete(@RequestParam String path) {
        resourceService.delete(path);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
