package com.ambev.order.infrastructure.messaging;

import com.ambev.order.domain.service.OrderService;
import com.ambev.order.infrastructure.dto.OrderItemDTO;
import com.ambev.order.infrastructure.dto.OrderRequestDTO;
import com.ambev.order.infrastructure.messaging.consumer.KafkaOrderCreatedConsumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class KafkaOrderCreatedConsumerTest {

    @Test
    void should_process_order_when_received_from_kafka() {
        // Given
        OrderService service = mock(OrderService.class);
        var consumer = new KafkaOrderCreatedConsumer(service);

        var dto = new OrderRequestDTO("order-kafka", List.of(
            new OrderItemDTO("P1", 2, new BigDecimal("15.0"))
        ));

        // When
        ConsumerRecord<String, OrderRequestDTO> record = new ConsumerRecord<>(
            "order-created", 0, 0L, "order-kafka", dto
        );

        consumer.consume(record);

        // Then
        verify(service, times(1)).process(dto);
    }

    @Test
    void should_throw_if_processing_fails() {
        // Given
        OrderService service = mock(OrderService.class);
        var consumer = new KafkaOrderCreatedConsumer(service);

        var dto = new OrderRequestDTO("fail", List.of(
            new OrderItemDTO("P2", 1, new BigDecimal("20.0"))
        ));

        ConsumerRecord<String, OrderRequestDTO> record = new ConsumerRecord<>("order-created", 0, 0L, "fail", dto);

        doThrow(new RuntimeException("Processing failed")).when(service).process(dto);

        // When + Then
        assertThatThrownBy(() -> consumer.consume(record))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("Processing failed");
    }

    @Test
    void should_always_throw_exception_in_consumeWithFailure() {
        // Given
        OrderService service = mock(OrderService.class);
        var consumer = new KafkaOrderCreatedConsumer(service);

        var dto = new OrderRequestDTO("fail-order", List.of(
            new OrderItemDTO("P1", 1, new BigDecimal("10.0"))
        ));

        // When + Then
        assertThatThrownBy(() -> consumer.consumeWithFailure(dto))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("Simulated failure");
    }

}

