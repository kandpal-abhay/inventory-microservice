#!/bin/bash

# Inventory Microservice - Database Setup Script
# This script creates the MySQL user and grants necessary privileges

echo "========================================"
echo "Inventory Microservice - Database Setup"
echo "========================================"
echo ""

# Check if MySQL is installed
if ! command -v mysql &> /dev/null; then
    echo "ERROR: MySQL is not installed or not in PATH"
    exit 1
fi

echo "Creating MySQL user 'inventory_user'..."
echo "You will be prompted for MySQL root password."
echo ""

# Execute SQL commands
sudo mysql -u root -p <<EOF
-- Create user if not exists
CREATE USER IF NOT EXISTS 'inventory_user'@'localhost' IDENTIFIED BY 'inventory_password';

-- Grant all privileges on all databases (needed for creating tenant schemas)
GRANT ALL PRIVILEGES ON *.* TO 'inventory_user'@'localhost';

-- Flush privileges
FLUSH PRIVILEGES;

-- Show created user
SELECT user, host FROM mysql.user WHERE user = 'inventory_user';
EOF

if [ $? -eq 0 ]; then
    echo ""
    echo "✅ Database user created successfully!"
    echo ""
    echo "User details:"
    echo "  Username: inventory_user"
    echo "  Password: inventory_password"
    echo "  Privileges: ALL on all databases"
    echo ""
    echo "The application will automatically create:"
    echo "  - Master database: inventory_master"
    echo "  - Tenant schemas: tenant_<tenant-id>"
    echo ""
    echo "You can now run the application:"
    echo "  cd backend && mvn spring-boot:run"
else
    echo ""
    echo "❌ Failed to create database user"
    echo "Please check your MySQL root password and try again"
    exit 1
fi
