package com.ambev.order.infrastructure.controller;

import com.ambev.order.domain.event.publisher.OrderEventPublisher;
import com.ambev.order.domain.model.Order;
import com.ambev.order.domain.service.OrderService;
import com.ambev.order.infrastructure.dto.OrderRequestDTO;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@Slf4j
@RestController
@RequestMapping("/orders")
public class OrderController {

    private final OrderService processor;
    private final OrderEventPublisher eventPublisher;

    public OrderController(OrderService processor, OrderEventPublisher eventPublisher) {
        this.processor = processor;
        this.eventPublisher = eventPublisher;
    }

    @PostMapping
    public ResponseEntity<Void> createOrder(@RequestBody @Valid OrderRequestDTO dto) {
        log.info("Received order: {}", dto);
        eventPublisher.publishOrderCreated(dto);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Order> findById(@PathVariable String id) {
        Order order = processor.findById(id);
        return ResponseEntity.ok(order);
    }
}