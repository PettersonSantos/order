package com.ambev.order.application.service;

import com.ambev.order.domain.model.Order;
import com.ambev.order.domain.repository.OrderRepository;
import com.ambev.order.domain.repository.ReadOnlyOrderRepository;
import com.ambev.order.infrastructure.dto.OrderItemDTO;
import com.ambev.order.infrastructure.dto.OrderRequestDTO;
import com.ambev.order.infrastructure.exception.BusinessException;
import com.ambev.order.infrastructure.exception.OrderAlreadyExistsException;
import com.ambev.order.infrastructure.exception.OrderNotFoundException;
import com.ambev.order.infrastructure.persistence.RedisOrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class OrderServiceImplTest {

    private OrderRepository repository;
    private ReadOnlyOrderRepository cache;
    private ReadOnlyOrderRepository mongo;
    private OrderServiceImpl service;

    @BeforeEach
    void setUp() {
        repository = mock(OrderRepository.class);
        cache = mock(ReadOnlyOrderRepository.class);
        mongo = mock(ReadOnlyOrderRepository.class);
        service = new OrderServiceImpl(repository, cache, mongo);
    }

    @Test
    void should_process_and_save_order_when_not_duplicate() {
        // Given
        var dto = new OrderRequestDTO(
            "order-123",
            List.of(
                new OrderItemDTO("prod-1", 2, new BigDecimal("10.00")),
                new OrderItemDTO("prod-2", 1, new BigDecimal("5.00"))
            )
        );
        when(repository.existsById("order-123")).thenReturn(false);

        // When
        service.process(dto);

        // Then
        ArgumentCaptor<Order> captor = ArgumentCaptor.forClass(Order.class);
        verify(repository).save(captor.capture());
        Order saved = captor.getValue();

        assertThat(saved.getId()).isEqualTo("order-123");
        assertThat(saved.getItems()).hasSize(2);
        assertThat(saved.getTotal()).isEqualByComparingTo("25.00");
        assertThat(saved.getStatus()).isEqualTo("CALCULATED");
        assertThat(saved.getCreatedAt()).isNotNull();
    }

    @Test
    void should_throw_exception_when_order_already_exists() {
        // Given
        var dto = new OrderRequestDTO("order-dup", List.of());
        when(repository.existsById("order-dup")).thenReturn(true);

        // Then
        assertThatThrownBy(() -> service.process(dto))
            .isInstanceOf(OrderAlreadyExistsException.class)
            .hasMessageContaining("order-dup");

        verify(repository, never()).save(any());
    }

    @Test
    void should_return_order_from_cache_if_present() {
        var order = new Order();
        order.setId("cached-id");

        when(cache.findById("cached-id")).thenReturn(Optional.of(order));

        var result = service.findById("cached-id");

        assertThat(result).isEqualTo(order);
        verifyNoInteractions(mongo);
    }

    @Test
    void should_return_order_from_mongo_and_cache_it() {
        var order = new Order();
        order.setId("mongo-id");

        when(cache.findById("mongo-id")).thenReturn(Optional.empty());
        when(mongo.findById("mongo-id")).thenReturn(Optional.of(order));

        // Spy Redis repository to verify cache save
        RedisOrderRepository redis = mock(RedisOrderRepository.class);
        service = new OrderServiceImpl(repository, redis, mongo);

        var result = service.findById("mongo-id");

        assertThat(result).isEqualTo(order);
        verify(redis).save(order);
    }

    @Test
    void should_throw_when_order_not_found() {
        when(cache.findById("404")).thenReturn(Optional.empty());
        when(mongo.findById("404")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findById("404"))
            .isInstanceOf(OrderNotFoundException.class)
            .hasMessageContaining("404");
    }

}
