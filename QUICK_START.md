# Inventory Microservice - Quick Start Guide

Get the Inventory Microservice running in under 5 minutes!

## Prerequisites

- Java 17+
- MySQL 8.0+
- Maven 3.6+

## Step 1: Database Setup (1 minute)

Run the automated setup script:

```bash
cd ~/Documents/inventory-microservice
./setup-database.sh
```

This creates the MySQL user `inventory_user` with password `inventory_password`.

## Step 2: Start the Application (1 minute)

```bash
cd backend
mvn spring-boot:run
```

Wait for the message:
```
Started InventoryMicroserviceApplication in X.XXX seconds
```

The API is now running at `http://localhost:8081`

## Step 3: Create Your First Tenant (30 seconds)

```bash
curl -X POST http://localhost:8081/api/tenants \
  -H "Content-Type: application/json" \
  -d '{"tenantId":"my-company","tenantName":"My Company Inc"}'
```

**Result:** A new tenant is created with its own isolated database schema!

## Step 4: Add a Product (30 seconds)

```bash
curl -X POST http://localhost:8081/api/products \
  -H "Content-Type: application/json" \
  -H "X-Tenant-ID: my-company" \
  -d '{
    "sku": "PROD-001",
    "name": "Sample Product",
    "description": "My first product",
    "category": "Electronics",
    "price": 99.99,
    "stockQuantity": 100,
    "reorderLevel": 10
  }'
```

**Result:** Product created with automatic stock tracking!

## Step 5: Make a Sale (30 seconds)

```bash
curl -X PATCH http://localhost:8081/api/products/1/stock \
  -H "Content-Type: application/json" \
  -H "X-Tenant-ID: my-company" \
  -d '{
    "quantityChange": -5,
    "adjustmentType": "SALE",
    "reason": "Customer purchase"
  }'
```

**Result:** Stock updated from 100 to 95, with full audit trail!

## Step 6: View Stock History (30 seconds)

```bash
curl http://localhost:8081/api/products/1/stock-history \
  -H "X-Tenant-ID: my-company"
```

**Result:** Complete history of all stock movements!

---

## Common Commands

### Get All Tenants
```bash
curl http://localhost:8081/api/tenants
```

### Get All Products
```bash
curl http://localhost:8081/api/products \
  -H "X-Tenant-ID: my-company"
```

### Get Products Needing Reorder
```bash
curl http://localhost:8081/api/products/reorder-needed \
  -H "X-Tenant-ID: my-company"
```

### Update Product
```bash
curl -X PUT http://localhost:8081/api/products/1 \
  -H "Content-Type: application/json" \
  -H "X-Tenant-ID: my-company" \
  -d '{
    "sku": "PROD-001",
    "name": "Updated Product Name",
    "description": "Updated description",
    "category": "Electronics",
    "price": 109.99,
    "stockQuantity": 95,
    "reorderLevel": 15
  }'
```

---

## Using Postman

1. Import the collection: `Inventory_Microservice_API.postman_collection.json`
2. All API endpoints are pre-configured with sample requests
3. Test multi-tenancy, CRUD operations, and more!

---

## Troubleshooting

### Application Won't Start

**Check MySQL is running:**
```bash
sudo systemctl status mysql
```

**Verify database user:**
```bash
mysql -u inventory_user -p'inventory_password' -e "SELECT 1"
```

### API Returns 400 Error

**Missing tenant header!** Remember to include:
```
X-Tenant-ID: your-tenant-id
```

### Optimistic Lock Error

**Someone else updated the product!** The system automatically retries up to 3 times. This is a feature, not a bug - it prevents data conflicts.

---

## Next Steps

1. Read the [README.md](README.md) for detailed documentation
2. Review [TEST_RESULTS.md](TEST_RESULTS.md) to see all features in action
3. Explore the Postman collection for API examples
4. Check the logs for scheduled job execution

---

## Architecture Highlights

- **Multi-Tenancy:** Each tenant gets a dedicated MySQL schema
- **Optimistic Locking:** Prevents concurrent update conflicts using `@Version`
- **Audit Trail:** Every stock movement is recorded with timestamp and reason
- **Scheduled Jobs:** Daily reconciliation and hourly low-stock alerts
- **RESTful APIs:** Clean, well-documented endpoints

---

## Quick Test: Multi-Tenancy Isolation

Create a second tenant and verify data isolation:

```bash
# Create Tenant B
curl -X POST http://localhost:8081/api/tenants \
  -H "Content-Type: application/json" \
  -d '{"tenantId":"company-b","tenantName":"Company B"}'

# Check products for Tenant B (should be empty)
curl http://localhost:8081/api/products \
  -H "X-Tenant-ID: company-b"

# Create product for Tenant B
curl -X POST http://localhost:8081/api/products \
  -H "Content-Type: application/json" \
  -H "X-Tenant-ID: company-b" \
  -d '{
    "sku": "B-PROD-001",
    "name": "Tenant B Product",
    "category": "Tools",
    "price": 49.99,
    "stockQuantity": 50,
    "reorderLevel": 5
  }'

# Verify Tenant A cannot see Tenant B's products
curl http://localhost:8081/api/products \
  -H "X-Tenant-ID: my-company"
```

**Result:** Complete data isolation - tenants cannot access each other's data!

---

**You're all set! 🚀**

For detailed API documentation, see [README.md](README.md) or import the Postman collection.
