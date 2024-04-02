package edu.example.springmvcdemo.service;

import edu.example.springmvcdemo.dao.ImageRepository;
import edu.example.springmvcdemo.dao.StorageRepository;
import edu.example.springmvcdemo.exception.EntityNotFoundException;
import edu.example.springmvcdemo.model.Image;
import edu.example.springmvcdemo.model.User;
import lombok.RequiredArgsConstructor;
import org.apache.commons.compress.utils.FileNameUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.InputStream;
import java.util.List;
import static java.util.Objects.isNull;

@Service
@RequiredArgsConstructor
public class ImageService {
    private static int FILENAME_MAX_LENGTH = 100;

    private final ImageRepository imageRepository;
    private final StorageRepository storageRepository;

    public Image getMeta(String link) {
        return imageRepository.findByLink(link)
                .orElseThrow(() -> new EntityNotFoundException("Image not found"));
    }

    public List<Image> getUserImageMetas(User user) {
        return imageRepository.findAllByUser(user);
    }

    public boolean exists(String link) {
        return imageRepository.existsByLink(link);
    }


    public InputStream get(String link) {
        return storageRepository.getObject(link);
    }

    public Image upload(MultipartFile file, User user) {
        var image = new Image();
        image.setUser(user);

        if (file.getSize() > Integer.MAX_VALUE) {
            throw new IllegalArgumentException();
        }
        image.setSizeBytes((int) file.getSize());

        // TODO
        String originalName = file.getOriginalFilename();
        if (isNull(originalName) || originalName.length() > FILENAME_MAX_LENGTH) {
            String extension = FileNameUtils.getExtension(file.getOriginalFilename());
            originalName = "unknown" + extension;
        }
        image.setOriginalName(originalName);

        var link = storageRepository.save(file);
        try {
            image.setLink(link);
            return imageRepository.save(image);
        } catch (Throwable ex) {
            storageRepository.deleteObject(link);
            throw ex;
        }
    }

    public void delete(String link) {
        storageRepository.deleteObject(link);
        imageRepository.deleteByLink(link);
    }
}
