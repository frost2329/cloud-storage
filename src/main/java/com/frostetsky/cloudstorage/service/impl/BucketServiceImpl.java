package com.frostetsky.cloudstorage.service.impl;

import com.frostetsky.cloudstorage.excepiton.BucketServiceException;
import com.frostetsky.cloudstorage.service.BucketService;
import com.frostetsky.cloudstorage.service.S3Service;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BucketServiceImpl implements BucketService {

    private final S3Service s3Service;

    @Override
    public void createBaseBucket() {
        try {
            if (!s3Service.checkBaseBucketExists()) {
                s3Service.createBaseBucket();
            }
        } catch (Exception e) {
            throw new BucketServiceException("Ошибка при создании базового бакета", e);
        }
    }
}
