package com.ambev.order.infrastructure.persistence;


import com.ambev.order.domain.model.Order;
import com.ambev.order.domain.model.OrderItem;
import com.ambev.order.domain.repository.OrderRepository;
import com.ambev.order.domain.repository.ReadOnlyOrderRepository;
import com.ambev.order.infrastructure.persistence.entity.OrderDocument;
import com.ambev.order.infrastructure.persistence.entity.OrderItemDocument;
import com.ambev.order.infrastructure.persistence.mongo.MongoOrderSpringRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Repository
@Qualifier("mongo")
public class MongoOrderRepository implements OrderRepository, ReadOnlyOrderRepository {

    private final MongoOrderSpringRepository repository;

    public MongoOrderRepository(MongoOrderSpringRepository repository) {
        this.repository = repository;
    }

    @Override
    public boolean existsById(String id) {
        return repository.existsById(id);
    }

    @Override
    public void save(Order order) {
        log.debug("Saving order: {}", order);

        OrderDocument doc = new OrderDocument();
        doc.setId(order.getId());
        doc.setStatus(order.getStatus());
        doc.setTotal(order.getTotal());
        doc.setCreatedAt(order.getCreatedAt());
        doc.setItems(order.getItems().stream()
            .map(i -> new OrderItemDocument(i.getProductCode(), i.getQuantity(), i.getUnitPrice()))
            .collect(Collectors.toList()));

        repository.insert(doc);
    }

    @Override
    public Optional<Order> findById(String id) {
        log.debug("Finding order by mongo: {}", id);

        return repository.findById(id).
            map(this::mapToDomain);

    }

    private Order mapToDomain(OrderDocument doc) {
        Order order = new Order();
        order.setId(doc.getId());
        order.setStatus(doc.getStatus());
        order.setTotal(doc.getTotal());
        order.setCreatedAt(doc.getCreatedAt());
        order.setItems(
            doc.getItems().stream().map(i -> {
                OrderItem item = new OrderItem();
                item.setProductCode(i.getProductCode());
                item.setQuantity(i.getQuantity());
                item.setUnitPrice(i.getUnitPrice());
                return item;
            }).toList()
        );
        return order;
    }

}
