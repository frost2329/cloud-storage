package com.frostetsky.cloudstorage.service.impl;

import com.frostetsky.cloudstorage.excepiton.BucketServiceException;
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
        try {
            log.debug("Ensuring base bucket exists");
            if (!s3Service.checkBaseBucketExists()) {
                log.info("Base bucket does not exist, creating");
                s3Service.createBaseBucket();
                log.info("Base bucket created");
            } else {
                log.debug("Base bucket already exists");
            }
        } catch (Exception e) {
            log.error("Failed to ensure base bucket exists", e);
            throw new BucketServiceException("Ошибка при создании базового бакета", e);
        }
    }
}
