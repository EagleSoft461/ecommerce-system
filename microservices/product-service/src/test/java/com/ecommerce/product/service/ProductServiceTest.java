package com.ecommerce.product.service;

import com.ecommerce.product.common.exception.NotFoundException;
import com.ecommerce.product.dto.ProductRequest;
import com.ecommerce.product.dto.ProductResponse;
import com.ecommerce.product.model.Category;
import com.ecommerce.product.model.Product;
import com.ecommerce.product.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProductService Unit Tests")
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CategoryService categoryService;

    @InjectMocks
    private ProductService productService;

    private Category mockCategory;
    private Product mockProduct;
    private ProductRequest productRequest;

    @BeforeEach
    void setUp() {
        mockCategory = Category.builder()
                .id(1L)
                .name("Electronics")
                .build();

        mockProduct = Product.builder()
                .id(1L)
                .name("iPhone 15")
                .description("Latest iPhone")
                .price(new BigDecimal("999.99"))
                .stock(50)
                .status(Product.ProductStatus.ACTIVE)
                .category(mockCategory)
                .createdAt(LocalDateTime.now())
                .build();

        productRequest = ProductRequest.builder()
                .name("iPhone 15")
                .description("Latest iPhone")
                .price(new BigDecimal("999.99"))
                .stock(50)
                .categoryId(1L)
                .build();
    }

    // ── Create Tests ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("create: başarılı ürün oluşturma")
    void create_Success_ReturnsProductResponse() {
        // Given
        when(categoryService.findById(1L)).thenReturn(mockCategory);
        when(productRepository.save(any(Product.class))).thenReturn(mockProduct);

        // When
        ProductResponse response = productService.create(productRequest);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getName()).isEqualTo("iPhone 15");
        assertThat(response.getPrice()).isEqualByComparingTo(new BigDecimal("999.99"));
        assertThat(response.getStock()).isEqualTo(50);
        verify(productRepository).save(any(Product.class));
    }

    @Test
    @DisplayName("create: kategori bulunamazsa NotFoundException fırlatmalı")
    void create_CategoryNotFound_ThrowsNotFoundException() {
        // Given
        when(categoryService.findById(1L)).thenThrow(new NotFoundException("Category", 1L));

        // When & Then
        assertThatThrownBy(() -> productService.create(productRequest))
                .isInstanceOf(NotFoundException.class);

        verify(productRepository, never()).save(any());
    }

    // ── GetAll Tests ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("getAll: aktif ürünleri sayfalı döndürmeli")
    void getAll_ReturnsPagedProducts() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        Page<Product> productPage = new PageImpl<>(List.of(mockProduct), pageable, 1);
        when(productRepository.findByStatus(Product.ProductStatus.ACTIVE, pageable))
                .thenReturn(productPage);

        // When
        Page<ProductResponse> result = productService.getAll(pageable);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).getName()).isEqualTo("iPhone 15");
    }

    @Test
    @DisplayName("getAll: ürün yoksa boş sayfa döndürmeli")
    void getAll_NoProducts_ReturnsEmptyPage() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        Page<Product> emptyPage = new PageImpl<>(List.of(), pageable, 0);
        when(productRepository.findByStatus(Product.ProductStatus.ACTIVE, pageable))
                .thenReturn(emptyPage);

        // When
        Page<ProductResponse> result = productService.getAll(pageable);

        // Then
        assertThat(result.getTotalElements()).isEqualTo(0);
        assertThat(result.getContent()).isEmpty();
    }

    // ── GetById Tests ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("getById: mevcut ürünü döndürmeli")
    void getById_ExistingProduct_ReturnsProductResponse() {
        // Given
        when(productRepository.findById(1L)).thenReturn(Optional.of(mockProduct));

        // When
        ProductResponse response = productService.getById(1L);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getName()).isEqualTo("iPhone 15");
    }

    @Test
    @DisplayName("getById: ürün bulunamazsa NotFoundException fırlatmalı")
    void getById_NotFound_ThrowsNotFoundException() {
        // Given
        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> productService.getById(99L))
                .isInstanceOf(NotFoundException.class);
    }

    // ── Update Tests ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("update: başarılı güncelleme")
    void update_Success_ReturnsUpdatedProduct() {
        // Given
        ProductRequest updateRequest = ProductRequest.builder()
                .name("iPhone 15 Pro")
                .description("Updated iPhone")
                .price(new BigDecimal("1199.99"))
                .stock(30)
                .categoryId(1L)
                .build();

        Product updatedProduct = Product.builder()
                .id(1L)
                .name("iPhone 15 Pro")
                .description("Updated iPhone")
                .price(new BigDecimal("1199.99"))
                .stock(30)
                .status(Product.ProductStatus.ACTIVE)
                .category(mockCategory)
                .createdAt(LocalDateTime.now())
                .build();

        when(productRepository.findById(1L)).thenReturn(Optional.of(mockProduct));
        when(categoryService.findById(1L)).thenReturn(mockCategory);
        when(productRepository.save(any(Product.class))).thenReturn(updatedProduct);

        // When
        ProductResponse response = productService.update(1L, updateRequest);

        // Then
        assertThat(response.getName()).isEqualTo("iPhone 15 Pro");
        assertThat(response.getPrice()).isEqualByComparingTo(new BigDecimal("1199.99"));
    }

    // ── Delete Tests ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("delete: ürün INACTIVE yapılmalı")
    void delete_ExistingProduct_SetsInactive() {
        // Given
        when(productRepository.findById(1L)).thenReturn(Optional.of(mockProduct));
        when(productRepository.save(any(Product.class))).thenReturn(mockProduct);

        // When
        productService.delete(1L);

        // Then
        verify(productRepository).save(argThat(p ->
                p.getStatus() == Product.ProductStatus.INACTIVE));
    }

    @Test
    @DisplayName("delete: ürün bulunamazsa NotFoundException fırlatmalı")
    void delete_NotFound_ThrowsNotFoundException() {
        // Given
        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> productService.delete(99L))
                .isInstanceOf(NotFoundException.class);
    }

    // ── Search Tests ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("search: keyword ile ürün araması")
    void search_WithKeyword_ReturnsMatchingProducts() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        Page<Product> productPage = new PageImpl<>(List.of(mockProduct), pageable, 1);
        when(productRepository.searchByKeyword("iPhone", pageable)).thenReturn(productPage);

        // When
        Page<ProductResponse> result = productService.search("iPhone", pageable);

        // Then
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).getName()).contains("iPhone");
    }
}
