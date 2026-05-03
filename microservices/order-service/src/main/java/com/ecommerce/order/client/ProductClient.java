package com.ecommerce.order.client;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.math.BigDecimal;

// Order-service, product-service'e HTTP ile stok kontrolü yapar
@Slf4j
@Component
@RequiredArgsConstructor
public class ProductClient {

    private final WebClient.Builder webClientBuilder;

    @Value("${services.product-service.url}")
    private String productServiceUrl;

    public ProductResponse getProduct(Long productId) {
        return webClientBuilder.build()
                .get()
                .uri(productServiceUrl + "/api/v1/products/" + productId)
                .retrieve()
                .bodyToMono(ProductClientResponse.class)
                .map(ProductClientResponse::getData)
                .block();
    }

    public void updateStock(Long productId, Integer quantity, String token) {
        // Stok güncelleme — product-service'e PATCH isteği
        webClientBuilder.build()
                .patch()
                .uri(productServiceUrl + "/api/v1/products/" + productId + "/stock?quantity=" + quantity)
                .header("Authorization", "Bearer " + token)
                .retrieve()
                .bodyToMono(Void.class)
                .block();
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProductResponse {
        private Long id;
        private String name;
        private BigDecimal price;
        private Integer stock;
        private String status;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProductClientResponse {
        private boolean success;
        private ProductResponse data;
    }
}
