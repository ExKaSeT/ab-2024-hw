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
import java.util.Objects;
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
        if (imageProcessingOpt.isEmpty()) {
            if (storageRepository.isObjectExist(processedImageId)) {
                storageRepository.deleteObject(processedImageId);
            }
            return;
        }

        var imageProcessing = imageProcessingOpt.get();
        if (nonNull(imageProcessing.getProcessedImage())) {
            if (!Objects.equals(processedImageId, imageProcessing.getProcessedImage()) &&
                    storageRepository.isObjectExist(processedImageId)) {
                storageRepository.deleteObject(processedImageId);
            }
            return;
        }

        imageProcessing.setProcessedImage(processedImageId);
        imageProcessing.setStatus(ImageProcessingStatus.DONE);
        var processed = imageProcessingRepository.save(imageProcessing);
        var original = processed.getOriginalImage();
        imageService.saveMeta(processed.getProcessedImage(), original.getOriginalName(),
                imageDoneDto.getSizeBytes(), original.getUser());
    }
}
