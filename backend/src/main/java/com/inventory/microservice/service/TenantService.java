package com.inventory.microservice.service;

import com.inventory.microservice.config.MultiTenantConfig;
import com.inventory.microservice.dto.CreateTenantRequest;
import com.inventory.microservice.entity.Tenant;
import com.inventory.microservice.repository.TenantRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class TenantService {

    private final TenantRepository tenantRepository;
    private final JdbcTemplate jdbcTemplate;
    private final MultiTenantConfig multiTenantConfig;

    @Transactional
    public Tenant createTenant(CreateTenantRequest request) {
        log.info("Creating new tenant: {}", request.getTenantId());

        if (tenantRepository.existsByTenantId(request.getTenantId())) {
            throw new RuntimeException("Tenant ID already exists: " + request.getTenantId());
        }

        String schemaName = "tenant_" + request.getTenantId();

        if (tenantRepository.existsBySchemaName(schemaName)) {
            throw new RuntimeException("Schema name already exists: " + schemaName);
        }

        // Create tenant record
        Tenant tenant = new Tenant();
        tenant.setTenantId(request.getTenantId());
        tenant.setTenantName(request.getTenantName());
        tenant.setSchemaName(schemaName);
        tenant.setActive(true);

        Tenant savedTenant = tenantRepository.save(tenant);
        log.info("Tenant record created: {}", savedTenant.getId());

        // Create dedicated schema for tenant
        createTenantSchema(schemaName);

        // Add datasource to the multi-tenant configuration
        multiTenantConfig.addTenantDataSource(request.getTenantId(), schemaName);

        log.info("Tenant created successfully: {} -> schema: {}", request.getTenantId(), schemaName);
        return savedTenant;
    }

    private void createTenantSchema(String schemaName) {
        try {
            log.info("Creating database schema: {}", schemaName);

            // Create schema with backticks to handle special characters
            jdbcTemplate.execute("CREATE DATABASE IF NOT EXISTS `" + schemaName + "`");

            // Create products table in tenant schema
            String createProductsTable = String.format(
                    "CREATE TABLE IF NOT EXISTS `%s`.`products` (" +
                            "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                            "sku VARCHAR(255) NOT NULL UNIQUE, " +
                            "name VARCHAR(255) NOT NULL, " +
                            "description VARCHAR(1000), " +
                            "category VARCHAR(255) NOT NULL, " +
                            "price DECIMAL(10,2) NOT NULL, " +
                            "stock_quantity INT NOT NULL DEFAULT 0, " +
                            "reorder_level INT NOT NULL DEFAULT 10, " +
                            "active BOOLEAN NOT NULL DEFAULT TRUE, " +
                            "version BIGINT, " +
                            "created_at TIMESTAMP NOT NULL, " +
                            "updated_at TIMESTAMP NOT NULL" +
                            ")", schemaName);

            jdbcTemplate.execute(createProductsTable);

            // Create stock_adjustments table in tenant schema
            String createStockAdjustmentsTable = String.format(
                    "CREATE TABLE IF NOT EXISTS `%s`.`stock_adjustments` (" +
                            "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                            "product_id BIGINT NOT NULL, " +
                            "product_sku VARCHAR(255) NOT NULL, " +
                            "adjustment_type VARCHAR(50) NOT NULL, " +
                            "quantity_change INT NOT NULL, " +
                            "previous_quantity INT NOT NULL, " +
                            "new_quantity INT NOT NULL, " +
                            "reason VARCHAR(500), " +
                            "created_at TIMESTAMP NOT NULL" +
                            ")", schemaName);

            jdbcTemplate.execute(createStockAdjustmentsTable);

            log.info("Schema created successfully: {}", schemaName);
        } catch (Exception e) {
            log.error("Error creating tenant schema: {}", schemaName, e);
            throw new RuntimeException("Failed to create tenant schema", e);
        }
    }

    public List<Tenant> getAllTenants() {
        return tenantRepository.findAll();
    }

    public Tenant getTenantById(String tenantId) {
        return tenantRepository.findByTenantId(tenantId)
                .orElseThrow(() -> new RuntimeException("Tenant not found: " + tenantId));
    }

    @Transactional
    public void deactivateTenant(String tenantId) {
        Tenant tenant = getTenantById(tenantId);
        tenant.setActive(false);
        tenantRepository.save(tenant);
        log.info("Tenant deactivated: {}", tenantId);
    }
}
