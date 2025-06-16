package com.ambev.order.infrastructure.messaging.producer;

import com.ambev.order.domain.event.publisher.OrderEventPublisher;
import com.ambev.order.infrastructure.config.KafkaTopicsProperties;
import com.ambev.order.infrastructure.dto.OrderRequestDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class KafkaOrderEventPublisher implements OrderEventPublisher {

    private final KafkaTemplate<String, OrderRequestDTO> kafka;
    private final KafkaTopicsProperties properties;

    public KafkaOrderEventPublisher(KafkaTemplate<String, OrderRequestDTO> kafka, KafkaTopicsProperties properties) {
        this.kafka = kafka;
        this.properties = properties;
    }

    @Override
    public void publishOrderCreated(OrderRequestDTO dto) {
        kafka.send(properties.getTopics().getOrderCreated(), dto.orderId(), dto)
            .thenAccept(result -> log.info("Order published successfully: {}", dto.orderId()))
            .exceptionally(ex -> {
                log.error("Failed to publish order: {}", dto.orderId(), ex);
                return null;
            });
    }
}

