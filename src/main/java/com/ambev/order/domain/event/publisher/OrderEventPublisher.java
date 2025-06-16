package com.ambev.order.domain.event.publisher;

import com.ambev.order.infrastructure.dto.OrderRequestDTO;

public interface OrderEventPublisher {
    void publishOrderCreated(OrderRequestDTO dto);
}
