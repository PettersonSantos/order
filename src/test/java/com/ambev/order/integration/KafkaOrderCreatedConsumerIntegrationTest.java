package com.ambev.order.integration;

import com.ambev.order.config.KafkaFailingListenerTest;
import com.ambev.order.domain.service.OrderService;
import com.ambev.order.infrastructure.dto.OrderItemDTO;
import com.ambev.order.infrastructure.dto.OrderRequestDTO;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@SpringBootTest
@Testcontainers
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@Import({KafkaOrderCreatedConsumerIntegrationTest.MockConfig.class, KafkaFailingListenerTest.class})
@ActiveProfiles("test")
class KafkaOrderCreatedConsumerIntegrationTest {

    @TestConfiguration
    static class MockConfig {
        @Bean
        @Primary
        public OrderService orderService() {
            return Mockito.mock(OrderService.class);
        }
    }

    @Container
    static final KafkaContainer kafka = new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.6.0"))
        .withStartupTimeout(Duration.ofSeconds(60));

    @BeforeAll
    static void setUp() {
        kafka.start();
        System.out.println("Kafka bootstrap servers: " + kafka.getBootstrapServers());
    }

    @DynamicPropertySource
    static void kafkaProps(DynamicPropertyRegistry registry) {
        registry.add("spring.kafka.bootstrap-servers", kafka::getBootstrapServers);
    }

    @Autowired
    private OrderService orderService;

    @Autowired
    private KafkaTemplate<String, OrderRequestDTO> kafkaTemplate;

    private final String topic = "order.created";

    @Test
    void should_call_order_service_on_message_received() {
        var dto = new OrderRequestDTO("order-ok", List.of(
            new OrderItemDTO("prod1", 1, new BigDecimal("10.0"))
        ));

        kafkaTemplate.send(topic, dto.orderId(), dto);

        Awaitility.await()
            .atMost(5, TimeUnit.SECONDS)
            .untilAsserted(() ->
                verify(orderService, times(1)).process(dto)
            );
    }

    @Test
    void should_handle_exception_and_continue_listening() {
        var dto = new OrderRequestDTO("order-fail", List.of(
            new OrderItemDTO("prod2", 1, new BigDecimal("20.0"))
        ));

        doThrow(new RuntimeException("Simulated failure"))
            .when(orderService).process(dto);

        kafkaTemplate.send(topic, dto.orderId(), dto);

        Awaitility.await()
            .atMost(5, TimeUnit.SECONDS)
            .untilAsserted(() ->
                verify(orderService, times(1)).process(dto)
            );
    }

    @Test
    void should_trigger_consumeWithFailure_and_throw_exception() throws InterruptedException {
        var dto = new OrderRequestDTO("order-exception", List.of(
            new OrderItemDTO("prod3", 2, new BigDecimal("30.0"))
        ));

        kafkaTemplate.send("order.created", dto.orderId(), dto);

        boolean consumed = KafkaFailingListenerTest.latch.await(5, TimeUnit.SECONDS);

        assertThat(consumed)
            .as("Esperava que a mensagem fosse consumida mesmo com erro")
            .isTrue();
    }
}