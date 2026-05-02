package com.ecommerce.product.service;

import com.ecommerce.common.exception.NotFoundException;
import com.ecommerce.product.dto.ProductRequest;
import com.ecommerce.product.dto.ProductResponse;
import com.ecommerce.product.model.Category;
import com.ecommerce.product.model.Product;
import com.ecommerce.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryService categoryService;

    @Transactional
    @CacheEvict(value = "product", allEntries = true)
    public ProductResponse create(ProductRequest request) {
        Category category = categoryService.findById(request.getCategoryId());

        Product product = Product.builder()
                .name(request.getName())
                .description(request.getDescription())
                .price(request.getPrice())
                .stock(request.getStock())
                .imageUrl(request.getImageUrl())
                .category(category)
                .status(Product.ProductStatus.ACTIVE)
                .build();

        product = productRepository.save(product);
        log.info("Product created: {}, cache evicted", product.getName());
        return toResponse(product);
    }

    // Cache key: "products::page-0-size-10-createdAt-desc" gibi
    public Page<ProductResponse> getAll(Pageable pageable) {
        log.info("Fetching products from DB");
        return productRepository.findByStatus(Product.ProductStatus.ACTIVE, pageable)
                .map(this::toResponse);
    }

    // Tek ürün cache'i - en çok kullanılan pattern
    @Cacheable(value = "product", key = "#id")
    public ProductResponse getById(Long id) {
        log.info("Cache MISS - fetching product {} from DB", id);
        return toResponse(findById(id));
    }

    public Page<ProductResponse> getByCategory(Long categoryId, Pageable pageable) {
        log.info("Fetching products by category {} from DB", categoryId);
        return productRepository.findByCategoryId(categoryId, pageable)
                .map(this::toResponse);
    }

    public Page<ProductResponse> search(String keyword, Pageable pageable) {
        // Arama sonuçlarını cache'lemiyoruz — her arama farklı olabilir
        return productRepository.searchByKeyword(keyword, pageable)
                .map(this::toResponse);
    }

    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "product", key = "#id")
    })
    public ProductResponse update(Long id, ProductRequest request) {
        Product product = findById(id);
        Category category = categoryService.findById(request.getCategoryId());

        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setStock(request.getStock());
        product.setImageUrl(request.getImageUrl());
        product.setCategory(category);

        log.info("Product {} updated, cache evicted", id);
        return toResponse(productRepository.save(product));
    }

    @Transactional
    @CacheEvict(value = "product", key = "#id")
    public void delete(Long id) {
        Product product = findById(id);
        product.setStatus(Product.ProductStatus.INACTIVE);
        productRepository.save(product);
        log.info("Product {} deactivated, cache evicted", id);
    }

    private Product findById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Product", id));
    }

    private ProductResponse toResponse(Product product) {
        return ProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .stock(product.getStock())
                .imageUrl(product.getImageUrl())
                .status(product.getStatus().name())
                .category(ProductResponse.CategoryInfo.builder()
                        .id(product.getCategory().getId())
                        .name(product.getCategory().getName())
                        .build())
                .createdAt(product.getCreatedAt())
                .build();
    }
}
