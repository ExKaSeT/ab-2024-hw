package edu.example.springmvcdemo.service;

import edu.example.springmvcdemo.annotation.IntegrationTest;
import edu.example.springmvcdemo.dao.*;
import edu.example.springmvcdemo.dto.auth.RegisterRequestDto;
import edu.example.springmvcdemo.dto.image_processing.ImageDoneDto;
import edu.example.springmvcdemo.model.ImageProcessing;
import edu.example.springmvcdemo.model.ImageProcessingStatus;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.transaction.annotation.Transactional;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

@IntegrationTest
@RequiredArgsConstructor
public class ImageProcessingServiceTest {

    private final ImageService imageService;
    private final ImageProcessingService imageProcessingService;
    private final AuthService authService;
    private final ImageProcessingRepository imageProcessingRepository;
    private final ImageRepository imageRepository;
    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final StorageRepository storageRepository;

    private static final String USERNAME = "username";
    private static final String PASSWORD = "password";

    @BeforeEach
    public void init() {
        authService.register(new RegisterRequestDto(USERNAME, PASSWORD));
    }

    @AfterEach
    public void clear() {
        imageProcessingRepository.deleteAll();
        imageRepository.deleteAll();
        refreshTokenRepository.deleteAll();
        userRepository.deleteAll();
        storageRepository.getAllObjects().forEach(storageRepository::deleteObject);
    }

    private MockMultipartFile createFile() {
        return new MockMultipartFile("filename", "filename", MediaType.TEXT_PLAIN_VALUE,
                "someText".getBytes());
    }

    private ImageProcessing createProcessingRecord(ImageProcessingStatus status) {
        var user = userRepository.findByUsername(USERNAME).get();
        var file = createFile();
        var image = imageService.upload(file, user);
        var imageMeta = imageService.getMeta(image.getLink());
        var requestId = UUID.randomUUID().toString();
        return imageProcessingService.createImageProcessingRecord(requestId, imageMeta.getLink(), status);
    }

    @Test
    @Transactional
    public void getImageProcessing() {
        var imageProcessing = createProcessingRecord(ImageProcessingStatus.WIP);
        var requestId = imageProcessing.getRequestId();
        var image = imageProcessing.getOriginalImage();

        var statusWipResponse = imageProcessingService.getImageProcessingStatus(requestId);

        assertEquals(image.getLink(), statusWipResponse.getImageId());
        assertEquals(ImageProcessingStatus.WIP, statusWipResponse.getStatus());


        var processedLink = UUID.randomUUID().toString();
        imageProcessing.setStatus(ImageProcessingStatus.DONE);
        imageProcessing.setProcessedImage(processedLink);
        imageProcessingRepository.save(imageProcessing);

        var statusDoneResponse = imageProcessingService.getImageProcessingStatus(requestId);

        assertEquals(processedLink, statusDoneResponse.getImageId());
        assertEquals(ImageProcessingStatus.DONE, statusDoneResponse.getStatus());
    }

    @Test
    @Transactional
    public void doneImageIgnoreIfRequestNotExist() {
        var imageProcessing = createProcessingRecord(ImageProcessingStatus.DONE);
        var requestId = imageProcessing.getRequestId();
        var image = imageProcessing.getOriginalImage();
        imageProcessingRepository.delete(imageProcessing);

        imageProcessingService.processDoneImage(new ImageDoneDto(image.getLink(), requestId, 666, ImageProcessingStatus.DONE));

        assertEquals(0, imageProcessingRepository.findAll().size());
        assertEquals(1, imageRepository.findAll().size());
    }

    @Test
    @Transactional
    public void doneImagePositive() {
        var imageProcessing = createProcessingRecord(ImageProcessingStatus.WIP);
        var original = imageProcessing.getOriginalImage();
        var requestId = imageProcessing.getRequestId();
        var user = userRepository.findByUsername(USERNAME).get();
        var processedImage = imageService.upload(createFile(), user);

        imageProcessingService.processDoneImage(new ImageDoneDto(
                processedImage.getLink(), requestId, 666, ImageProcessingStatus.DONE));
        imageProcessing = imageProcessingRepository.findById(requestId).get();
        var meta = imageService.getMeta(imageProcessing.getProcessedImage());

        assertEquals(processedImage.getLink(), imageProcessing.getProcessedImage());
        assertEquals(ImageProcessingStatus.DONE, imageProcessing.getStatus());
        assertEquals(original, imageProcessing.getOriginalImage());
        assertEquals(original, imageProcessing.getOriginalImage());
        assertEquals(original.getOriginalName(), meta.getOriginalName());
        assertEquals(original.getUser(), meta.getUser());
    }
}
