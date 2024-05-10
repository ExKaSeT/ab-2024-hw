package edu.example.springmvcdemo.service;

import edu.example.springmvcdemo.dao.ImageProcessingRepository;
import edu.example.springmvcdemo.dto.image_processing.ImageDoneDto;
import edu.example.springmvcdemo.dto.image_processing.ImageWipDto;
import edu.example.springmvcdemo.model.ImageProcessing;
import edu.example.springmvcdemo.model.ImageProcessingFilter;
import edu.example.springmvcdemo.model.ImageProcessingStatus;
import lombok.RequiredArgsConstructor;
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

    private final ImageService imageService;
    private final ImageProcessingRepository imageProcessingRepository;
    private final KafkaTemplate<Object, Object> allAcksKafkaTemplate;
    private final ImageProcessingService imageProcessingService;

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

    @KafkaListener(
            topics = IMAGES_DONE_TOPIC_NAME
    )
    public void processDoneImages(ImageDoneDto imageDoneDto, Acknowledgment acknowledgment) {
        imageProcessingService.processDoneImage(imageDoneDto);
        acknowledgment.acknowledge();
    }
}
