package com.ambev.order.infrastructure.exception;

public class OrderNotFoundException extends BusinessException {
    public OrderNotFoundException(String id) {
        super("Order not found with ID: " + id);
    }
}
