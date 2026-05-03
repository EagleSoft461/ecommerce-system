package com.ecommerce.event.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderCreatedEvent {

    private Long orderId;
    private Long userId;
    private String userEmail;
    private BigDecimal totalAmount;
    private String shippingAddress;
    private LocalDateTime createdAt;

    @Builder.Default
    private String eventType = "ORDER_CREATED";
}
