package com.ambev.order.domain.repository;

import com.ambev.order.domain.model.Order;

import java.util.Optional;

public interface ReadOnlyOrderRepository {
    Optional<Order> findById(String id);
}

