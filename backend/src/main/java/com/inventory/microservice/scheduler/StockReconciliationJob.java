package com.inventory.microservice.scheduler;

import com.inventory.microservice.config.TenantContext;
import com.inventory.microservice.entity.Product;
import com.inventory.microservice.entity.Tenant;
import com.inventory.microservice.repository.ProductRepository;
import com.inventory.microservice.repository.TenantRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class StockReconciliationJob {

    private final TenantRepository tenantRepository;
    private final ProductRepository productRepository;

    /**
     * Runs stock reconciliation for all tenants every day at 2 AM
     * Checks for products that need reordering and logs alerts
     */
    @Scheduled(cron = "0 0 2 * * ?") // Every day at 2 AM
    public void reconcileStock() {
        log.info("Starting daily stock reconciliation job");

        List<Tenant> activeTenants = tenantRepository.findAll().stream()
                .filter(Tenant::getActive)
                .toList();

        log.info("Found {} active tenants for reconciliation", activeTenants.size());

        for (Tenant tenant : activeTenants) {
            try {
                reconcileStockForTenant(tenant);
            } catch (Exception e) {
                log.error("Error reconciling stock for tenant: {}", tenant.getTenantId(), e);
            } finally {
                TenantContext.clear();
            }
        }

        log.info("Stock reconciliation job completed");
    }

    /**
     * Runs every hour to check for low stock items
     */
    @Scheduled(fixedRate = 3600000) // Every hour (3600000 ms)
    public void checkLowStockItems() {
        log.debug("Running hourly low stock check");

        List<Tenant> activeTenants = tenantRepository.findAll().stream()
                .filter(Tenant::getActive)
                .toList();

        for (Tenant tenant : activeTenants) {
            try {
                checkLowStockForTenant(tenant);
            } catch (Exception e) {
                log.error("Error checking low stock for tenant: {}", tenant.getTenantId(), e);
            } finally {
                TenantContext.clear();
            }
        }
    }

    private void reconcileStockForTenant(Tenant tenant) {
        log.info("Reconciling stock for tenant: {}", tenant.getTenantId());
        TenantContext.setCurrentTenant(tenant.getTenantId());

        List<Product> allProducts = productRepository.findAll();
        int totalProducts = allProducts.size();
        int activeProducts = (int) allProducts.stream().filter(Product::getActive).count();
        int inactiveProducts = totalProducts - activeProducts;

        log.info("Tenant {} - Total products: {}, Active: {}, Inactive: {}",
                tenant.getTenantId(), totalProducts, activeProducts, inactiveProducts);

        // Check for products needing reorder
        List<Product> reorderProducts = productRepository.findProductsNeedingReorder();
        if (!reorderProducts.isEmpty()) {
            log.warn("Tenant {} - {} products need reordering:", tenant.getTenantId(), reorderProducts.size());
            for (Product product : reorderProducts) {
                log.warn("  - {} (SKU: {}) - Current stock: {}, Reorder level: {}",
                        product.getName(), product.getSku(), product.getStockQuantity(), product.getReorderLevel());
            }
        }

        // Calculate total inventory value
        double totalValue = allProducts.stream()
                .filter(Product::getActive)
                .mapToDouble(p -> p.getPrice().doubleValue() * p.getStockQuantity())
                .sum();

        log.info("Tenant {} - Total inventory value: ${}", tenant.getTenantId(), String.format("%.2f", totalValue));
    }

    private void checkLowStockForTenant(Tenant tenant) {
        TenantContext.setCurrentTenant(tenant.getTenantId());

        List<Product> reorderProducts = productRepository.findProductsNeedingReorder();
        if (!reorderProducts.isEmpty()) {
            log.warn("Tenant {} - ALERT: {} products below reorder level",
                    tenant.getTenantId(), reorderProducts.size());
        }
    }
}
