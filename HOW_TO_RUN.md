# How to Run Inventory Microservice

## ⚡ Quick Start (For Daily Use)

### Step 1: Start MySQL
```bash
sudo systemctl start mysql
```

### Step 2: Navigate to Project
```bash
cd ~/Documents/inventory-microservice/backend
```

### Step 3: Run the Application
```bash
mvn spring-boot:run
```

### Step 4: Wait for Startup
Look for this message:
```
Started InventoryMicroserviceApplication in X.XXX seconds (process running for Y.YYY)
Tomcat started on port 8081 (http) with context path ''
```

### Step 5: Test It's Running
Open another terminal and run:
```bash
curl http://localhost:8081/api/tenants
```

You should see a JSON response with tenant data.

---

## 🎯 For Interviews - Quick Demo

### Option 1: Live Demo (Recommended)

**Before Interview (5 minutes):**
```bash
# Terminal 1: Start the application
cd ~/Documents/inventory-microservice/backend
mvn spring-boot:run

# Terminal 2: Have these commands ready
curl http://localhost:8081/api/tenants
```

**During Interview:**
- Follow the **INTERVIEW_DEMO_GUIDE.md** for complete demo scenarios
- Show multi-tenancy, optimistic locking, and audit trail features

### Option 2: Use Postman

**Before Interview:**
1. Open Postman
2. Import: `Inventory_Microservice_API.postman_collection.json`
3. Test all endpoints beforehand

**During Interview:**
- Show organized API collection
- Execute requests live
- Demonstrate tenant isolation

### Option 3: Show GitHub Repository

If live demo isn't feasible:
1. Open: https://github.com/kandpal-abhay/inventory-microservice
2. Walk through code structure
3. Show README.md and TEST_RESULTS.md
4. Explain architecture

---

## 🔧 Troubleshooting

### Application Won't Start

**Error: Port 8081 already in use**
```bash
# Find process using port 8081
sudo lsof -i :8081

# Kill it
kill -9 <PID>

# Or use different port in application.properties
```

**Error: MySQL connection failed**
```bash
# Check MySQL is running
sudo systemctl status mysql

# Start MySQL if needed
sudo systemctl start mysql

# Test connection
mysql -u inventory_user -p'inventory_password' -e "SELECT 1"
```

**Error: Access denied for user**
```bash
# Run database setup again
cd ~/Documents/inventory-microservice
./setup-database.sh
```

### Stop the Application

**If running in foreground:**
```bash
Ctrl + C
```

**If running in background:**
```bash
ps aux | grep spring-boot:run | grep -v grep | awk '{print $2}' | xargs kill -9
```

---

## 📱 Testing the API

### Using cURL (Command Line)

**Get all tenants:**
```bash
curl http://localhost:8081/api/tenants
```

**Create a tenant:**
```bash
curl -X POST http://localhost:8081/api/tenants \
  -H "Content-Type: application/json" \
  -d '{"tenantId":"test-store","tenantName":"Test Store"}'
```

**Get products (requires tenant header):**
```bash
curl -H "X-Tenant-ID: test-store" \
  http://localhost:8081/api/products
```

### Using Postman

1. Import collection: `Inventory_Microservice_API.postman_collection.json`
2. All requests are pre-configured
3. Just click "Send" to test

### Using Browser

Navigate to:
```
http://localhost:8081/api/tenants
```

You'll see JSON response in the browser.

---

## 🎓 Understanding the Application

### Architecture Overview
```
Client Request
    ↓
TenantInterceptor (extracts X-Tenant-ID header)
    ↓
TenantContext (stores in ThreadLocal)
    ↓
Controller (REST API)
    ↓
Service Layer (business logic + @Retryable)
    ↓
Repository (Spring Data JPA)
    ↓
TenantDataSource (routes to correct schema)
    ↓
MySQL (tenant_<tenant-id> schema)
```

### Key Features to Demonstrate

1. **Multi-Tenancy:**
   - Create multiple tenants
   - Show data isolation
   - Explain schema-based approach

2. **Optimistic Locking:**
   - Update stock
   - Show version increment
   - Explain concurrent update protection

3. **Audit Trail:**
   - View stock history
   - Show complete traceability
   - Explain compliance benefits

4. **Scheduled Jobs:**
   - Daily reconciliation (2 AM)
   - Hourly low-stock checks
   - Multi-tenant processing

---

## 📊 Database Access

### View All Databases
```bash
mysql -u inventory_user -p'inventory_password' -e "SHOW DATABASES;"
```

### Check Tenant Schemas
```bash
mysql -u inventory_user -p'inventory_password' -e "
SHOW DATABASES LIKE 'tenant_%';
"
```

### View Products for a Tenant
```bash
mysql -u inventory_user -p'inventory_password' -e "
USE tenant_company_a;
SELECT * FROM products;
"
```

### View Stock History
```bash
mysql -u inventory_user -p'inventory_password' -e "
USE tenant_company_a;
SELECT * FROM stock_adjustments ORDER BY created_at DESC;
"
```

---

## 🚀 Advanced: Running in Production Mode

### Build JAR File
```bash
cd ~/Documents/inventory-microservice/backend
mvn clean package
```

### Run the JAR
```bash
java -jar target/inventory-microservice-1.0.0.jar
```

### Run with Custom Port
```bash
java -jar target/inventory-microservice-1.0.0.jar --server.port=8082
```

### Run in Background
```bash
nohup java -jar target/inventory-microservice-1.0.0.jar > app.log 2>&1 &
```

---

## 📝 Quick Reference Commands

| Action | Command |
|--------|---------|
| Start App | `cd backend && mvn spring-boot:run` |
| Stop App | `Ctrl + C` |
| Test Running | `curl http://localhost:8081/api/tenants` |
| View Logs | `tail -f backend/logs/application.log` |
| Build JAR | `mvn clean package` |
| Database Setup | `./setup-database.sh` |

---

## 🎯 Pre-Interview Checklist

- [ ] MySQL is running: `sudo systemctl status mysql`
- [ ] Database user exists: `mysql -u inventory_user -p'inventory_password' -e "SELECT 1"`
- [ ] Application starts: `mvn spring-boot:run`
- [ ] API responds: `curl http://localhost:8081/api/tenants`
- [ ] Postman collection imported (optional)
- [ ] INTERVIEW_DEMO_GUIDE.md reviewed
- [ ] GitHub repository accessible: https://github.com/kandpal-abhay/inventory-microservice

---

## 🆘 Common Issues & Solutions

### Issue: "Address already in use: bind"
**Solution:** Port 8081 is busy
```bash
sudo lsof -i :8081
kill -9 <PID>
```

### Issue: "Unable to acquire JDBC Connection"
**Solution:** MySQL not running or wrong credentials
```bash
sudo systemctl start mysql
./setup-database.sh
```

### Issue: "Maven command not found"
**Solution:** Maven not installed
```bash
sudo apt install maven
```

### Issue: "Java version mismatch"
**Solution:** Need Java 17
```bash
java -version
sudo update-alternatives --config java
```

---

## 📚 Additional Resources

- **Complete Documentation:** `README.md`
- **Interview Guide:** `INTERVIEW_DEMO_GUIDE.md`
- **Quick Start:** `QUICK_START.md`
- **Test Results:** `TEST_RESULTS.md`
- **Postman Collection:** `Inventory_Microservice_API.postman_collection.json`
- **GitHub Repo:** https://github.com/kandpal-abhay/inventory-microservice

---

**Your application is ready to demonstrate! 🎉**

Good luck with your interviews!
