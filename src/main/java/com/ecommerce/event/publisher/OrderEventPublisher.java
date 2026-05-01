package com.ecommerce.event.publisher;

import com.ecommerce.event.model.OrderCancelledEvent;
import com.ecommerce.event.model.OrderCreatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderEventPublisher {

    // Topic isimleri — sabit string yerine constant kullanmak best practice
    public static final String ORDER_CREATED_TOPIC = "order-created";
    public static final String ORDER_CANCELLED_TOPIC = "order-cancelled";

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publishOrderCreated(OrderCreatedEvent event) {
        log.info("Publishing ORDER_CREATED event for orderId: {}", event.getOrderId());
        kafkaTemplate.send(ORDER_CREATED_TOPIC, String.valueOf(event.getOrderId()), event);
        log.info("ORDER_CREATED event published successfully for orderId: {}", event.getOrderId());
    }

    public void publishOrderCancelled(OrderCancelledEvent event) {
        log.info("Publishing ORDER_CANCELLED event for orderId: {}", event.getOrderId());
        kafkaTemplate.send(ORDER_CANCELLED_TOPIC, String.valueOf(event.getOrderId()), event);
        log.info("ORDER_CANCELLED event published successfully for orderId: {}", event.getOrderId());
    }
}
