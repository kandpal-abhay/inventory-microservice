package com.inventory.microservice.service;

import com.inventory.microservice.dto.CreateProductRequest;
import com.inventory.microservice.dto.UpdateStockRequest;
import com.inventory.microservice.entity.Product;
import com.inventory.microservice.entity.StockAdjustment;
import com.inventory.microservice.repository.ProductRepository;
import com.inventory.microservice.repository.StockAdjustmentRepository;
import jakarta.persistence.OptimisticLockException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductService {

    private final ProductRepository productRepository;
    private final StockAdjustmentRepository stockAdjustmentRepository;

    @Transactional
    public Product createProduct(CreateProductRequest request) {
        log.info("Creating new product: {}", request.getSku());

        if (productRepository.existsBySku(request.getSku())) {
            throw new RuntimeException("Product with SKU already exists: " + request.getSku());
        }

        Product product = new Product();
        product.setSku(request.getSku());
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setCategory(request.getCategory());
        product.setPrice(request.getPrice());
        product.setStockQuantity(request.getStockQuantity());
        product.setReorderLevel(request.getReorderLevel());
        product.setActive(true);

        Product savedProduct = productRepository.save(product);
        log.info("Product created: {} (ID: {})", savedProduct.getSku(), savedProduct.getId());

        // Record initial stock if any
        if (request.getStockQuantity() > 0) {
            recordStockAdjustment(savedProduct, 0, request.getStockQuantity(),
                    request.getStockQuantity(), "RESTOCK", "Initial stock");
        }

        return savedProduct;
    }

    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    public List<Product> getActiveProducts() {
        return productRepository.findByActiveTrue();
    }

    public Product getProductById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));
    }

    public Product getProductBySku(String sku) {
        return productRepository.findBySku(sku)
                .orElseThrow(() -> new RuntimeException("Product not found with SKU: " + sku));
    }

    public List<Product> getProductsByCategory(String category) {
        return productRepository.findByCategory(category);
    }

    public List<Product> getProductsNeedingReorder() {
        return productRepository.findProductsNeedingReorder();
    }

    @Transactional
    public Product updateProduct(Long id, CreateProductRequest request) {
        log.info("Updating product: {}", id);

        Product product = getProductById(id);
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setCategory(request.getCategory());
        product.setPrice(request.getPrice());
        product.setReorderLevel(request.getReorderLevel());

        Product updatedProduct = productRepository.save(product);
        log.info("Product updated: {}", updatedProduct.getId());

        return updatedProduct;
    }

    @Transactional
    @Retryable(
            retryFor = {OptimisticLockException.class, ObjectOptimisticLockingFailureException.class},
            maxAttempts = 3,
            backoff = @Backoff(delay = 100)
    )
    public Product updateStock(Long id, UpdateStockRequest request) {
        log.info("Updating stock for product: {} by {} ({})",
                id, request.getQuantityChange(), request.getAdjustmentType());

        Product product = getProductById(id);
        int previousQuantity = product.getStockQuantity();
        int newQuantity = previousQuantity + request.getQuantityChange();

        if (newQuantity < 0) {
            throw new RuntimeException("Insufficient stock. Available: " + previousQuantity +
                    ", Requested: " + Math.abs(request.getQuantityChange()));
        }

        product.setStockQuantity(newQuantity);

        try {
            Product updatedProduct = productRepository.save(product);

            // Record stock adjustment
            recordStockAdjustment(updatedProduct, previousQuantity, newQuantity,
                    request.getQuantityChange(), request.getAdjustmentType(), request.getReason());

            log.info("Stock updated successfully. Product: {}, Previous: {}, New: {}",
                    updatedProduct.getSku(), previousQuantity, newQuantity);

            return updatedProduct;
        } catch (ObjectOptimisticLockingFailureException | OptimisticLockException e) {
            log.warn("Optimistic lock failure for product: {}. Retrying...", id);
            throw e; // Will be retried by @Retryable
        }
    }

    @Transactional
    public void deleteProduct(Long id) {
        log.info("Deleting product: {}", id);
        Product product = getProductById(id);
        product.setActive(false);
        productRepository.save(product);
        log.info("Product deactivated: {}", id);
    }

    private void recordStockAdjustment(Product product, int previousQuantity, int newQuantity,
                                       int quantityChange, String adjustmentType, String reason) {
        StockAdjustment adjustment = new StockAdjustment();
        adjustment.setProductId(product.getId());
        adjustment.setProductSku(product.getSku());
        adjustment.setAdjustmentType(adjustmentType);
        adjustment.setQuantityChange(quantityChange);
        adjustment.setPreviousQuantity(previousQuantity);
        adjustment.setNewQuantity(newQuantity);
        adjustment.setReason(reason);

        stockAdjustmentRepository.save(adjustment);
        log.debug("Stock adjustment recorded for product: {}", product.getSku());
    }

    public List<StockAdjustment> getStockHistory(Long productId) {
        return stockAdjustmentRepository.findByProductId(productId);
    }
}
