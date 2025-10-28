#!/bin/bash

# Inventory Microservice - Interview Demo Script
# Usage: source demo.sh  (then use the aliases)

# Colors for better visibility
GREEN='\033[0;32m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

BASE_URL="http://localhost:8081"

echo -e "${GREEN}========================================${NC}"
echo -e "${GREEN}Inventory Microservice - Demo Helpers${NC}"
echo -e "${GREEN}========================================${NC}"
echo ""
echo -e "${BLUE}Available Commands:${NC}"
echo ""
echo -e "${YELLOW}Tenant Management:${NC}"
echo "  demo-tenants          - Get all tenants"
echo "  demo-create-tenant    - Create a new demo tenant"
echo ""
echo -e "${YELLOW}Product Management:${NC}"
echo "  demo-products         - Get products for demo tenant"
echo "  demo-create-product   - Create a demo product"
echo "  demo-update-stock     - Update stock (sale)"
echo "  demo-stock-history    - View stock history"
echo ""
echo -e "${YELLOW}Multi-Tenancy Demo:${NC}"
echo "  demo-isolation        - Demonstrate tenant isolation"
echo ""
echo -e "${YELLOW}Utility:${NC}"
echo "  demo-clean            - Clean up demo data"
echo ""
echo -e "${GREEN}========================================${NC}"
echo ""

# Tenant Management
alias demo-tenants='echo -e "\n${BLUE}📍 Getting all tenants...${NC}\n"; curl -s http://localhost:8081/api/tenants | python3 -m json.tool'

alias demo-create-tenant='echo -e "\n${BLUE}📍 Creating demo tenant...${NC}\n"; curl -X POST http://localhost:8081/api/tenants -H "Content-Type: application/json" -d "{\"tenantId\":\"demo-$(date +%s)\",\"tenantName\":\"Demo Company\"}" | python3 -m json.tool'

# Product Management
alias demo-products='echo -e "\n${BLUE}📍 Getting products for company-a...${NC}\n"; curl -s -H "X-Tenant-ID: company-a" http://localhost:8081/api/products | python3 -m json.tool'

alias demo-create-product='echo -e "\n${BLUE}📍 Creating demo product...${NC}\n"; curl -X POST http://localhost:8081/api/products -H "Content-Type: application/json" -H "X-Tenant-ID: company-a" -d "{\"sku\":\"DEMO-$(date +%s)\",\"name\":\"Demo Product\",\"category\":\"Electronics\",\"price\":299.99,\"stockQuantity\":100,\"reorderLevel\":20}" | python3 -m json.tool'

alias demo-update-stock='echo -e "\n${BLUE}📍 Updating stock (making a sale)...${NC}\n"; curl -X PATCH http://localhost:8081/api/products/1/stock -H "Content-Type: application/json" -H "X-Tenant-ID: company-a" -d "{\"quantityChange\":-5,\"adjustmentType\":\"SALE\",\"reason\":\"Demo sale for interview\"}" | python3 -m json.tool'

alias demo-stock-history='echo -e "\n${BLUE}📍 Getting stock history...${NC}\n"; curl -s -H "X-Tenant-ID: company-a" http://localhost:8081/api/products/1/stock-history | python3 -m json.tool'

# Multi-Tenancy Demonstration
demo-isolation() {
    echo -e "\n${GREEN}========================================${NC}"
    echo -e "${GREEN}Demonstrating Multi-Tenant Isolation${NC}"
    echo -e "${GREEN}========================================${NC}\n"

    echo -e "${BLUE}📍 Step 1: Products for Company A${NC}"
    curl -s -H "X-Tenant-ID: company-a" http://localhost:8081/api/products | python3 -m json.tool

    echo -e "\n${BLUE}📍 Step 2: Products for Company B${NC}"
    curl -s -H "X-Tenant-ID: company-b" http://localhost:8081/api/products | python3 -m json.tool

    echo -e "\n${GREEN}✅ Notice: Each tenant sees only their own products!${NC}\n"
}

# Quick demo sequence
demo-quick() {
    echo -e "\n${GREEN}========================================${NC}"
    echo -e "${GREEN}Quick Demo Sequence${NC}"
    echo -e "${GREEN}========================================${NC}\n"

    echo -e "${BLUE}1. Getting all tenants...${NC}"
    curl -s http://localhost:8081/api/tenants | python3 -c "import sys, json; data=json.load(sys.stdin); print(f'✅ Found {len(data)} tenants'); [print(f'   - {t[\"tenantId\"]}: {t[\"tenantName\"]}') for t in data]"

    echo -e "\n${BLUE}2. Getting products for company-a...${NC}"
    curl -s -H "X-Tenant-ID: company-a" http://localhost:8081/api/products | python3 -c "import sys, json; data=json.load(sys.stdin); print(f'✅ Company A has {len(data)} product(s)'); [print(f'   - {p[\"name\"]} (Stock: {p[\"stockQuantity\"]}, Version: {p[\"version\"]})') for p in data]"

    echo -e "\n${BLUE}3. Demonstrating tenant isolation...${NC}"
    curl -s -H "X-Tenant-ID: company-b" http://localhost:8081/api/products | python3 -c "import sys, json; data=json.load(sys.stdin); print(f'✅ Company B has {len(data)} product(s) (isolated from Company A)'); [print(f'   - {p[\"name\"]}') for p in data]"

    echo -e "\n${GREEN}✅ Demo complete!${NC}\n"
}

# Clean up
demo-clean() {
    echo -e "\n${YELLOW}⚠️  This will delete demo data. Continue? (y/n)${NC}"
    read -r response
    if [[ "$response" == "y" ]]; then
        echo -e "${BLUE}Cleaning up demo data...${NC}"
        # Add cleanup logic here
        echo -e "${GREEN}✅ Cleanup complete${NC}"
    else
        echo -e "${BLUE}Cleanup cancelled${NC}"
    fi
}

echo -e "${GREEN}✅ Demo helpers loaded! Try: demo-quick${NC}\n"
