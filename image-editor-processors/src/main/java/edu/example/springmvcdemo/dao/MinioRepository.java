package edu.example.springmvcdemo.dao;

import io.minio.*;
import io.minio.errors.ErrorResponseException;
import io.minio.messages.*;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.stereotype.Repository;

import java.io.InputStream;
import java.time.ZonedDateTime;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;



@Repository
@RequiredArgsConstructor
public class MinioRepository implements StorageRepository {

    private static final int MAX_ATTEMPTS_TO_GEN_FILENAME = 3;
    private static final Tag TEMPORARY_OBJECT_TAG = new Tag("ttl", "temp");

    private final MinioClient minioClient;

    @Value("${minio.bucket-name}")
    private String bucketName;

    @Value("${minio.ttl-days}")
    private int ttlDays;

    @PostConstruct
    public void setupStorage() {
        createBucket();
        addTTLRule();
    }

    @SneakyThrows
    public void createBucket() {
        boolean bucketExists = minioClient.bucketExists(BucketExistsArgs.builder()
                .bucket(bucketName)
                .build());

        if (!bucketExists) {
            minioClient.makeBucket(MakeBucketArgs.builder()
                    .bucket(bucketName).build());
        }
    }

    @SneakyThrows
    public void addTTLRule() {
        List<LifecycleRule> rules = new LinkedList<>();
        rules.add(new LifecycleRule(
                Status.ENABLED,
                null,
                new Expiration((ZonedDateTime) null, ttlDays, null),
                new RuleFilter(TEMPORARY_OBJECT_TAG),
                "TTL-Rule",
                null,
                null,
                null
        ));
        LifecycleConfiguration config = new LifecycleConfiguration(rules);
        minioClient.setBucketLifecycle(
                SetBucketLifecycleArgs.builder()
                        .bucket(bucketName)
                        .config(config).build());
    }

    private ObjectWriteResponse saveObject(String objectName, InputStream object, long sizeBytes) {
        try {
            return minioClient.putObject(PutObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectName)
                    .tags(Map.of(TEMPORARY_OBJECT_TAG.key(), TEMPORARY_OBJECT_TAG.value()))
                    .stream(object, sizeBytes, -1).build());
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
    public String save(InputStream object, long sizeBytes) {

        String generatedFileName = UUID.randomUUID().toString();

        int tryCount = 0;
        while (this.isObjectExist(generatedFileName)) {
            if (tryCount++ > MAX_ATTEMPTS_TO_GEN_FILENAME) {
                throw new RuntimeException("Unable to generate unique filename");
            }
            generatedFileName = UUID.randomUUID().toString();
        }

        this.saveObject(generatedFileName, object, sizeBytes);

        return generatedFileName;
    }
}
