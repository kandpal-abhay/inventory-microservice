package com.inventory.microservice.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
@Slf4j
public class TenantInterceptor implements HandlerInterceptor {

    @Value("${multitenancy.tenant.header:X-Tenant-ID}")
    private String tenantHeader;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String tenantId = request.getHeader(tenantHeader);

        // Skip tenant validation for tenant management endpoints
        String requestPath = request.getRequestURI();
        if (requestPath.startsWith("/api/tenants")) {
            log.debug("Skipping tenant validation for tenant management endpoint");
            return true;
        }

        if (tenantId == null || tenantId.isEmpty()) {
            log.error("Missing tenant header: {}", tenantHeader);
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return false;
        }

        log.debug("Setting tenant context: {}", tenantId);
        TenantContext.setCurrentTenant(tenantId);
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        TenantContext.clear();
    }
}
