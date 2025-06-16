package com.ambev.order.infrastructure.persistence;

import com.ambev.order.domain.model.Order;
import com.ambev.order.domain.repository.ReadOnlyOrderRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.util.Optional;

@Slf4j
@Repository
@Qualifier("cache")
public class RedisOrderRepository implements ReadOnlyOrderRepository {

    private final StringRedisTemplate redis;
    private final ObjectMapper mapper;

    public RedisOrderRepository(StringRedisTemplate redis, ObjectMapper mapper) {
        this.redis = redis;
        this.mapper = mapper;
    }

    @Override
    public Optional<Order> findById(String id) {
        log.debug("Finding order by redis: {}", id);
        String value = redis.opsForValue().get("order:" + id);
        if (value == null) return Optional.empty();

        try {
            return Optional.of(mapper.readValue(value, Order.class));
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    public void save(Order order) {
        try {
            String json = mapper.writeValueAsString(order);
            redis.opsForValue().set("order:" + order.getId(), json, Duration.ofMinutes(10));
        } catch (Exception ignored) {
        }
    }
}
