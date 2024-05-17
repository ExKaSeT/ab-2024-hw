package edu.example.springmvcdemo.service;

import edu.example.springmvcdemo.config.ProcessorTypeConfig;
import edu.example.springmvcdemo.dto.image_processing.ImageDoneDto;
import edu.example.springmvcdemo.dto.processor.StreamDataDto;
import edu.example.springmvcdemo.model.ImageProcessingStatus;
import edu.example.springmvcdemo.model.ProcessedImage;
import edu.example.springmvcdemo.processor_async.AsyncImageProcessor;
import edu.example.springmvcdemo.dto.image_processing.ImageWipDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.KafkaException;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.kafka.support.converter.ConversionException;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Service;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import static java.util.Objects.isNull;

@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "spring.kafka.enable", havingValue = "true")
public class ImageProcessingKafkaService {

    private static final int IMAGE_PROCESSING_MAX_WAIT_SEC = 10;

    @Value("${spring.kafka.topic-name.images-done}")
    private String imagesDoneTopicName;
    @Value("${spring.kafka.topic-name.images-wip}")
    private String imagesWipTopicName;

    private final AsyncImageProcessor processor;
    private final ImageProcessingService imageProcessingService;
    private final KafkaTemplate<Object, Object> allAcksKafkaTemplate;
    private final ProcessorTypeConfig processorTypeConfig;


    /*
    * Сделал парализацию батча, т.к. парализация consumer быстро забьет все свободные партиции
    *
    * Пока события об обработке / ошибке не будут отправлены, обработка партии будет продолжаться
    * */
    @KafkaListener(
            topics = "${spring.kafka.topic-name.images-wip}"
    )
    public void processImages(List<ImageWipDto> imageWipDto,
                              @Header(KafkaHeaders.CONVERSION_FAILURES) List<ConversionException> exceptions) {
        Map<Integer, Future<StreamDataDto>> tasks = new HashMap<>();
        Map<Integer, ProcessedImage> eventsToSend = new HashMap<>();
        for (int index = 0; index < imageWipDto.size(); index++) {
            var imageDto = imageWipDto.get(index);

            // checks deserialization exceptions
            if (isNull(imageDto) && exceptions.get(index) != null) {
                log.warn("Conversion error", exceptions.get(index));
                continue;
            }

            // checks need to process
            try {
                if (!processorTypeConfig.getProcessorName()
                        .equalsIgnoreCase(imageDto.getFilters().get(0).toString())) {
                    continue;
                }
            } catch (Exception ex) {
                log.warn("Unexpected error", ex);
                continue;
            }

            // checks already processed
            var alreadyProcessed = imageProcessingService
                    .getProcessedEvent(imageDto.getRequestId(), imageDto.getImageId());
            if (alreadyProcessed.isPresent()) {
                eventsToSend.put(index, alreadyProcessed.get());
                continue;
            }

            tasks.put(index, processor.process(imageDto.getImageId(), imageDto.getExtension().toString()));
        }

        // processing
        for (var entry : tasks.entrySet()) {
            var imageDtoIndex = entry.getKey();
            var imageDto = imageWipDto.get(imageDtoIndex);
            var task = entry.getValue();
            StreamDataDto result;
            ProcessedImage processed;
            try {
                result = task.get(IMAGE_PROCESSING_MAX_WAIT_SEC, TimeUnit.SECONDS);
                processed = imageProcessingService.saveProcessedImage(imageDto.getRequestId(), imageDto.getImageId(), result);
            } catch (Exception e) {
                processed = imageProcessingService.saveProcessedEvent(imageDto.getRequestId(), imageDto.getImageId(), null, null);
            }
            eventsToSend.put(imageDtoIndex, processed);
        }

        try {
            for (var entry : eventsToSend.entrySet()) {
                var imageDto = imageWipDto.get(entry.getKey());
                var processed = entry.getValue();

                // failed processing
                if (isNull(processed.getProcessedImageId())) {
                    allAcksKafkaTemplate.send(imagesDoneTopicName,
                                    new ImageDoneDto(null, processed.getId().getRequestId(),
                                            null, ImageProcessingStatus.FAILED))
                            .get(1, TimeUnit.SECONDS);
                    continue;
                }

                var filters = imageDto.getFilters();
                filters.remove(0);
                if (filters.isEmpty()) {
                    allAcksKafkaTemplate.send(imagesDoneTopicName,
                                    new ImageDoneDto(processed.getProcessedImageId(), processed.getId().getRequestId(),
                                            processed.getSizeBytes(), ImageProcessingStatus.DONE))
                            .get(1, TimeUnit.SECONDS);
                } else {
                    allAcksKafkaTemplate.send(imagesWipTopicName,
                                    new ImageWipDto(processed.getProcessedImageId(), processed.getId().getRequestId(),
                                            filters, imageDto.getExtension()))
                            .get(1, TimeUnit.SECONDS);
                }
            }
            allAcksKafkaTemplate.flush();
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            throw new KafkaException("Unable to send to Kafka", e);
        }
    }
}
