package edu.example.springmvcdemo.service;

import edu.example.springmvcdemo.processor_async.AsyncImageProcessor;
import edu.example.springmvcdemo.dto.image_processing.ImageWipDto;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "spring.kafka.enable", havingValue = "true")
public class ImageProcessingKafkaService {

    @Value("${spring.kafka.topic-name.images-done}")
    private String imagesDoneTopicName;
    @Value("${spring.kafka.topic-name.images-wip}")
    private String imagesWipTopicName;

    private final AsyncImageProcessor processor;
    private final KafkaTemplate<Object, Object> allAcksKafkaTemplate;

    @KafkaListener(
            topics = "${spring.kafka.topic-name.images-wip}"
    )
    public void processImages(List<ImageWipDto> imageWipDto, Acknowledgment acknowledgment) {


        acknowledgment.acknowledge();
    }
}
