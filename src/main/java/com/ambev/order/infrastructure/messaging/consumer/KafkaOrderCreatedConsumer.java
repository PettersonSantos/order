package com.ambev.order.infrastructure.messaging.consumer;

import com.ambev.order.domain.service.OrderService;
import com.ambev.order.infrastructure.dto.OrderRequestDTO;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class KafkaOrderCreatedConsumer {

    private final OrderService processor;

    public KafkaOrderCreatedConsumer(OrderService processor) {
        this.processor = processor;
    }

    @KafkaListener(
        topics = "#{@kafkaTopicsProperties.topics.orderCreated}",
        groupId = "#{@kafkaTopicsProperties.consumerGroup}",
        containerFactory = "kafkaListenerContainerFactory"
    )
    public void consume(ConsumerRecord<String, OrderRequestDTO> record) {
        log.debug("Receiving message: " + record.value());
        try{
            processor.process(record.value());
        } catch (Exception e) {
            throw e;
        }

    }

    @KafkaListener(topics = "#{@kafkaTopicsProperties.topics.orderCreated}", groupId = "retry-group")
    public void consumeWithFailure(OrderRequestDTO dto) {
        log.debug("Consumindo mensagem com falha: " + dto);
        throw new RuntimeException("Simulated failure");
    }
}
