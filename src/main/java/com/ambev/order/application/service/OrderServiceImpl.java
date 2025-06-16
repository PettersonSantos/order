package com.ambev.order.application.service;


import com.ambev.order.domain.model.Order;
import com.ambev.order.domain.model.OrderItem;
import com.ambev.order.domain.repository.OrderRepository;
import com.ambev.order.domain.repository.ReadOnlyOrderRepository;
import com.ambev.order.domain.service.OrderService;
import com.ambev.order.infrastructure.dto.OrderRequestDTO;
import com.ambev.order.infrastructure.exception.OrderAlreadyExistsException;
import com.ambev.order.infrastructure.exception.OrderNotFoundException;
import com.ambev.order.infrastructure.persistence.RedisOrderRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

@Slf4j
@Service
public class OrderServiceImpl implements OrderService {

    private final OrderRepository repository;
    private final ReadOnlyOrderRepository cache;
    private final ReadOnlyOrderRepository mongo;

    public OrderServiceImpl(OrderRepository repository,
                            @Qualifier("cache") ReadOnlyOrderRepository cache,
                            @Qualifier("mongo") ReadOnlyOrderRepository mongo) {
        this.repository = repository;
        this.cache = cache;
        this.mongo = mongo;
    }

    @Override
    public void process(OrderRequestDTO dto) {
        if (repository.existsById(dto.orderId())) {
            throw new OrderAlreadyExistsException(dto.orderId());
        }

        var items = dto.items().stream().map(i -> {
            var item = new OrderItem();
            item.setProductCode(i.productCode());
            item.setQuantity(i.quantity());
            item.setUnitPrice(i.unitPrice());
            return item;
        }).toList();

        var total = items.stream()
            .map(i -> i.getUnitPrice().multiply(BigDecimal.valueOf(i.getQuantity())))
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        var order = new Order();
        order.setId(dto.orderId());
        order.setItems(items);
        order.setStatus("CALCULATED");
        order.setTotal(total);
        order.setCreatedAt(LocalDateTime.now());

        repository.save(order);
    }

    public Order findById(String id) {
        return cache.findById(id).or(() -> {
            Optional<Order> fromMongo = mongo.findById(id);
            fromMongo.ifPresent(this::cache);
            return fromMongo;
        }).orElseThrow(() -> new OrderNotFoundException(id));
    }

    private void cache(Order order) {
        if (cache instanceof RedisOrderRepository redisRepo) {
            redisRepo.save(order);
        }
    }
}

