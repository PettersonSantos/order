package com.ambev.order.util;

import com.ambev.order.domain.model.Order;
import com.ambev.order.domain.model.OrderItem;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class TestFactory {

    public static Order createSampleOrder(String id) {
        OrderItem item1 = new OrderItem();
        item1.setProductCode("P001");
        item1.setQuantity(2);
        item1.setUnitPrice(new BigDecimal("10.00"));

        OrderItem item2 = new OrderItem();
        item2.setProductCode("P002");
        item2.setQuantity(1);
        item2.setUnitPrice(new BigDecimal("25.00"));

        Order order = new Order();
        order.setId(id);
        order.setItems(List.of(item1, item2));
        order.setStatus("CALCULATED");
        order.setTotal(new BigDecimal("45.00"));
        order.setCreatedAt(LocalDateTime.now());

        return order;
    }
}

