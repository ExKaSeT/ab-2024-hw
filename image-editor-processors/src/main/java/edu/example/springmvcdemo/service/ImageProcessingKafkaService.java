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

@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "spring.kafka.enable", havingValue = "true")
public class ImageProcessingKafkaService {

    private final KafkaTemplate<Object, Object> allAcksKafkaTemplate;

    @KafkaListener(
            topics = IMAGES_DONE_TOPIC_NAME
    )
    public void processDoneImages(ImageDoneDto imageDoneDto, Acknowledgment acknowledgment) {

        acknowledgment.acknowledge();
    }
}
