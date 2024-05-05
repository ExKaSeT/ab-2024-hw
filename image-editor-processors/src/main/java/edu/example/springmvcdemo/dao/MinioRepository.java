package edu.example.springmvcdemo.dao;

import io.minio.*;
import io.minio.errors.ErrorResponseException;
import io.minio.messages.Item;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.stereotype.Repository;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class MinioRepository implements StorageRepository {

    private static final int MAX_ATTEMPTS_TO_GEN_FILENAME = 3;

    private final MinioClient minioClient;

    @Value("${minio.bucket-name}")
    private String bucketName;

    @Value("${minio.ttl-days}")
    private Integer ttlDays;

    public ObjectWriteResponse saveObject(String objectName, Long size, InputStream object) {
        try {
            return minioClient.putObject(PutObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectName)
                    .stream(object, size, -1).build());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean isObjectExist(String objectName) {
        try {
            minioClient.statObject(StatObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectName).build());
            return true;
        } catch (ErrorResponseException e) {
            return false;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public InputStream getObject(String objectName) {
        try {
            return minioClient.getObject(GetObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectName).build());
        } catch (Exception e) {
            throw new DataAccessResourceFailureException(e.getMessage(), e);
        }
    }

    @Override
    public void deleteObject(String objectName) {
        try {
            minioClient.removeObject(RemoveObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectName)
                    .build());
        } catch (Exception e) {
            throw new DataAccessResourceFailureException(e.getMessage(), e);
        }
    }

    @Override
    public List<String> getAllObjects() {
        var iterable = minioClient.listObjects(ListObjectsArgs.builder()
                .bucket(bucketName).build());

        List<String> objectNameList = new LinkedList<>();
        for (Result<Item> result : iterable) {
            try {
                objectNameList.add(result.get().objectName());
            } catch (Exception ignored) {}
        }
        return objectNameList;
    }

    @Override
    public String save(MultipartFile file) {

        String generatedFileName = UUID.randomUUID().toString();

        int tryCount = 0;
        while (this.isObjectExist(generatedFileName)) {
            if (tryCount++ > MAX_ATTEMPTS_TO_GEN_FILENAME) {
                throw new RuntimeException("Unable to generate unique filename");
            }
            generatedFileName = UUID.randomUUID().toString();
        }

        try {
            this.saveObject(generatedFileName, file.getSize(), file.getInputStream());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return generatedFileName;
    }
}
