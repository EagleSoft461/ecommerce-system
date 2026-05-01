package com.ecommerce.order.service;

import com.ecommerce.auth.model.User;
import com.ecommerce.common.exception.BadRequestException;
import com.ecommerce.common.exception.NotFoundException;
import com.ecommerce.event.model.OrderCancelledEvent;
import com.ecommerce.event.model.OrderCreatedEvent;
import com.ecommerce.event.publisher.OrderEventPublisher;
import com.ecommerce.order.dto.CreateOrderRequest;
import com.ecommerce.order.dto.OrderItemResponse;
import com.ecommerce.order.dto.OrderResponse;
import com.ecommerce.order.model.Order;
import com.ecommerce.order.model.OrderItem;
import com.ecommerce.order.repository.OrderRepository;
import com.ecommerce.product.model.Product;
import com.ecommerce.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final OrderEventPublisher orderEventPublisher;

    @Transactional
    public OrderResponse createOrder(CreateOrderRequest request) {
        // Giriş yapmış kullanıcıyı al
        User currentUser = getCurrentUser();

        // Sipariş oluştur
        Order order = Order.builder()
                .user(currentUser)
                .shippingAddress(request.getShippingAddress())
                .status(Order.OrderStatus.PENDING)
                .build();

        BigDecimal totalAmount = BigDecimal.ZERO;

        // Her ürün için OrderItem oluştur
        for (var itemRequest : request.getItems()) {
            Product product = productRepository.findById(itemRequest.getProductId())
                    .orElseThrow(() -> new NotFoundException("Product", itemRequest.getProductId()));

            // Stok kontrolü
            if (product.getStock() < itemRequest.getQuantity()) {
                throw new BadRequestException(
                        "Insufficient stock for product: " + product.getName() +
                        ". Available: " + product.getStock() +
                        ", Requested: " + itemRequest.getQuantity()
                );
            }

            // Sipariş anındaki fiyatı kaydet
            BigDecimal unitPrice = product.getPrice();
            BigDecimal itemTotal = unitPrice.multiply(BigDecimal.valueOf(itemRequest.getQuantity()));

            OrderItem orderItem = OrderItem.builder()
                    .order(order)
                    .product(product)
                    .quantity(itemRequest.getQuantity())
                    .unitPrice(unitPrice)
                    .totalPrice(itemTotal)
                    .build();

            order.getItems().add(orderItem);
            totalAmount = totalAmount.add(itemTotal);

            // Stoku düş
            product.setStock(product.getStock() - itemRequest.getQuantity());
            productRepository.save(product);
        }

        order.setTotalAmount(totalAmount);
        Order savedOrder = orderRepository.save(order);

        // Kafka'ya ORDER_CREATED eventi yayınla
        orderEventPublisher.publishOrderCreated(OrderCreatedEvent.builder()
                .orderId(savedOrder.getId())
                .userId(currentUser.getId())
                .userEmail(currentUser.getEmail())
                .totalAmount(savedOrder.getTotalAmount())
                .shippingAddress(savedOrder.getShippingAddress())
                .createdAt(savedOrder.getCreatedAt())
                .build());

        log.info("Order created: {} for user: {}", savedOrder.getId(), currentUser.getEmail());
        return toResponse(savedOrder);
    }

    // Kullanıcının kendi siparişleri
    public Page<OrderResponse> getMyOrders(Pageable pageable) {
        User currentUser = getCurrentUser();
        return orderRepository.findByUserId(currentUser.getId(), pageable)
                .map(this::toResponse);
    }

    // Tek sipariş detayı
    public OrderResponse getOrderById(Long id) {
        User currentUser = getCurrentUser();
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Order", id));

        // Kullanıcı sadece kendi siparişini görebilir (ADMIN hepsini görebilir)
        boolean isAdmin = currentUser.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        if (!isAdmin && !order.getUser().getId().equals(currentUser.getId())) {
            throw new BadRequestException("You don't have permission to view this order");
        }

        return toResponse(order);
    }

    // ADMIN: tüm siparişler
    public Page<OrderResponse> getAllOrders(Pageable pageable) {
        return orderRepository.findAll(pageable).map(this::toResponse);
    }

    // ADMIN: sipariş durumu güncelle
    @Transactional
    public OrderResponse updateStatus(Long id, String status) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Order", id));

        try {
            Order.OrderStatus newStatus = Order.OrderStatus.valueOf(status.toUpperCase());
            order.setStatus(newStatus);
            log.info("Order {} status updated to {}", id, newStatus);
            return toResponse(orderRepository.save(order));
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Invalid status: " + status +
                    ". Valid values: PENDING, CONFIRMED, SHIPPED, DELIVERED, CANCELLED");
        }
    }

    // ADMIN: sipariş iptal et (stok geri ver)
    @Transactional
    public OrderResponse cancelOrder(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Order", id));

        if (order.getStatus() == Order.OrderStatus.DELIVERED) {
            throw new BadRequestException("Cannot cancel a delivered order");
        }

        // Stokları geri ver
        for (OrderItem item : order.getItems()) {
            Product product = item.getProduct();
            product.setStock(product.getStock() + item.getQuantity());
            productRepository.save(product);
        }

        order.setStatus(Order.OrderStatus.CANCELLED);

        // Kafka'ya ORDER_CANCELLED eventi yayınla
        orderEventPublisher.publishOrderCancelled(OrderCancelledEvent.builder()
                .orderId(order.getId())
                .userId(order.getUser().getId())
                .userEmail(order.getUser().getEmail())
                .cancelledAt(java.time.LocalDateTime.now())
                .build());

        log.info("Order {} cancelled", id);
        return toResponse(orderRepository.save(order));
    }

    private User getCurrentUser() {
        return (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    private OrderResponse toResponse(Order order) {
        List<OrderItemResponse> itemResponses = order.getItems().stream()
                .map(item -> OrderItemResponse.builder()
                        .id(item.getId())
                        .productId(item.getProduct().getId())
                        .productName(item.getProduct().getName())
                        .quantity(item.getQuantity())
                        .unitPrice(item.getUnitPrice())
                        .totalPrice(item.getTotalPrice())
                        .build())
                .collect(Collectors.toList());

        return OrderResponse.builder()
                .id(order.getId())
                .userId(order.getUser().getId())
                .userEmail(order.getUser().getEmail())
                .items(itemResponses)
                .totalAmount(order.getTotalAmount())
                .status(order.getStatus().name())
                .shippingAddress(order.getShippingAddress())
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .build();
    }
}
