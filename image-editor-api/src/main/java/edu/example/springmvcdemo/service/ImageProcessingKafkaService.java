package edu.example.springmvcdemo.service;

import edu.example.springmvcdemo.config.AllowedImageExtension;
import edu.example.springmvcdemo.dto.image_processing.ImageDoneDto;
import edu.example.springmvcdemo.dto.image_processing.ImageWipDto;
import edu.example.springmvcdemo.model.ImageProcessingFilter;
import edu.example.springmvcdemo.model.ImageProcessingStatus;
import lombok.RequiredArgsConstructor;
import org.apache.curator.shaded.com.google.common.io.Files;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.UUID;
import static edu.example.springmvcdemo.config.KafkaConfig.IMAGES_DONE_TOPIC_NAME;
import static edu.example.springmvcdemo.config.KafkaConfig.IMAGES_WIP_TOPIC_NAME;

@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "spring.kafka.enable", havingValue = "true")
public class ImageProcessingKafkaService {

    private final KafkaTemplate<Object, Object> allAcksKafkaTemplate;
    private final ImageProcessingService imageProcessingService;

    /*
    * Худший редкий кейс - транзакция не закомитится в бд и сообщение отправится в кафку ->
    * в consumer надо проверять существует ли такой requestId
    * */
    @Transactional
    public String createApplyFiltersRequest(String imageId, List<ImageProcessingFilter> filters) {
        var requestId = UUID.randomUUID().toString();
        var processingRecord = imageProcessingService.createImageProcessingRecord(requestId, imageId, ImageProcessingStatus.WIP);

        var extensionString = Files.getFileExtension(processingRecord.getOriginalImage().getOriginalName());
        AllowedImageExtension extension;
        try {
            extension = AllowedImageExtension.valueOf(extensionString.toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new IllegalStateException("Image extension not allowed");
        }
        var imageWip = new ImageWipDto();
        imageWip.setImageId(imageId);
        imageWip.setRequestId(requestId);
        imageWip.setFilters(filters);
        imageWip.setExtension(extension);
        allAcksKafkaTemplate.send(IMAGES_WIP_TOPIC_NAME, imageWip);
        return requestId;
    }

    @KafkaListener(
            topics = IMAGES_DONE_TOPIC_NAME
    )
    public void processDoneImages(ImageDoneDto imageDoneDto, Acknowledgment acknowledgment) {
        imageProcessingService.processDoneImage(imageDoneDto);
        acknowledgment.acknowledge();
    }
}
