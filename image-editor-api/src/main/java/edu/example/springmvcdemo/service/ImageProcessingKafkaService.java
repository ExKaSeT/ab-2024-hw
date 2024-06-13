package edu.example.springmvcdemo.service;

import com.giffing.bucket4j.spring.boot.starter.context.RateLimiting;
import edu.example.springmvcdemo.config.AllowedImageExtension;
import edu.example.springmvcdemo.dto.image_processing.ImageDoneDto;
import edu.example.springmvcdemo.dto.image_processing.ImageWipDto;
import edu.example.springmvcdemo.model.ImageProcessingFilter;
import edu.example.springmvcdemo.model.ImageProcessingStatus;
import lombok.RequiredArgsConstructor;
import org.apache.curator.shaded.com.google.common.io.Files;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.dao.RecoverableDataAccessException;
import org.springframework.dao.TransientDataAccessException;
import org.springframework.kafka.KafkaException;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

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
    @RateLimiting(
            name = "apply-filters-limit",
            cacheKey= "@userDetailsServiceImpl.getUsername()",
            ratePerMethod = true
    )
    @Transactional
    @Retryable(retryFor = {KafkaException.class, RecoverableDataAccessException.class,
            TransientDataAccessException.class}, backoff = @Backoff(delay = 500))
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
        try {
            allAcksKafkaTemplate.send(IMAGES_WIP_TOPIC_NAME, imageWip).get(1, TimeUnit.SECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            throw new KafkaException("Unable to send to kafka", e);
        }
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
