package edu.example.springmvcdemo.dao;

import org.springframework.web.multipart.MultipartFile;
import java.io.InputStream;

public interface StorageRepository {
    InputStream getObject(String objectName);

    void deleteObject(String objectName);

    String save(MultipartFile file);


    boolean isObjectExist(String objectName);
}
