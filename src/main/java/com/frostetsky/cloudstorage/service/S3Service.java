package com.frostetsky.cloudstorage.service;

import io.minio.ObjectWriteResponse;
import io.minio.StatObjectResponse;
import io.minio.messages.DeleteObject;
import io.minio.messages.Item;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.List;

public interface S3Service {

    boolean checkBaseBucketExists();

    void createBaseBucket();

    List<Item> getObjectsInDirectory(String path, boolean recursive);

    ObjectWriteResponse createEmptyDir(String path);

    ObjectWriteResponse putObject(String path, MultipartFile file);

    void deleteObjects(List<DeleteObject> objectsToDelete);

    boolean checkExistObject(String path);

    InputStream downloadObject(String path);

    ObjectWriteResponse copyObject(String pathFrom, String pathTo);

    StatObjectResponse getObjectInfo(String path);
}
