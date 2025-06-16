package com.ambev.order.infrastructure.config;

import com.ambev.order.infrastructure.dto.OrderRequestDTO;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.TopicPartition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.util.backoff.FixedBackOff;

@Configuration
public class KafkaConsumerConfig {

    /**
     * Container factory with retry + dead-letter topic
     */
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, OrderRequestDTO> kafkaListenerContainerFactory(
        ConsumerFactory<String, OrderRequestDTO> consumerFactory,
        KafkaTemplate<String, OrderRequestDTO> kafkaTemplate
    ) {
        var factory = new ConcurrentKafkaListenerContainerFactory<String, OrderRequestDTO>();
        factory.setConsumerFactory(consumerFactory);
        factory.setCommonErrorHandler(new DefaultErrorHandler(new FixedBackOff(0L, 0)));

        var backOff = new FixedBackOff(5000L, 3);

        var recoverer = new DeadLetterPublishingRecoverer(kafkaTemplate,
            (record, ex) -> new TopicPartition(record.topic() + ".DLT", record.partition()));

        factory.setCommonErrorHandler(new DefaultErrorHandler(recoverer, backOff));
        return factory;
    }
}
