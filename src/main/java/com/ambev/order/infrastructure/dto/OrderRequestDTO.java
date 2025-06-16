package com.ambev.order.infrastructure.dto;

import jakarta.validation.constraints.*;
import java.util.List;

public record OrderRequestDTO(
    @NotBlank String orderId,
    @NotEmpty List<OrderItemDTO> items
) {}