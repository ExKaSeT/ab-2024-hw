package edu.example.springmvcdemo.config;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.RoundRobinPartitioner;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.converter.ByteArrayJsonMessageConverter;
import org.springframework.kafka.support.converter.RecordMessageConverter;
import java.util.Map;
import java.util.function.Consumer;

@Configuration
@RequiredArgsConstructor
@ConditionalOnProperty(name = "spring.kafka.enable", havingValue = "true")
public class KafkaInitializer {
    @Getter
    @Value("${kafka.topic-name.images-wip}")
    private String imagesWipTopicName;
    @Getter
    @Value("${kafka.topic-name.images-done}")
    private String imagesDoneTopicName;

    private final KafkaProperties properties;

    @Bean
    public KafkaTemplate<Object, Object> allAcksKafkaTemplate() {
        return new KafkaTemplate<>(producerFactory(props -> props.put(ProducerConfig.ACKS_CONFIG, "all")));
    }

    @Bean
    public RecordMessageConverter converter() {
        return new ByteArrayJsonMessageConverter();
    }

    private ProducerFactory<Object, Object> producerFactory(Consumer<Map<String, Object>> enchanter) {
        var props = properties.buildProducerProperties(null);
        props.put(ProducerConfig.PARTITIONER_CLASS_CONFIG, RoundRobinPartitioner.class);
        props.put(ProducerConfig.LINGER_MS_CONFIG, 0);
        props.put(ProducerConfig.RETRIES_CONFIG, 1);
        enchanter.accept(props);
        return new DefaultKafkaProducerFactory<>(props);
    }
}
