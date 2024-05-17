package edu.example.springmvcdemo.service;

import edu.example.springmvcdemo.annotation.IntegrationTest;
import edu.example.springmvcdemo.dao.ProcessedImageRepository;
import edu.example.springmvcdemo.dao.StorageRepository;
import edu.example.springmvcdemo.dto.processor.StreamDataDto;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.transaction.annotation.Transactional;
import java.io.IOException;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

@IntegrationTest
@RequiredArgsConstructor
public class ImageProcessingServiceTest {

    private final ProcessedImageRepository processedImageRepository;
    private final ImageProcessingService imageProcessingService;
    private final StorageRepository storageRepository;

    @AfterEach
    public void clear() {
        processedImageRepository.deleteAll();
        storageRepository.getAllObjects().forEach(storageRepository::deleteObject);
    }

    private MockMultipartFile createFile() {
        return new MockMultipartFile("filename", "filename", MediaType.TEXT_PLAIN_VALUE,
                "someText".getBytes());
    }

    @Test
    @Transactional
    public void saveGetProcessedEvent() {
        var requestId = UUID.randomUUID().toString();
        var imageId = UUID.randomUUID().toString();

        imageProcessingService.saveProcessedEvent(requestId, imageId, null, null);
        var processed = imageProcessingService.getProcessedEvent(requestId, imageId).get();

        assertEquals(1, processedImageRepository.findAll().size());
        assertEquals(requestId, processed.getId().getRequestId());
        assertEquals(imageId, processed.getId().getImageId());
    }

    @Test
    @Transactional
    public void saveProcessedImage() throws IOException {
        var requestId = UUID.randomUUID().toString();
        var imageId = UUID.randomUUID().toString();
        var file = createFile();
        var data = new StreamDataDto();
        data.setStream(file.getInputStream());
        data.setSize(file.getSize());

        imageProcessingService.saveProcessedImage(requestId, imageId, data);

        var saved = processedImageRepository.findAll().get(0);
        assertNotNull(saved);
        assertEquals(requestId, saved.getId().getRequestId());
        assertEquals(imageId, saved.getId().getImageId());
        assertTrue(storageRepository.isObjectExist(saved.getProcessedImageId()));
        assertEquals((int) file.getSize(), saved.getSizeBytes());
    }
}
