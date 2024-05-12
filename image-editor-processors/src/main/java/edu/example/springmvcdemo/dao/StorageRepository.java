package edu.example.springmvcdemo.dao;

import java.io.InputStream;
import java.util.List;

public interface StorageRepository {
    InputStream getObject(String objectName);

    void deleteObject(String objectName);

    boolean isObjectExist(String objectName);

    List<String> getAllObjects();

    String save(InputStream object, long sizeBytes);
}
