package com.ecommerce.order.service;

import com.ecommerce.order.client.ProductClient;
import com.ecommerce.order.common.exception.BadRequestException;
import com.ecommerce.order.common.exception.NotFoundException;
import com.ecommerce.order.dto.CreateOrderRequest;
import com.ecommerce.order.dto.OrderItemResponse;
import com.ecommerce.order.dto.OrderResponse;
import com.ecommerce.order.event.OrderCancelledEvent;
import com.ecommerce.order.event.OrderCreatedEvent;
import com.ecommerce.order.event.OrderEventPublisher;
import com.ecommerce.order.model.Order;
import com.ecommerce.order.model.OrderItem;
import com.ecommerce.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductClient productClient;
    private final OrderEventPublisher orderEventPublisher;

    @Transactional
    public OrderResponse createOrder(CreateOrderRequest request) {
        // Gateway'den gelen X-User-Email header'ından kullanıcıyı al
        String userEmail = SecurityContextHolder.getContext().getAuthentication().getName();

        Order order = Order.builder()
                .userEmail(userEmail)
                .userId(0L) // Gateway'den userId de gelebilir, şimdilik 0
                .shippingAddress(request.getShippingAddress())
                .status(Order.OrderStatus.PENDING)
                .build();

        BigDecimal totalAmount = BigDecimal.ZERO;

        for (var itemRequest : request.getItems()) {
            // Product-service'e HTTP ile sor
            ProductClient.ProductResponse product = productClient.getProduct(itemRequest.getProductId());

            if (product == null) {
                throw new NotFoundException("Product", itemRequest.getProductId());
            }

            if (product.getStock() < itemRequest.getQuantity()) {
                throw new BadRequestException(
                        "Insufficient stock for product: " + product.getName() +
                        ". Available: " + product.getStock() +
                        ", Requested: " + itemRequest.getQuantity()
                );
            }

            BigDecimal unitPrice = product.getPrice();
            BigDecimal itemTotal = unitPrice.multiply(BigDecimal.valueOf(itemRequest.getQuantity()));

            OrderItem orderItem = OrderItem.builder()
                    .order(order)
                    .productId(product.getId())
                    .productName(product.getName())
                    .quantity(itemRequest.getQuantity())
                    .unitPrice(unitPrice)
                    .totalPrice(itemTotal)
                    .build();

            order.getItems().add(orderItem);
            totalAmount = totalAmount.add(itemTotal);
        }

        order.setTotalAmount(totalAmount);
        Order savedOrder = orderRepository.save(order);

        // Kafka event yayınla
        orderEventPublisher.publishOrderCreated(OrderCreatedEvent.builder()
                .orderId(savedOrder.getId())
                .userEmail(userEmail)
                .totalAmount(savedOrder.getTotalAmount())
                .shippingAddress(savedOrder.getShippingAddress())
                .createdAt(savedOrder.getCreatedAt())
                .build());

        log.info("Order created: {} for user: {}", savedOrder.getId(), userEmail);
        return toResponse(savedOrder);
    }

    public Page<OrderResponse> getMyOrders(Pageable pageable) {
        String userEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        return orderRepository.findByUserEmail(userEmail, pageable).map(this::toResponse);
    }

    public OrderResponse getOrderById(Long id) {
        String userEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        Order order = findById(id);

        boolean isAdmin = SecurityContextHolder.getContext().getAuthentication()
                .getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        if (!isAdmin && !order.getUserEmail().equals(userEmail)) {
            throw new BadRequestException("You don't have permission to view this order");
        }

        return toResponse(order);
    }

    public Page<OrderResponse> getAllOrders(Pageable pageable) {
        return orderRepository.findAll(pageable).map(this::toResponse);
    }

    @Transactional
    public OrderResponse updateStatus(Long id, String status) {
        Order order = findById(id);
        try {
            order.setStatus(Order.OrderStatus.valueOf(status.toUpperCase()));
            return toResponse(orderRepository.save(order));
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Invalid status: " + status);
        }
    }

    @Transactional
    public OrderResponse cancelOrder(Long id) {
        Order order = findById(id);

        if (order.getStatus() == Order.OrderStatus.DELIVERED) {
            throw new BadRequestException("Cannot cancel a delivered order");
        }

        order.setStatus(Order.OrderStatus.CANCELLED);

        orderEventPublisher.publishOrderCancelled(OrderCancelledEvent.builder()
                .orderId(order.getId())
                .userEmail(order.getUserEmail())
                .cancelledAt(LocalDateTime.now())
                .build());

        log.info("Order {} cancelled", id);
        return toResponse(orderRepository.save(order));
    }

    private Order findById(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Order", id));
    }

    private OrderResponse toResponse(Order order) {
        List<OrderItemResponse> itemResponses = order.getItems().stream()
                .map(item -> OrderItemResponse.builder()
                        .id(item.getId())
                        .productId(item.getProductId())
                        .productName(item.getProductName())
                        .quantity(item.getQuantity())
                        .unitPrice(item.getUnitPrice())
                        .totalPrice(item.getTotalPrice())
                        .build())
                .collect(Collectors.toList());

        return OrderResponse.builder()
                .id(order.getId())
                .userId(order.getUserId())
                .userEmail(order.getUserEmail())
                .items(itemResponses)
                .totalAmount(order.getTotalAmount())
                .status(order.getStatus().name())
                .shippingAddress(order.getShippingAddress())
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .build();
    }
}
