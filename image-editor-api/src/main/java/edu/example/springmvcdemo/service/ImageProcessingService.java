package edu.example.springmvcdemo.service;

import edu.example.springmvcdemo.dao.ImageProcessingRepository;
import edu.example.springmvcdemo.dao.StorageRepository;
import edu.example.springmvcdemo.dto.image.GetModifiedImageByRequestIdResponseDto;
import edu.example.springmvcdemo.dto.image_processing.ImageDoneDto;
import edu.example.springmvcdemo.exception.EntityNotFoundException;
import edu.example.springmvcdemo.model.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

@Service
@RequiredArgsConstructor
public class ImageProcessingService {

    private final ImageService imageService;
    private final ImageProcessingRepository imageProcessingRepository;
    private final StorageRepository storageRepository;

    public GetModifiedImageByRequestIdResponseDto getImageProcessingStatus(String requestId) {
        var imageProcessing = imageProcessingRepository.findById(requestId).orElseThrow(() -> new EntityNotFoundException("Request not found"));
        var result = new GetModifiedImageByRequestIdResponseDto();
        var imageId = imageProcessing.getProcessedImage();
        if (isNull(imageId)) {
            imageId = imageProcessing.getOriginalImage().getLink();
        }
        result.setImageId(imageId);
        result.setStatus(imageProcessing.getStatus());
        return result;
    }

    @Transactional
    public void processDoneImage(ImageDoneDto imageDoneDto) {
        var imageProcessingOpt = imageProcessingRepository.findById(imageDoneDto.getRequestId());
        var processedImageId = imageDoneDto.getImageId();

        // checks request existence
        if (imageProcessingOpt.isEmpty()) {
            return;
        }

        // checks already processed
        var imageProcessing = imageProcessingOpt.get();
        if (nonNull(imageProcessing.getProcessedImage()) ||
                imageProcessing.getStatus().equals(ImageProcessingStatus.FAILED)) {
            return;
        }

        var newStatus = imageDoneDto.getStatus();
        imageProcessing.setProcessedImage(processedImageId);
        imageProcessing.setStatus(newStatus);
        var processed = imageProcessingRepository.save(imageProcessing);

        // checks failed
        if (newStatus.equals(ImageProcessingStatus.FAILED)) {
            return;
        }

        var original = processed.getOriginalImage();
        imageService.saveMeta(processedImageId, original.getOriginalName(),
                imageDoneDto.getSizeBytes(), original.getUser());
        // remove tags to make object non-temporary
        storageRepository.removeObjectTags(processedImageId);
    }

    public ImageProcessing createImageProcessingRecord(String requestId, String originalImageId,
                                                       ImageProcessingStatus status) {
        var imageProcessing = new ImageProcessing();
        imageProcessing.setOriginalImage(imageService.getMeta(originalImageId));
        imageProcessing.setRequestId(requestId);
        imageProcessing.setStatus(status);
        return imageProcessingRepository.save(imageProcessing);
    }
}
