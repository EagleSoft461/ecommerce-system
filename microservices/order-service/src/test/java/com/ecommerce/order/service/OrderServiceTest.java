package com.ecommerce.order.service;

import com.ecommerce.order.client.ProductClient;
import com.ecommerce.order.common.exception.BadRequestException;
import com.ecommerce.order.common.exception.NotFoundException;
import com.ecommerce.order.dto.CreateOrderRequest;
import com.ecommerce.order.dto.OrderItemRequest;
import com.ecommerce.order.dto.OrderResponse;
import com.ecommerce.order.event.OrderCancelledEvent;
import com.ecommerce.order.event.OrderCreatedEvent;
import com.ecommerce.order.event.OrderEventPublisher;
import com.ecommerce.order.model.Order;
import com.ecommerce.order.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("OrderService Unit Tests")
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private ProductClient productClient;

    @Mock
    private OrderEventPublisher orderEventPublisher;

    @InjectMocks
    private OrderService orderService;

    private ProductClient.ProductResponse mockProduct;
    private Order mockOrder;
    private CreateOrderRequest createOrderRequest;

    @BeforeEach
    void setUp() {
        // Security context'e mock user ekle
        var auth = new UsernamePasswordAuthenticationToken(
                "test@example.com", null,
                List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );
        SecurityContextHolder.getContext().setAuthentication(auth);

        mockProduct = new ProductClient.ProductResponse(
                1L, "iPhone 15", new BigDecimal("999.99"), 50, "ACTIVE"
        );

        mockOrder = Order.builder()
                .id(1L)
                .userEmail("test@example.com")
                .userId(0L)
                .totalAmount(new BigDecimal("999.99"))
                .status(Order.OrderStatus.PENDING)
                .shippingAddress("Istanbul, Turkey")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        createOrderRequest = CreateOrderRequest.builder()
                .items(List.of(
                        OrderItemRequest.builder()
                                .productId(1L)
                                .quantity(1)
                                .build()
                ))
                .shippingAddress("Istanbul, Turkey")
                .build();
    }

    // ── CreateOrder Tests ────────────────────────────────────────────────────

    @Test
    @DisplayName("createOrder: başarılı sipariş oluşturma ve Kafka event yayınlama")
    void createOrder_Success_PublishesKafkaEvent() {
        // Given
        when(productClient.getProduct(1L)).thenReturn(mockProduct);
        when(orderRepository.save(any(Order.class))).thenReturn(mockOrder);

        // When
        OrderResponse response = orderService.createOrder(createOrderRequest);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getUserEmail()).isEqualTo("test@example.com");
        verify(orderEventPublisher).publishOrderCreated(any(OrderCreatedEvent.class));
    }

    @Test
    @DisplayName("createOrder: ürün bulunamazsa NotFoundException fırlatmalı")
    void createOrder_ProductNotFound_ThrowsNotFoundException() {
        // Given
        when(productClient.getProduct(1L)).thenReturn(null);

        // When & Then
        assertThatThrownBy(() -> orderService.createOrder(createOrderRequest))
                .isInstanceOf(NotFoundException.class);

        verify(orderRepository, never()).save(any());
        verify(orderEventPublisher, never()).publishOrderCreated(any());
    }

    @Test
    @DisplayName("createOrder: yetersiz stok BadRequestException fırlatmalı")
    void createOrder_InsufficientStock_ThrowsBadRequestException() {
        // Given — stok 0
        ProductClient.ProductResponse lowStockProduct = new ProductClient.ProductResponse(
                1L, "iPhone 15", new BigDecimal("999.99"), 0, "ACTIVE"
        );
        when(productClient.getProduct(1L)).thenReturn(lowStockProduct);

        // When & Then
        assertThatThrownBy(() -> orderService.createOrder(createOrderRequest))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Insufficient stock");

        verify(orderRepository, never()).save(any());
    }

    @Test
    @DisplayName("createOrder: toplam tutar doğru hesaplanmalı")
    void createOrder_CalculatesTotalAmountCorrectly() {
        // Given — 2 adet ürün
        CreateOrderRequest multiItemRequest = CreateOrderRequest.builder()
                .items(List.of(
                        OrderItemRequest.builder().productId(1L).quantity(2).build()
                ))
                .shippingAddress("Istanbul, Turkey")
                .build();

        Order savedOrder = Order.builder()
                .id(1L)
                .userEmail("test@example.com")
                .userId(0L)
                .totalAmount(new BigDecimal("1999.98"))
                .status(Order.OrderStatus.PENDING)
                .shippingAddress("Istanbul, Turkey")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        when(productClient.getProduct(1L)).thenReturn(mockProduct);
        when(orderRepository.save(any(Order.class))).thenReturn(savedOrder);

        // When
        OrderResponse response = orderService.createOrder(multiItemRequest);

        // Then
        assertThat(response.getTotalAmount()).isEqualByComparingTo(new BigDecimal("1999.98"));
    }

    // ── CancelOrder Tests ────────────────────────────────────────────────────

    @Test
    @DisplayName("cancelOrder: başarılı iptal ve Kafka event yayınlama")
    void cancelOrder_Success_PublishesCancelledEvent() {
        // Given
        when(orderRepository.findById(1L)).thenReturn(Optional.of(mockOrder));
        when(orderRepository.save(any(Order.class))).thenReturn(mockOrder);

        // When
        orderService.cancelOrder(1L);

        // Then
        verify(orderEventPublisher).publishOrderCancelled(any(OrderCancelledEvent.class));
        verify(orderRepository).save(argThat(o ->
                o.getStatus() == Order.OrderStatus.CANCELLED));
    }

    @Test
    @DisplayName("cancelOrder: teslim edilmiş sipariş iptal edilemez")
    void cancelOrder_DeliveredOrder_ThrowsBadRequestException() {
        // Given
        Order deliveredOrder = Order.builder()
                .id(1L)
                .userEmail("test@example.com")
                .userId(0L)
                .status(Order.OrderStatus.DELIVERED)
                .totalAmount(BigDecimal.TEN)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        when(orderRepository.findById(1L)).thenReturn(Optional.of(deliveredOrder));

        // When & Then
        assertThatThrownBy(() -> orderService.cancelOrder(1L))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Cannot cancel a delivered order");

        verify(orderEventPublisher, never()).publishOrderCancelled(any());
    }

    @Test
    @DisplayName("cancelOrder: sipariş bulunamazsa NotFoundException fırlatmalı")
    void cancelOrder_NotFound_ThrowsNotFoundException() {
        // Given
        when(orderRepository.findById(99L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> orderService.cancelOrder(99L))
                .isInstanceOf(NotFoundException.class);
    }

    // ── UpdateStatus Tests ───────────────────────────────────────────────────

    @Test
    @DisplayName("updateStatus: geçerli status güncelleme")
    void updateStatus_ValidStatus_UpdatesOrder() {
        // Given
        Order confirmedOrder = Order.builder()
                .id(1L)
                .userEmail("test@example.com")
                .userId(0L)
                .status(Order.OrderStatus.CONFIRMED)
                .totalAmount(BigDecimal.TEN)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        when(orderRepository.findById(1L)).thenReturn(Optional.of(mockOrder));
        when(orderRepository.save(any(Order.class))).thenReturn(confirmedOrder);

        // When
        OrderResponse response = orderService.updateStatus(1L, "CONFIRMED");

        // Then
        assertThat(response.getStatus()).isEqualTo("CONFIRMED");
    }

    @Test
    @DisplayName("updateStatus: geçersiz status BadRequestException fırlatmalı")
    void updateStatus_InvalidStatus_ThrowsBadRequestException() {
        // Given
        when(orderRepository.findById(1L)).thenReturn(Optional.of(mockOrder));

        // When & Then
        assertThatThrownBy(() -> orderService.updateStatus(1L, "INVALID_STATUS"))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Invalid status");
    }
}
