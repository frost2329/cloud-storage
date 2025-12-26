package com.frostetsky.cloudstorage.runner;

import com.frostetsky.cloudstorage.service.BucketService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class BucketInitializer implements CommandLineRunner {

    private final BucketService bucketService;

    @Override
    public void run(String... args) {
        log.info("Initializing base bucket");
        bucketService.createBaseBucket();
        log.info("Base bucket initialization completed");
    }
}
