package edu.example.springmvcdemo.dao;

import io.minio.*;
import io.minio.errors.ErrorResponseException;
import io.minio.messages.Item;
import io.minio.messages.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.stereotype.Repository;

import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class MinioRepository implements StorageRepository {

    private static final int MAX_ATTEMPTS_TO_GEN_FILENAME = 3;
    private static final int UPLOAD_STREAM_PART_SIZE_BYTES = 10_000_000;
    private static final Tag TEMPORARY_OBJECT_TAG = new Tag("ttl", "temp");

    private final MinioClient minioClient;

    @Value("${minio.bucket-name}")
    private String bucketName;

    @Value("${minio.ttl-days}")
    private int ttlDays;

    private ObjectWriteResponse saveObject(String objectName, InputStream object, boolean isTemporary) {
        try {
            return minioClient.putObject(PutObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectName)
                    .tags(isTemporary ?
                            Map.of(TEMPORARY_OBJECT_TAG.key(), TEMPORARY_OBJECT_TAG.value()) :
                            null)
                    .stream(object, -1, UPLOAD_STREAM_PART_SIZE_BYTES).build());
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
    public String save(InputStream object, boolean isTemporary) {

        String generatedFileName = UUID.randomUUID().toString();

        int tryCount = 0;
        while (this.isObjectExist(generatedFileName)) {
            if (tryCount++ > MAX_ATTEMPTS_TO_GEN_FILENAME) {
                throw new RuntimeException("Unable to generate unique filename");
            }
            generatedFileName = UUID.randomUUID().toString();
        }

        this.saveObject(generatedFileName, object, isTemporary);

        return generatedFileName;
    }
}
