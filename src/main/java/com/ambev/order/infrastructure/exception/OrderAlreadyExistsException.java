package com.ambev.order.infrastructure.exception;

public class OrderAlreadyExistsException extends BusinessException {
    public OrderAlreadyExistsException(String id) {
        super("Order already exists with ID: " + id);
    }
}