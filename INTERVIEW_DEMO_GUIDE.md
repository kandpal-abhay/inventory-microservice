# Inventory Microservice - Interview Demo Guide

## 🎯 Quick Start for Interviews

This guide helps you confidently demonstrate the Inventory Microservice during technical interviews.

---

## 📋 Before the Interview

### Option 1: Live Demo Setup (Recommended)

**1. Start the Application (5 minutes before interview):**
```bash
cd ~/Documents/inventory-microservice

# Start the backend
cd backend
mvn spring-boot:run
```

**2. Wait for:**
```
Started InventoryMicroserviceApplication in X.XXX seconds
Tomcat started on port 8081
```

**3. Keep this terminal open during the interview**

### Option 2: Pre-recorded Demo

If live demo has risks, record a video beforehand:
- Use OBS Studio or screen recording
- Show all the demonstrations below
- Keep it under 5 minutes

---

## 🎬 Interview Demonstration Script

### **Introduction (30 seconds)**

**What to say:**
> "I'd like to show you the Inventory Microservice I built. It's a multi-tenant inventory management system that demonstrates enterprise-level architecture patterns. The key features are schema-based tenant isolation, optimistic locking for data integrity, and automated stock reconciliation."

---

## 🔥 Demo Scenario 1: Multi-Tenancy in Action (2 minutes)

**What it demonstrates:** Complete data isolation between tenants

### **Step 1: Create First Tenant**
```bash
curl -X POST http://localhost:8081/api/tenants \
  -H "Content-Type: application/json" \
  -d '{
    "tenantId": "electronics-store",
    "tenantName": "Electronics Store Inc"
  }'
```

**What to say:**
> "When I create a tenant, the system automatically creates a dedicated database schema. This ensures complete data isolation - each tenant's data is physically separated."

**Expected Output:**
```json
{
  "id": 1,
  "tenantId": "electronics-store",
  "tenantName": "Electronics Store Inc",
  "schemaName": "tenant_electronics-store",
  "active": true
}
```

### **Step 2: Add Product for Electronics Store**
```bash
curl -X POST http://localhost:8081/api/products \
  -H "Content-Type: application/json" \
  -H "X-Tenant-ID: electronics-store" \
  -d '{
    "sku": "LAPTOP-001",
    "name": "Dell XPS 15",
    "category": "Laptops",
    "price": 1499.99,
    "stockQuantity": 50,
    "reorderLevel": 10
  }'
```

**What to say:**
> "Notice the X-Tenant-ID header - this routes the request to the correct schema. The product is stored in the electronics-store's isolated schema."

### **Step 3: Create Second Tenant**
```bash
curl -X POST http://localhost:8081/api/tenants \
  -H "Content-Type: application/json" \
  -d '{
    "tenantId": "clothing-store",
    "tenantName": "Fashion Boutique"
  }'
```

### **Step 4: Add Product for Clothing Store**
```bash
curl -X POST http://localhost:8081/api/products \
  -H "Content-Type: application/json" \
  -H "X-Tenant-ID: clothing-store" \
  -d '{
    "sku": "SHIRT-001",
    "name": "Cotton T-Shirt",
    "category": "Clothing",
    "price": 29.99,
    "stockQuantity": 200,
    "reorderLevel": 50
  }'
```

### **Step 5: Prove Tenant Isolation**
```bash
# Get products for Electronics Store
curl -H "X-Tenant-ID: electronics-store" \
  http://localhost:8081/api/products

# Output: Shows ONLY Dell XPS 15

# Get products for Clothing Store
curl -H "X-Tenant-ID: clothing-store" \
  http://localhost:8081/api/products

# Output: Shows ONLY Cotton T-Shirt
```

**What to say:**
> "As you can see, each tenant only sees their own products. The electronics store cannot access the clothing store's data, and vice versa. This is true multi-tenancy with schema-based isolation."

---

## 🔥 Demo Scenario 2: Optimistic Locking (2 minutes)

**What it demonstrates:** Data integrity during concurrent updates

### **Step 1: Show Initial Product State**
```bash
curl -H "X-Tenant-ID: electronics-store" \
  http://localhost:8081/api/products/1
```

**What to say:**
> "Notice the version field - it's currently 0. This is used for optimistic locking to prevent data conflicts."

**Output:**
```json
{
  "id": 1,
  "sku": "LAPTOP-001",
  "stockQuantity": 50,
  "version": 0,
  ...
}
```

### **Step 2: Update Stock - Make a Sale**
```bash
curl -X PATCH http://localhost:8081/api/products/1/stock \
  -H "Content-Type: application/json" \
  -H "X-Tenant-ID: electronics-store" \
  -d '{
    "quantityChange": -5,
    "adjustmentType": "SALE",
    "reason": "Customer order #12345"
  }'
```

**What to say:**
> "I'm simulating a sale of 5 laptops. Watch what happens to the version number."

**Output:**
```json
{
  "id": 1,
  "stockQuantity": 45,
  "version": 1,  // ← Incremented!
  ...
}
```

### **Step 3: Explain Concurrent Update Protection**

**What to say:**
> "If two warehouse managers try to update stock simultaneously, the version field prevents conflicts. The first update succeeds and increments the version. The second update detects the version changed and automatically retries with the latest data. This prevents the classic 'lost update' problem."

**Show the code (if asked):**
```java
@Version
private Long version;  // Hibernate automatically handles this

@Retryable(
    retryFor = OptimisticLockException.class,
    maxAttempts = 3
)
public Product updateStock(...) { ... }
```

---

## 🔥 Demo Scenario 3: Complete Audit Trail (1 minute)

**What it demonstrates:** Full traceability of all stock movements

### **Step 1: View Stock History**
```bash
curl -H "X-Tenant-ID: electronics-store" \
  http://localhost:8081/api/products/1/stock-history
```

**What to say:**
> "Every stock change is recorded with complete audit information - what changed, when, why, and by how much."

**Output:**
```json
[
  {
    "id": 1,
    "productSku": "LAPTOP-001",
    "adjustmentType": "RESTOCK",
    "quantityChange": 50,
    "previousQuantity": 0,
    "newQuantity": 50,
    "reason": "Initial stock",
    "createdAt": "2025-10-28T10:00:00"
  },
  {
    "id": 2,
    "adjustmentType": "SALE",
    "quantityChange": -5,
    "previousQuantity": 50,
    "newQuantity": 45,
    "reason": "Customer order #12345",
    "createdAt": "2025-10-28T11:30:00"
  }
]
```

**What to say:**
> "This is crucial for compliance, accounting, and dispute resolution. You can trace every inventory movement back to its source."

---

## 🔥 Demo Scenario 4: Low Stock Monitoring (Optional - 1 minute)

### **Step 1: Reduce Stock Below Reorder Level**
```bash
curl -X PATCH http://localhost:8081/api/products/1/stock \
  -H "Content-Type: application/json" \
  -H "X-Tenant-ID: electronics-store" \
  -d '{
    "quantityChange": -40,
    "adjustmentType": "SALE",
    "reason": "Black Friday sale"
  }'
```

### **Step 2: Check Products Needing Reorder**
```bash
curl -H "X-Tenant-ID: electronics-store" \
  http://localhost:8081/api/products/reorder-needed
```

**What to say:**
> "The system automatically identifies products that need reordering. There's also a scheduled job that runs hourly to alert about low stock levels."

---

## 💡 Key Talking Points for Interviews

### **Architecture Questions**

**Q: Why schema-based multi-tenancy instead of row-based?**
> "Schema-based provides better isolation and security. If there's a bug in the query logic, tenants can't accidentally access each other's data. It also allows per-tenant database tuning and easier data migrations."

**Q: How do you handle the tenant routing?**
> "I use a ThreadLocal context to store the tenant ID from the HTTP header. A custom DataSource implementation reads this context and routes queries to the correct schema. The interceptor extracts the tenant ID before the controller, and clears it after the response."

**Q: What happens if two users update the same product simultaneously?**
> "The @Version annotation with optimistic locking handles this. If user B tries to update while user A already updated, the version check fails. The @Retryable annotation automatically retries up to 3 times with exponential backoff."

### **Scalability Questions**

**Q: Can this handle thousands of tenants?**
> "Yes. Schema-based isolation scales well because each tenant's data is independent. You can shard tenants across multiple database servers, implement read replicas per tenant group, or migrate high-volume tenants to dedicated databases without changing application code."

**Q: What about database connection pooling?**
> "Currently using dynamic datasource switching. For production at scale, I'd implement connection pool per tenant with eviction strategies for inactive tenants, or use a connection proxy like PgBouncer."

### **Production Readiness Questions**

**Q: How would you monitor this in production?**
> "I'd add:
- Metrics for stock updates per tenant
- Alerts on optimistic lock retry rates
- Monitoring of scheduled job execution
- Database connection pool metrics
- Per-tenant API usage tracking
- Low stock alerts via email/Slack integration"

**Q: What about testing?**
> "The project needs:
- Unit tests for service layer logic
- Integration tests for multi-tenancy isolation
- Concurrent update tests for optimistic locking
- Load tests simulating multiple tenants
- Schema migration tests for new tenant provisioning"

---

## 📊 Database Demonstration (If Asked)

### **Show Database Schemas:**
```bash
mysql -u inventory_user -p'inventory_password' -e "SHOW DATABASES LIKE '%tenant%';"
```

**Output:**
```
tenant_electronics-store
tenant_clothing-store
```

### **Show Tables in Tenant Schema:**
```bash
mysql -u inventory_user -p'inventory_password' -e "
USE tenant_electronics_store;
SHOW TABLES;
"
```

**Output:**
```
products
stock_adjustments
```

### **Show Version Column:**
```bash
mysql -u inventory_user -p'inventory_password' -e "
USE tenant_electronics_store;
SELECT id, sku, stock_quantity, version FROM products;
"
```

---

## 🎓 Technical Deep Dive (If They Want Details)

### **Code Walkthrough:**

**1. Tenant Context Management:**
```java
// TenantContext.java
private static final ThreadLocal<String> currentTenant = new ThreadLocal<>();

public static void setCurrentTenant(String tenantId) {
    currentTenant.set(tenantId);
}
```

**2. Request Interception:**
```java
// TenantInterceptor.java
@Override
public boolean preHandle(HttpServletRequest request, ...) {
    String tenantId = request.getHeader("X-Tenant-ID");
    TenantContext.setCurrentTenant(tenantId);
    return true;
}
```

**3. Dynamic Datasource Routing:**
```java
// TenantDataSource.java
@Override
protected Object determineCurrentLookupKey() {
    return TenantContext.getCurrentTenant();
}
```

**4. Optimistic Locking:**
```java
// Product.java
@Version
private Long version;

// ProductService.java
@Retryable(retryFor = OptimisticLockException.class, maxAttempts = 3)
public Product updateStock(...) { ... }
```

---

## 🎯 Common Interview Questions & Answers

### **Q1: Why did you build this project?**
> "I wanted to demonstrate understanding of enterprise-level patterns like multi-tenancy and data consistency mechanisms. These are common requirements in SaaS applications, and I wanted hands-on experience implementing them properly."

### **Q2: What was the biggest challenge?**
> "Implementing true schema-based multi-tenancy was complex. I had to handle dynamic schema creation, datasource routing, and ensure complete isolation. The ThreadLocal context management and proper cleanup was tricky to get right."

### **Q3: What would you improve?**
> "For production, I'd add:
- Comprehensive test coverage (unit, integration, load tests)
- Metrics and monitoring (Prometheus/Grafana)
- Tenant-specific rate limiting
- Database migration versioning per tenant
- Caching layer (Redis) per tenant
- Event-driven architecture for stock updates (Kafka)
- API documentation (Swagger/OpenAPI)"

### **Q4: How does this compare to row-level multi-tenancy?**
> "Row-level uses a tenant_id column and filters all queries. It's simpler but riskier - one missing WHERE clause exposes all data. Schema-based is more complex but safer - tenants are physically isolated. Trade-off is schema-based requires more database resources."

### **Q5: Explain the scheduled jobs**
> "There are two jobs:
1. Daily reconciliation (2 AM) - analyzes inventory, calculates value, identifies reorder needs
2. Hourly low-stock check - monitors critical stock levels

Both iterate through all active tenants, setting the context for each, processing their data independently."

---

## 📱 Demo on Your Laptop

### **Setup (30 seconds):**
1. Open terminal
2. Navigate to project: `cd ~/Documents/inventory-microservice/backend`
3. Run: `mvn spring-boot:run`
4. Wait for "Started InventoryMicroserviceApplication"

### **Alternative: Show Postman Collection**
1. Open Postman
2. Import: `Inventory_Microservice_API.postman_collection.json`
3. Show organized requests:
   - Tenant Management
   - Product CRUD
   - Stock Operations
   - Multi-Tenancy Tests

### **Alternative: Show GitHub Repository**
1. Open: https://github.com/kandpal-abhay/inventory-microservice
2. Walk through:
   - README.md - Complete documentation
   - Code structure - Professional organization
   - TEST_RESULTS.md - Proof of testing

---

## ⚡ Quick Demo (30 seconds version)

If time is very limited:

```bash
# Show it's running
curl http://localhost:8081/api/tenants

# Explain multi-tenancy
echo "Each tenant gets isolated schema"

# Show optimistic locking
echo "Products have version field for concurrent updates"

# Show documentation
cat README.md | head -20
```

---

## 🎬 Closing Statement

**What to say:**
> "This project demonstrates my ability to build enterprise-grade applications with proper architecture patterns. The multi-tenancy ensures scalability for SaaS use cases, optimistic locking ensures data integrity, and the audit trail provides compliance. The code is production-ready with proper error handling, logging, and documentation. I'm happy to discuss any aspect in more detail."

---

## 📝 Cheat Sheet (Keep This Handy)

**Start Application:**
```bash
cd ~/Documents/inventory-microservice/backend && mvn spring-boot:run
```

**Quick Test:**
```bash
curl http://localhost:8081/api/tenants
```

**Create Tenant:**
```bash
curl -X POST http://localhost:8081/api/tenants -H "Content-Type: application/json" \
  -d '{"tenantId":"demo","tenantName":"Demo Company"}'
```

**Add Product:**
```bash
curl -X POST http://localhost:8081/api/products -H "X-Tenant-ID: demo" \
  -H "Content-Type: application/json" -d '{"sku":"DEMO-001","name":"Demo Product",
  "category":"Demo","price":99.99,"stockQuantity":100,"reorderLevel":10}'
```

**Show Isolation:**
```bash
curl -H "X-Tenant-ID: demo" http://localhost:8081/api/products
```

---

**Good luck with your interviews! 🚀**

You've got a solid project that demonstrates real-world skills. Be confident!
