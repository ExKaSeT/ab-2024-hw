package edu.example.springmvcdemo.service;

import edu.example.springmvcdemo.dao.ProcessedImageRepository;
import edu.example.springmvcdemo.dao.StorageRepository;
import edu.example.springmvcdemo.dto.processor.StreamDataDto;
import edu.example.springmvcdemo.model.ProcessedImage;
import edu.example.springmvcdemo.model.ProcessedImageId;
import jakarta.annotation.Nullable;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ImageProcessingService {
    private final ProcessedImageRepository processedImageRepository;
    private final StorageRepository storageRepository;

    public ProcessedImage saveProcessedEvent(String requestId, String imageId,
                                             @Nullable String processedId, @Nullable Integer sizeBytes) {
        var id = new ProcessedImageId();
        id.setImageId(imageId);
        id.setRequestId(requestId);
        var processed = new ProcessedImage();
        processed.setId(id);
        processed.setProcessedImageId(processedId);
        processed.setSizeBytes(sizeBytes);
        return processedImageRepository.save(processed);
    }

    public Optional<ProcessedImage> getProcessedEvent(@Nullable String requestId, @Nullable String imageId) {
        var id = new ProcessedImageId();
        id.setImageId(imageId);
        id.setRequestId(requestId);
        return processedImageRepository.findById(id);
    }

    @Transactional
    public ProcessedImage saveProcessedImage(String requestId, String imageId, StreamDataDto processedImage) {
        var processedId = storageRepository.save(processedImage.getStream(), processedImage.getSize());
        return saveProcessedEvent(requestId, imageId, processedId, (int) processedImage.getSize());
    }
}
