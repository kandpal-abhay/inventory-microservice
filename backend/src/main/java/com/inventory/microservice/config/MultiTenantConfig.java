package com.inventory.microservice.config;

import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

@Configuration
@Slf4j
public class MultiTenantConfig {

    @Value("${multitenancy.master.datasource.url}")
    private String masterUrl;

    @Value("${multitenancy.master.datasource.username}")
    private String masterUsername;

    @Value("${multitenancy.master.datasource.password}")
    private String masterPassword;

    @Bean
    @Primary
    public DataSource dataSource() {
        TenantDataSource tenantDataSource = new TenantDataSource();

        Map<Object, Object> targetDataSources = new HashMap<>();

        // Master datasource for tenant management
        DataSource masterDataSource = createDataSource("inventory_master");
        targetDataSources.put("master", masterDataSource);

        tenantDataSource.setTargetDataSources(targetDataSources);
        tenantDataSource.setDefaultTargetDataSource(masterDataSource);
        tenantDataSource.afterPropertiesSet();

        log.info("Multi-tenant datasource configured with master schema");
        return tenantDataSource;
    }

    private DataSource createDataSource(String schemaName) {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        String url = masterUrl.replace("inventory_master", schemaName);
        dataSource.setUrl(url);
        dataSource.setUsername(masterUsername);
        dataSource.setPassword(masterPassword);
        dataSource.setDriverClassName("com.mysql.cj.jdbc.Driver");

        log.debug("Created datasource for schema: {}", schemaName);
        return dataSource;
    }

    // Method to dynamically add tenant datasource at runtime
    public void addTenantDataSource(String tenantId, String schemaName) {
        TenantDataSource tenantDataSource = (TenantDataSource) dataSource();
        DataSource newDataSource = createDataSource(schemaName);

        Map<Object, Object> targetDataSources = new HashMap<>(tenantDataSource.getResolvedDataSources());
        targetDataSources.put(tenantId, newDataSource);

        tenantDataSource.setTargetDataSources(targetDataSources);
        tenantDataSource.afterPropertiesSet();

        log.info("Added new tenant datasource: {} -> schema: {}", tenantId, schemaName);
    }
}
