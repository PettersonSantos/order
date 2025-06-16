package com.ambev.order.domain.repository;


import com.ambev.order.domain.model.Order;

import java.util.Optional;

public interface OrderRepository {
    boolean existsById(String id);
    void save(Order order);
    Optional<Order> findById(String id);
}
