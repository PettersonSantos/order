package com.ambev.order.domain.model;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class Order {
    private String id;
    private List<OrderItem> items;
    private BigDecimal total;
    private String status;
    private LocalDateTime createdAt;
}
