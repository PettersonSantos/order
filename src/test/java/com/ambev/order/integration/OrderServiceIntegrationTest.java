package com.ambev.order.integration;

import com.ambev.order.domain.repository.OrderRepository;
import com.ambev.order.domain.service.OrderService;
import com.ambev.order.infrastructure.dto.OrderItemDTO;
import com.ambev.order.infrastructure.dto.OrderRequestDTO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Testcontainers
class OrderServiceIntegrationTest {

    @Container
    static final MongoDBContainer mongo = new MongoDBContainer("mongo:7.0");

    @Container
    static final GenericContainer<?> redis = new GenericContainer<>("redis:7.2")
        .withExposedPorts(6379);

    @DynamicPropertySource
    static void overrideProps(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", mongo::getReplicaSetUrl);
        registry.add("spring.data.redis.host", redis::getHost);
        registry.add("spring.data.redis.port", () -> redis.getMappedPort(6379));
    }

    @Autowired
    OrderService orderService;
    @Autowired
    OrderRepository repository;

    @Test
    void should_calculate_total_and_persist_order() {
        var dto = new OrderRequestDTO("test-001", List.of(
            new OrderItemDTO("prod-1", 2, new BigDecimal("5.00")),
            new OrderItemDTO("prod-2", 1, new BigDecimal("10.00"))
        ));

        orderService.process(dto);

        var saved = repository.findById("test-001");
        assertThat(saved).isPresent();
        assertThat(saved.get().getTotal()).isEqualTo(new BigDecimal("20.00"));
    }
}
