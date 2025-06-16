package com.ambev.order.domain.service;

import com.ambev.order.domain.model.Order;
import com.ambev.order.infrastructure.dto.OrderRequestDTO;

public interface OrderService {
    void process(OrderRequestDTO dto);
    Order findById(String id);
}

