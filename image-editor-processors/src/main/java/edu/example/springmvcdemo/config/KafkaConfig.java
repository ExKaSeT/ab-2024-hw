package edu.example.springmvcdemo.config;

import edu.example.springmvcdemo.processor_async.AsyncImageProcessor;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.RoundRobinPartitioner;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.KafkaListenerContainerFactory;
import org.springframework.kafka.core.*;
import org.springframework.kafka.listener.CommonErrorHandler;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.converter.BatchMessagingMessageConverter;
import org.springframework.kafka.support.converter.ByteArrayJsonMessageConverter;
import org.springframework.kafka.support.converter.RecordMessageConverter;
import org.springframework.util.backoff.ExponentialBackOff;
import java.util.Map;
import java.util.function.Consumer;

@Configuration
@RequiredArgsConstructor
@ConditionalOnProperty(name = "spring.kafka.enable", havingValue = "true")
public class KafkaConfig {

    @Value("${spring.kafka.topic-name.images-done}")
    private String imagesDoneTopicName;
    @Value("${spring.kafka.topic-name.images-wip}")
    private String imagesWipTopicName;

    private final KafkaProperties properties;

    @Bean
    public KafkaTemplate<Object, Object> allAcksKafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }

    @Bean
    public NewTopic imagesWipTopic() {
        return new NewTopic(imagesWipTopicName, 3, (short) 2);
    }

    @Bean
    public NewTopic imagesDoneTopic() {
        return new NewTopic(imagesDoneTopicName, 2, (short) 2);
    }

    @Bean
    public KafkaListenerContainerFactory<ConcurrentMessageListenerContainer<Object, Object>> kafkaListenerContainerFactory(
            RecordMessageConverter converter,
            AsyncImageProcessor imageProcessor,
            ProcessorTypeConfig processorTypeConfig,
            CommonErrorHandler errorHandler
    ) {
        var factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory(props -> props.putAll(Map.of(
                ConsumerConfig.MAX_POLL_RECORDS_CONFIG, imageProcessor.simultaneousTasksOptimalCount(),
                ConsumerConfig.GROUP_ID_CONFIG, processorTypeConfig.getProcessorName()
        ))));
        factory.setCommonErrorHandler(errorHandler);
        factory.setConcurrency(1);
        factory.setRecordMessageConverter(converter);
        factory.setBatchMessageConverter(new BatchMessagingMessageConverter(converter));
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.BATCH);
        factory.setBatchListener(true);
        return factory;
    }

    @Bean
    public DefaultErrorHandler handler() {
        /*
        * Infinitive retrying for guaranteed sending FAILED event to kafka
        * */
        ExponentialBackOff bo = new ExponentialBackOff();
        bo.setInitialInterval(200L);
        bo.setMultiplier(2.0);
        bo.setMaxInterval(5_000L);
        return new DefaultErrorHandler(bo);
    }

    @Bean
    public RecordMessageConverter converter() {
        return new ByteArrayJsonMessageConverter();
    }

    private ProducerFactory<Object, Object> producerFactory() {
        var props = properties.buildProducerProperties(null);
        props.put(ProducerConfig.PARTITIONER_CLASS_CONFIG, RoundRobinPartitioner.class);
        props.put(ProducerConfig.LINGER_MS_CONFIG, 0);
        props.put(ProducerConfig.RETRIES_CONFIG, 1);
        props.put(ProducerConfig.ACKS_CONFIG, "all");
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, "org.springframework.kafka.support.serializer.JsonSerializer");
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, "org.springframework.kafka.support.serializer.JsonSerializer");
        return new DefaultKafkaProducerFactory<>(props);
    }

    private ConsumerFactory<Object, Object> consumerFactory(Consumer<Map<String, Object>> enchanter) {
        var props = properties.buildProducerProperties(null);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(ConsumerConfig.ISOLATION_LEVEL_CONFIG, "read_committed");
        props.put(ConsumerConfig.PARTITION_ASSIGNMENT_STRATEGY_CONFIG, "org.apache.kafka.clients.consumer.CooperativeStickyAssignor");
        props.put(ConsumerConfig.FETCH_MAX_WAIT_MS_CONFIG, "500");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.ByteArrayDeserializer");
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.ByteArrayDeserializer");
        enchanter.accept(props);
        return new DefaultKafkaConsumerFactory<>(props);
    }
}
