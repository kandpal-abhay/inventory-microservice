# Inventory Microservice

A production-ready, multi-tenant inventory management microservice built with Spring Boot 3.2 and Java 17.

## Features

### Multi-Tenancy Support
- **Schema-based isolation** - Each tenant gets a dedicated database schema
- **Dynamic tenant provisioning** - Create new tenants via API
- **Automatic schema creation** - Tenant schemas are created automatically
- **Tenant context management** - Thread-safe tenant identification via headers
- **Complete data isolation** - Zero cross-tenant data leakage

### Product Management
- **Full CRUD operations** - Create, Read, Update, Delete products
- **SKU-based lookup** - Find products by unique SKU
- **Category filtering** - Filter products by category
- **Active/Inactive status** - Soft delete functionality
- **Stock tracking** - Real-time inventory levels
- **Reorder management** - Automatic low stock detection

### Stock Management
- **Optimistic locking** - Prevents concurrent modification conflicts using `@Version`
- **Automatic retry** - Failed updates due to version conflicts are retried (max 3 attempts)
- **Stock adjustments** - Track all inventory changes with full audit trail
- **Adjustment types**:
  - RESTOCK - Incoming inventory
  - SALE - Sold items
  - DAMAGE - Damaged/lost items
  - RECONCILIATION - Manual adjustments
- **Stock history** - Complete audit trail for each product

### Scheduled Jobs
- **Daily reconciliation** - Runs at 2 AM to analyze inventory
- **Hourly low-stock alerts** - Monitors products below reorder level
- **Multi-tenant processing** - Jobs run for all active tenants
- **Inventory value calculation** - Total value reporting

## Technology Stack

- **Java 17**
- **Spring Boot 3.2.0**
- **Spring Data JPA**
- **MySQL 8.0+**
- **Hibernate ORM**
- **Spring Retry** - Optimistic locking retry mechanism
- **Lombok** - Reduced boilerplate
- **Maven** - Dependency management

## Architecture

### Multi-Tenancy Design

```
Request → TenantInterceptor → TenantContext → TenantDataSource → Schema Selection
                ↓
        Extract X-Tenant-ID header
                ↓
        Set thread-local context
                ↓
        Route to tenant schema
```

### Database Structure

**Master Schema (`inventory_master`):**
- `tenants` - Tenant registry and metadata

**Tenant Schemas (`tenant_company-a`, `tenant_company-b`, etc.):**
- `products` - Product catalog with optimistic locking
- `stock_adjustments` - Complete stock movement history

## Getting Started

### Prerequisites

- Java 17 or higher
- Maven 3.6+
- MySQL 8.0+

### Database Setup

1. **Start MySQL:**
```bash
sudo systemctl start mysql
```

2. **Create MySQL User:**
```bash
sudo mysql -u root -p
```

```sql
CREATE USER 'inventory_user'@'localhost' IDENTIFIED BY 'inventory_password';
GRANT ALL PRIVILEGES ON *.* TO 'inventory_user'@'localhost';
FLUSH PRIVILEGES;
EXIT;
```

The master database (`inventory_master`) will be created automatically.

### Running the Application

1. **Navigate to backend directory:**
```bash
cd ~/Documents/inventory-microservice/backend
```

2. **Build the project:**
```bash
mvn clean install
```

3. **Run the application:**
```bash
mvn spring-boot:run
```

The application will start on `http://localhost:8081`

## API Documentation

### Tenant Management

#### Create Tenant
```http
POST /api/tenants
Content-Type: application/json

{
  "tenantId": "company-a",
  "tenantName": "Company A Inc"
}
```

**Response:**
```json
{
  "id": 1,
  "tenantId": "company-a",
  "tenantName": "Company A Inc",
  "schemaName": "tenant_company-a",
  "active": true,
  "createdAt": "2025-10-28T10:00:00",
  "updatedAt": "2025-10-28T10:00:00"
}
```

#### Get All Tenants
```http
GET /api/tenants
```

#### Get Tenant by ID
```http
GET /api/tenants/{tenantId}
```

#### Deactivate Tenant
```http
DELETE /api/tenants/{tenantId}
```

### Product Management

**Important:** All product endpoints require the `X-Tenant-ID` header!

#### Create Product
```http
POST /api/products
X-Tenant-ID: company-a
Content-Type: application/json

{
  "sku": "LAPTOP-001",
  "name": "Dell XPS 15",
  "description": "High-performance laptop",
  "category": "Electronics",
  "price": 1499.99,
  "stockQuantity": 50,
  "reorderLevel": 10
}
```

#### Get All Products
```http
GET /api/products
X-Tenant-ID: company-a
```

**Optional query parameters:**
- `category` - Filter by category
- `activeOnly=true` - Show only active products

#### Get Product by ID
```http
GET /api/products/{id}
X-Tenant-ID: company-a
```

#### Get Product by SKU
```http
GET /api/products/sku/{sku}
X-Tenant-ID: company-a
```

#### Get Products Needing Reorder
```http
GET /api/products/reorder-needed
X-Tenant-ID: company-a
```

#### Update Product
```http
PUT /api/products/{id}
X-Tenant-ID: company-a
Content-Type: application/json

{
  "sku": "LAPTOP-001",
  "name": "Dell XPS 15 Updated",
  "description": "Updated description",
  "category": "Electronics",
  "price": 1599.99,
  "stockQuantity": 50,
  "reorderLevel": 15
}
```

#### Update Stock (with Optimistic Locking)
```http
PATCH /api/products/{id}/stock
X-Tenant-ID: company-a
Content-Type: application/json

{
  "quantityChange": 20,
  "adjustmentType": "RESTOCK",
  "reason": "Weekly restock from supplier"
}
```

**Adjustment Types:**
- `RESTOCK` - Add inventory
- `SALE` - Remove inventory (sale)
- `DAMAGE` - Remove inventory (damaged)
- `RECONCILIATION` - Manual adjustment

**Optimistic Locking:**
If two requests try to update stock simultaneously, the second one will fail and automatically retry (up to 3 times). This ensures data consistency.

#### Get Stock History
```http
GET /api/products/{id}/stock-history
X-Tenant-ID: company-a
```

#### Delete Product (Soft Delete)
```http
DELETE /api/products/{id}
X-Tenant-ID: company-a
```

## Testing with Postman

A comprehensive Postman collection is included: `Inventory_Microservice_API.postman_collection.json`

**Import into Postman:**
1. Open Postman
2. Click **Import**
3. Select `Inventory_Microservice_API.postman_collection.json`

**Test Scenarios Included:**
1. **Tenant Creation** - Create multiple tenants
2. **Product CRUD** - Full product lifecycle
3. **Stock Management** - Various stock operations
4. **Multi-Tenancy Isolation** - Verify data isolation between tenants
5. **Optimistic Locking** - Concurrent update handling

## Key Features Demonstrated

### 1. Multi-Tenant Isolation

```bash
# Create Tenant A
curl -X POST http://localhost:8081/api/tenants \
  -H "Content-Type: application/json" \
  -d '{"tenantId":"company-a","tenantName":"Company A"}'

# Create Product for Tenant A
curl -X POST http://localhost:8081/api/products \
  -H "Content-Type: application/json" \
  -H "X-Tenant-ID: company-a" \
  -d '{"sku":"PROD-A","name":"Product A","category":"Electronics","price":100,"stockQuantity":50,"reorderLevel":10}'

# Create Tenant B
curl -X POST http://localhost:8081/api/tenants \
  -H "Content-Type: application/json" \
  -d '{"tenantId":"company-b","tenantName":"Company B"}'

# Get Products for Tenant B (should be empty)
curl http://localhost:8081/api/products \
  -H "X-Tenant-ID: company-b"
```

### 2. Optimistic Locking in Action

**Scenario:** Two users try to update the same product's stock simultaneously

```bash
# User 1: Update stock
curl -X PATCH http://localhost:8081/api/products/1/stock \
  -H "Content-Type: application/json" \
  -H "X-Tenant-ID: company-a" \
  -d '{"quantityChange":-5,"adjustmentType":"SALE","reason":"Order #123"}'

# User 2: Update same product simultaneously
curl -X PATCH http://localhost:8081/api/products/1/stock \
  -H "Content-Type: application/json" \
  -H "X-Tenant-ID: company-a" \
  -d '{"quantityChange":-3,"adjustmentType":"SALE","reason":"Order #124"}'
```

**Result:** The second request automatically retries if there's a version conflict, ensuring both updates succeed without data loss.

### 3. Stock Reconciliation Job

The system automatically runs reconciliation jobs:

**Daily at 2 AM:**
- Counts total products per tenant
- Identifies products needing reorder
- Calculates total inventory value
- Logs comprehensive reports

**Hourly:**
- Checks for low stock items
- Generates alerts for products below reorder level

**View logs:**
```bash
tail -f logs/application.log
```

## Error Handling

The API uses standard HTTP status codes:

- `200 OK` - Successful request
- `201 Created` - Resource created successfully
- `400 Bad Request` - Invalid request data
- `404 Not Found` - Resource not found
- `409 Conflict` - Optimistic locking failure (will auto-retry)
- `500 Internal Server Error` - Server error

**Error Response Format:**
```json
{
  "status": 409,
  "message": "The resource was modified by another user. Please retry your operation.",
  "timestamp": "2025-10-28T10:30:00"
}
```

## Configuration

Edit `src/main/resources/application.properties` to customize:

```properties
# Server port
server.port=8081

# Database credentials
spring.datasource.username=inventory_user
spring.datasource.password=inventory_password

# Tenant header name
multitenancy.tenant.header=X-Tenant-ID

# Scheduling
spring.task.scheduling.enabled=true
```

## Project Structure

```
inventory-microservice/
├── backend/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/inventory/microservice/
│   │   │   │   ├── config/           # Multi-tenancy configuration
│   │   │   │   │   ├── MultiTenantConfig.java
│   │   │   │   │   ├── TenantContext.java
│   │   │   │   │   ├── TenantDataSource.java
│   │   │   │   │   ├── TenantInterceptor.java
│   │   │   │   │   └── WebMvcConfig.java
│   │   │   │   ├── controller/       # REST API controllers
│   │   │   │   │   ├── ProductController.java
│   │   │   │   │   └── TenantController.java
│   │   │   │   ├── dto/              # Data transfer objects
│   │   │   │   │   ├── CreateProductRequest.java
│   │   │   │   │   ├── CreateTenantRequest.java
│   │   │   │   │   └── UpdateStockRequest.java
│   │   │   │   ├── entity/           # JPA entities
│   │   │   │   │   ├── Product.java (with @Version)
│   │   │   │   │   ├── StockAdjustment.java
│   │   │   │   │   └── Tenant.java
│   │   │   │   ├── exception/        # Global exception handling
│   │   │   │   │   └── GlobalExceptionHandler.java
│   │   │   │   ├── repository/       # Spring Data repositories
│   │   │   │   │   ├── ProductRepository.java
│   │   │   │   │   ├── StockAdjustmentRepository.java
│   │   │   │   │   └── TenantRepository.java
│   │   │   │   ├── scheduler/        # Scheduled jobs
│   │   │   │   │   └── StockReconciliationJob.java
│   │   │   │   ├── service/          # Business logic
│   │   │   │   │   ├── ProductService.java (with @Retryable)
│   │   │   │   │   └── TenantService.java
│   │   │   │   └── InventoryMicroserviceApplication.java
│   │   │   └── resources/
│   │   │       └── application.properties
│   │   └── test/
│   └── pom.xml
├── Inventory_Microservice_API.postman_collection.json
└── README.md
```

## Resume-Worthy Features

This project demonstrates:

1. **Multi-Tenant Architecture** - Schema-based isolation with dynamic provisioning
2. **Optimistic Locking** - Handling concurrent updates with automatic retry
3. **RESTful API Design** - Well-documented, comprehensive endpoints
4. **Scheduled Jobs** - Automated stock reconciliation and monitoring
5. **Audit Trail** - Complete stock movement history
6. **Error Handling** - Global exception handling with meaningful responses
7. **Production-Ready Code** - Proper logging, validation, and configuration
8. **Spring Best Practices** - Layered architecture, dependency injection
9. **Database Schema Management** - Dynamic schema creation
10. **API Testing** - Comprehensive Postman collection

## Author

**Abhay Kandpal**

Created: October 2025

Purpose: Portfolio project demonstrating microservices architecture and multi-tenancy expertise

## License

This project is created for portfolio purposes.
