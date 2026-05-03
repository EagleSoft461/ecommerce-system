package com.ecommerce.event.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderCancelledEvent {

    private Long orderId;
    private Long userId;
    private String userEmail;
    private LocalDateTime cancelledAt;

    @Builder.Default
    private String eventType = "ORDER_CANCELLED";
}
