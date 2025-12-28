package com.frostetsky.cloudstorage.service.impl;

import com.frostetsky.cloudstorage.service.BucketService;
import com.frostetsky.cloudstorage.service.S3Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class BucketServiceImpl implements BucketService {

    private final S3Service s3Service;

    @Override
    public void createBaseBucket() {
        if (!s3Service.checkBaseBucketExists()) {
            log.info("Base bucket does not exist, creating");
            s3Service.createBaseBucket();
            log.info("Base bucket created");
        } else {
            log.debug("Base bucket already exists");
        }
    }
}
