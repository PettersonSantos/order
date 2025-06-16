package com.ambev.order.infrastructure.persistence.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Document(collection = "orders")
@Data
public class OrderDocument {
    @Id
    private String id;
    private List<OrderItemDocument> items;
    private BigDecimal total;
    private String status;
    private LocalDateTime createdAt;
}
