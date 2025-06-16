package com.ambev.order.integration;

import com.ambev.order.domain.event.publisher.OrderEventPublisher;
import com.ambev.order.domain.service.OrderService;
import com.ambev.order.infrastructure.dto.OrderItemDTO;
import com.ambev.order.infrastructure.dto.OrderRequestDTO;
import com.ambev.order.infrastructure.exception.OrderNotFoundException;
import com.ambev.order.util.TestFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class OrderControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private OrderService orderService;

    @MockBean
    private OrderEventPublisher publisher;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void should_return_201_on_successful_order_creation() throws Exception {
        var dto = new OrderRequestDTO("order-1", List.of(
            new OrderItemDTO("P1", 2, new BigDecimal("10.0"))
        ));

        mockMvc.perform(post("/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
            .andExpect(status().isCreated());

        verify(publisher, times(1)).publishOrderCreated(dto);
    }

    @Test
    void should_return_200_on_existing_order_found() throws Exception {
        var order = TestFactory.createSampleOrder("order-1");

        when(orderService.findById("order-1")).thenReturn(order);

        mockMvc.perform(get("/orders/order-1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value("order-1"))
            .andExpect(jsonPath("$.total").value(new BigDecimal("45.0")));
    }

    @Test
    void should_return_404_when_order_not_found() throws Exception {
        when(orderService.findById("not-found")).thenThrow(new OrderNotFoundException("not-found"));

        mockMvc.perform(get("/orders/not-found"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.message").value(containsString("not found")));
    }

    @Test
    void should_return_500_on_generic_exception() throws Exception {
        var dto = new OrderRequestDTO("boom", List.of(
            new OrderItemDTO("P1", 1, new BigDecimal("10.0"))
        ));

        doThrow(new RuntimeException("boom")).when(publisher).publishOrderCreated(dto);

        mockMvc.perform(post("/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
            .andExpect(status().isInternalServerError())
            .andExpect(jsonPath("$.message").value(containsString("Unexpected error occurred.")));
    }

}

