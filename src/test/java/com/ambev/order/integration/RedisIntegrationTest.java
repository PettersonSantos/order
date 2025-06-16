package com.ambev.order.integration;

import com.ambev.order.infrastructure.persistence.RedisOrderRepository;
import com.ambev.order.util.TestFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@ExtendWith(SpringExtension.class)
@SpringBootTest
class RedisIntegrationTest {

    @Container
    static final GenericContainer<?> redis =
        new GenericContainer<>("redis:7.2").withExposedPorts(6379);

    @DynamicPropertySource
    static void redisProps(DynamicPropertyRegistry registry) {
        registry.add("spring.data.redis.host", redis::getHost);
        registry.add("spring.data.redis.port", () -> redis.getMappedPort(6379));
    }

    @Autowired
    RedisOrderRepository redisRepo;

    @Test
    void should_store_and_retrieve_order_in_redis() {
        var order = TestFactory.createSampleOrder("order-321");
        redisRepo.save(order);

        var found = redisRepo.findById("order-321");
        assertThat(found).isPresent();
        assertThat(found.get().getId()).isEqualTo("order-321");
    }

    @Test
    void should_override_cached_order_with_new_one() {
        var order = TestFactory.createSampleOrder("order-999");
        redisRepo.save(order);

        var updated = TestFactory.createSampleOrder("order-999");
        updated.setStatus("PAID");
        redisRepo.save(updated);

        var found = redisRepo.findById("order-999");
        assertThat(found).isPresent();
        assertThat(found.get().getStatus()).isEqualTo("PAID");
    }

}

