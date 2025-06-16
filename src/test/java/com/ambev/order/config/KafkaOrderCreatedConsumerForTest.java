package com.ambev.order.config;

import com.ambev.order.domain.service.OrderService;
import com.ambev.order.infrastructure.dto.OrderRequestDTO;
import com.ambev.order.infrastructure.messaging.consumer.KafkaOrderCreatedConsumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Profile("test")
@Component
class KafkaOrderCreatedConsumerForTest extends KafkaOrderCreatedConsumer {

    public KafkaOrderCreatedConsumerForTest(OrderService processor) {
        super(processor);
    }

    @KafkaListener(
        topics = "order.created",
        groupId = "test-group",
        containerFactory = "kafkaListenerContainerFactory"
    )
    public void consumeForTest(ConsumerRecord<String, OrderRequestDTO> record) {
        super.consume(record);
    }
}

