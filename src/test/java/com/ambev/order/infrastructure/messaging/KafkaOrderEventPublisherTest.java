package com.ambev.order.infrastructure.messaging;

import com.ambev.order.infrastructure.config.KafkaTopicsProperties;
import com.ambev.order.infrastructure.dto.OrderItemDTO;
import com.ambev.order.infrastructure.dto.OrderRequestDTO;
import com.ambev.order.infrastructure.messaging.producer.KafkaOrderEventPublisher;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.TopicPartition;
import org.junit.jupiter.api.Test;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.mockito.Mockito.*;

class KafkaOrderEventPublisherTest {

    @Test
    void should_send_message_to_kafka() {
        // Given
        KafkaTemplate<String, OrderRequestDTO> kafka = mock();
        KafkaTopicsProperties props = new KafkaTopicsProperties();
        var topics = new KafkaTopicsProperties.Topics();
        topics.setOrderCreated("orders.topic");
        props.setTopics(topics);

        var publisher = new KafkaOrderEventPublisher(kafka, props);

        var dto = new OrderRequestDTO("order-1", List.of(new OrderItemDTO("P1", 1, new BigDecimal("10.0"))));

        SendResult<String, OrderRequestDTO> result =
            new SendResult<>(null, new RecordMetadata(new TopicPartition("orders.topic", 0), 0, 0, 0L, 0L, 0, 0));

        CompletableFuture<SendResult<String, OrderRequestDTO>> future = CompletableFuture.completedFuture(result);

        when(kafka.send("orders.topic", "order-1", dto)).thenReturn(future);

        // When
        publisher.publishOrderCreated(dto);

        // Then
        verify(kafka, times(1)).send("orders.topic", "order-1", dto);
    }

    @Test
    void should_log_error_when_publish_fails() {
        // Given
        KafkaTemplate<String, OrderRequestDTO> kafka = mock();
        KafkaTopicsProperties props = new KafkaTopicsProperties();
        var topics = new KafkaTopicsProperties.Topics();
        topics.setOrderCreated("orders.topic");
        props.setTopics(topics);

        var publisher = new KafkaOrderEventPublisher(kafka, props);

        var dto = new OrderRequestDTO("order-error", List.of(
            new OrderItemDTO("ERR", 1, new BigDecimal("10.0"))
        ));

        var future = new CompletableFuture<SendResult<String, OrderRequestDTO>>();
        future.completeExceptionally(new RuntimeException("Kafka send failed"));

        when(kafka.send("orders.topic", "order-error", dto)).thenReturn(future);

        // When
        publisher.publishOrderCreated(dto);

        // Then
        verify(kafka).send("orders.topic", "order-error", dto);
    }

}

