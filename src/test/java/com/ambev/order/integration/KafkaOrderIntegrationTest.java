package com.ambev.order.integration;

import com.ambev.order.infrastructure.dto.OrderItemDTO;
import com.ambev.order.infrastructure.dto.OrderRequestDTO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@EnableKafka
@SpringBootTest
@Testcontainers
@DirtiesContext
class KafkaOrderIntegrationTest {

    @Container
    static final KafkaContainer kafka = new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.6.0"));

    @DynamicPropertySource
    static void kafkaProps(DynamicPropertyRegistry registry) {
        registry.add("spring.kafka.bootstrap-servers", kafka::getBootstrapServers);
    }

    @Autowired
    private KafkaTemplate<String, OrderRequestDTO> kafkaTemplate;

    private final String topic = "order.created";

    private final CountDownLatch latch = new CountDownLatch(1);

    private final List<OrderRequestDTO> receivedEvents = new ArrayList<>();

    @Test
    void should_publish_and_consume_order_created_event() throws InterruptedException {
        var dto = new OrderRequestDTO("event-123", List.of(
            new OrderItemDTO("prod1", 1, new BigDecimal("10.0"))
        ));

        kafkaTemplate.send(topic, dto.orderId(), dto);

        boolean consumed = latch.await(10, TimeUnit.SECONDS);

        assertThat(consumed).isTrue();
        assertThat(receivedEvents).hasSize(1);
        assertThat(receivedEvents.get(0).orderId()).isEqualTo("event-123");
    }

    @KafkaListener(topics = "order.created", groupId = "test-group")
    public void consume(OrderRequestDTO dto) {
        receivedEvents.add(dto);
        latch.countDown();
    }
}

