package com.ecommerce.event.listener;

import com.ecommerce.event.model.OrderCancelledEvent;
import com.ecommerce.event.model.OrderCreatedEvent;
import com.ecommerce.event.publisher.OrderEventPublisher;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class OrderEventListener {

    // ORDER_CREATED eventini dinle
    @KafkaListener(
            topics = OrderEventPublisher.ORDER_CREATED_TOPIC,
            groupId = "notification-service"
    )
    public void handleOrderCreated(OrderCreatedEvent event) {
        log.info("=== NOTIFICATION SERVICE ===");
        log.info("New order received!");
        log.info("Order ID  : {}", event.getOrderId());
        log.info("User      : {}", event.getUserEmail());
        log.info("Amount    : {} TL", event.getTotalAmount());
        log.info("Address   : {}", event.getShippingAddress());
        log.info("Simulating: Sending confirmation email to {}", event.getUserEmail());
        log.info("Simulating: Sending SMS notification");
        log.info("===========================");

        // Gerçek sistemde burada:
        // emailService.sendOrderConfirmation(event.getUserEmail(), event.getOrderId());
        // smsService.sendSms(event.getUserPhone(), "Siparişiniz alındı!");
    }

    // ORDER_CANCELLED eventini dinle
    @KafkaListener(
            topics = OrderEventPublisher.ORDER_CANCELLED_TOPIC,
            groupId = "notification-service"
    )
    public void handleOrderCancelled(OrderCancelledEvent event) {
        log.info("=== NOTIFICATION SERVICE ===");
        log.info("Order cancelled!");
        log.info("Order ID  : {}", event.getOrderId());
        log.info("User      : {}", event.getUserEmail());
        log.info("Simulating: Sending cancellation email to {}", event.getUserEmail());
        log.info("===========================");
    }
}
