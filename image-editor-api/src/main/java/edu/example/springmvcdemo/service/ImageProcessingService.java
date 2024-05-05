package edu.example.springmvcdemo.service;

import edu.example.springmvcdemo.dao.ImageProcessingRepository;
import edu.example.springmvcdemo.dao.StorageRepository;
import edu.example.springmvcdemo.dto.image.GetModifiedImageByRequestIdResponseDto;
import edu.example.springmvcdemo.dto.image_processing.ImageDoneDto;
import edu.example.springmvcdemo.dto.image_processing.ImageWipDto;
import edu.example.springmvcdemo.exception.EntityNotFoundException;
import edu.example.springmvcdemo.model.*;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.UUID;
import static edu.example.springmvcdemo.config.KafkaInitializer.IMAGES_DONE_TOPIC_NAME;
import static edu.example.springmvcdemo.config.KafkaInitializer.IMAGES_WIP_TOPIC_NAME;
import static java.util.Objects.isNull;

@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "spring.kafka.enable", havingValue = "true")
public class ImageProcessingService {

    private final ImageService imageService;
    private final ImageProcessingRepository imageProcessingRepository;
    private final KafkaTemplate<Object, Object> allAcksKafkaTemplate;
    private final StorageRepository storageRepository;

    /*
    * Худший редкий кейс - транзакция не закомитится в бд и сообщение отправится в кафку ->
    * в consumer надо проверять существует ли такой requestId
    * */
    @Transactional
    public String createApplyFiltersRequest(String imageId, List<ImageProcessingFilter> filters) {
        var imageDb = new ImageProcessing();
        imageDb.setOriginalImage(imageService.getMeta(imageId));
        var requestId = UUID.randomUUID().toString();
        imageDb.setRequestId(requestId);
        imageDb.setStatus(ImageProcessingStatus.WIP);
        imageProcessingRepository.save(imageDb);

        var imageKafka = new ImageWipDto();
        imageKafka.setImageId(imageId);
        imageKafka.setRequestId(requestId);
        imageKafka.setFilters(filters);
        allAcksKafkaTemplate.send(IMAGES_WIP_TOPIC_NAME, imageKafka);
        return requestId;
    }

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

    @KafkaListener(
            topics = IMAGES_DONE_TOPIC_NAME,
            groupId = "${app.group-id}",
            concurrency = "2",
            properties = {
                    ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG + "=false",
                    ConsumerConfig.ISOLATION_LEVEL_CONFIG + "=read_committed",
                    ConsumerConfig.PARTITION_ASSIGNMENT_STRATEGY_CONFIG +
                            "=org.apache.kafka.clients.consumer.CooperativeStickyAssignor",
                    ConsumerConfig.FETCH_MAX_WAIT_MS_CONFIG + "=500"
            }
    )
    public void processDoneImages(@Payload ImageDoneDto imageDoneDto, Acknowledgment acknowledgment) {
        var imageProcessingOpt = imageProcessingRepository.findById(imageDoneDto.getRequestId());
        if (imageProcessingOpt.isEmpty()) {
            if (storageRepository.isObjectExist(imageDoneDto.getImageId())) {
                storageRepository.deleteObject(imageDoneDto.getImageId());
            }
            acknowledgment.acknowledge();
            return;
        }
        var imageProcessing = imageProcessingOpt.get();
        imageProcessing.setProcessedImage(imageDoneDto.getImageId());
        imageProcessing.setStatus(ImageProcessingStatus.DONE);
        imageProcessingRepository.save(imageProcessing);

        acknowledgment.acknowledge();
    }
}
