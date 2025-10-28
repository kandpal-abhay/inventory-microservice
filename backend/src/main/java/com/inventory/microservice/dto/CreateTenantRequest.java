package com.inventory.microservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateTenantRequest {

    @NotBlank(message = "Tenant ID is required")
    @Pattern(regexp = "^[a-z0-9_-]+$", message = "Tenant ID must contain only lowercase letters, numbers, hyphens, and underscores")
    private String tenantId;

    @NotBlank(message = "Tenant name is required")
    private String tenantName;
}
