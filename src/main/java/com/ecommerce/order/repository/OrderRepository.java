package com.ecommerce.order.repository;

import com.ecommerce.order.model.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    // Kullanıcının kendi siparişleri
    Page<Order> findByUserId(Long userId, Pageable pageable);

    // Belirli durumdaki siparişler (ADMIN için)
    Page<Order> findByStatus(Order.OrderStatus status, Pageable pageable);
}
