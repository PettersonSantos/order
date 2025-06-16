package com.ambev.order.infrastructure.persistence.entity;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class OrderItemDocument {
    private String productCode;
    private Integer quantity;
    private BigDecimal unitPrice;
}
