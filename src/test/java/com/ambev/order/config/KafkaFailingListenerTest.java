package com.ambev.order.config;

import com.ambev.order.infrastructure.dto.OrderRequestDTO;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.concurrent.CountDownLatch;

@Component
@Profile("test")
public class KafkaFailingListenerTest {

    public static final CountDownLatch latch = new CountDownLatch(1);

    @KafkaListener(topics = "order.created", groupId = "retry-group")
    public void listen(OrderRequestDTO dto) {
        latch.countDown();
        throw new RuntimeException("Simulated failure");
    }
}


