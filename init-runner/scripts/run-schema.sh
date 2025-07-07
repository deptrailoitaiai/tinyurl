#!/bin/bash
# Script to run schema on MySQL master

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Function to print colored output
print_status() {
    local status=$1
    local message=$2
    if [ "$status" = "SUCCESS" ]; then
        echo -e "${GREEN}✓${NC} $message"
    elif [ "$status" = "ERROR" ]; then
        echo -e "${RED}✗${NC} $message"
    elif [ "$status" = "INFO" ]; then
        echo -e "${YELLOW}ℹ${NC} $message"
    fi
}

echo "=== Running TinyURL Microservices Schema ==="
echo "Timestamp: $(date)"

# Kiểm tra xem schema.sql đã được mount vào container chưa
if [ ! -f "/scripts/schema.sql" ]; then
    print_status "ERROR" "Schema file not found at /scripts/schema.sql"
    exit 1
fi

# Thực thi schema.sql trên MySQL master
print_status "INFO" "Running schema.sql on MySQL master..."
print_status "INFO" "This will create the following databases and key tables:"
echo "  - url_shortener_service: urls, service_references"
echo "  - analytics_batch_service: url_daily_stats, service_references"
echo "  - analytics_realtime_service: click_events, devices, locations, service_references"
echo "  - user_management_service: users"
echo

# Thực thi schema
if mysql -h mysql-master -u root -proot123 < /scripts/schema.sql; then
    print_status "SUCCESS" "Schema executed successfully"
else
    print_status "ERROR" "Failed to execute schema"
    exit 1
fi

# Xác minh các database đã được tạo
print_status "INFO" "Verifying schema creation..."
print_status "INFO" "Checking databases..."
databases=$(mysql -h mysql-master -u root -proot123 -e "SHOW DATABASES;" | grep -E "(url_shortener_service|analytics_batch_service|analytics_realtime_service|user_management_service)")

if [ -z "$databases" ]; then
    print_status "ERROR" "No microservice databases found"
    exit 1
fi

print_status "SUCCESS" "Databases created successfully:"
echo "$databases"

# Kiểm tra sự sao chép sang slave
print_status "INFO" "Verifying database replication to slave..."

# Give replication more time to propagate
print_status "INFO" "Waiting for replication to complete (10 seconds)..."
sleep 10

# Get status of replication
replication_status=$(mysql -h mysql-slave -u root -proot123 -e "SHOW SLAVE STATUS\G" 2>/dev/null || mysql -h mysql-slave -u root -proot123 -e "SHOW REPLICA STATUS\G" 2>/dev/null)
echo "Replication status excerpt:"
echo "$replication_status" | grep -E "(Running|Error|Behind)"

# Check databases on slave
slave_databases=$(mysql -h mysql-slave -u root -proot123 -e "SHOW DATABASES;" | grep -E "(url_shortener_service|analytics_batch_service|analytics_realtime_service|user_management_service)")
echo "Slave databases: $slave_databases"

if [ -z "$slave_databases" ]; then
    print_status "WARNING" "Databases not replicated to slave yet. This is a warning only, continuing with setup."
    
    # Try to reset replication if needed
    print_status "INFO" "Attempting to restart replication..."
    mysql -h mysql-slave -u root -proot123 -e "STOP SLAVE; START SLAVE;" 2>/dev/null || mysql -h mysql-slave -u root -proot123 -e "STOP REPLICA; START REPLICA;" 2>/dev/null
else
    print_status "SUCCESS" "Databases replicated to slave successfully"
fi

# Xác minh các bảng trong từng database
print_status "INFO" "Checking tables in url_shortener_service..."
url_tables=$(mysql -h mysql-master -u root -proot123 -e "USE url_shortener_service; SHOW TABLES;")
echo "$url_tables"

print_status "INFO" "Checking tables in analytics_batch_service..."
batch_tables=$(mysql -h mysql-master -u root -proot123 -e "USE analytics_batch_service; SHOW TABLES;")
echo "$batch_tables"

print_status "INFO" "Checking tables in analytics_realtime_service..."
realtime_tables=$(mysql -h mysql-master -u root -proot123 -e "USE analytics_realtime_service; SHOW TABLES;")
echo "$realtime_tables"

print_status "INFO" "Checking tables in user_management_service..."
user_tables=$(mysql -h mysql-master -u root -proot123 -e "USE user_management_service; SHOW TABLES;")
echo "$user_tables"

# Thực hiện xác minh chi tiết cho cơ sở dữ liệu analytics_realtime_service
print_status "INFO" "Performing detailed validation of analytics_realtime_service tables..."
realtime_tables_count=$(mysql -h mysql-master -u root -proot123 -e "
    USE analytics_realtime_service; 
    SELECT COUNT(*) FROM information_schema.tables 
    WHERE table_schema = 'analytics_realtime_service' 
    AND table_name IN ('click_events', 'devices', 'locations', 'service_references');" | grep -v "COUNT")

if [ "$realtime_tables_count" -eq "4" ]; then
    print_status "SUCCESS" "All 4 required tables found in analytics_realtime_service"
    
    # Kiểm tra các ràng buộc khóa ngoại
    fk_check=$(mysql -h mysql-master -u root -proot123 -e "
        USE analytics_realtime_service;
        SELECT COUNT(*) FROM information_schema.table_constraints
        WHERE constraint_schema = 'analytics_realtime_service'
        AND constraint_type = 'FOREIGN KEY';" | grep -v "COUNT")
    
    if [ "$fk_check" -ge "2" ]; then
        print_status "SUCCESS" "Foreign key constraints verified in click_events table"
    else
        print_status "WARNING" "Some foreign key constraints may be missing"
    fi
else
    print_status "ERROR" "Missing tables in analytics_realtime_service. Found $realtime_tables_count of 4 required tables"
fi

# Kiểm tra xem các bảng có đúng không
if echo "$url_tables" | grep -q "urls" && echo "$url_tables" | grep -q "service_references" && \
   echo "$batch_tables" | grep -q "url_daily_stats" && \
   echo "$realtime_tables" | grep -q "click_events" && \
   echo "$realtime_tables" | grep -q "locations" && \
   echo "$realtime_tables" | grep -q "devices" && \
   echo "$user_tables" | grep -q "users"; then
    print_status "SUCCESS" "All required tables have been created correctly"
else
    print_status "ERROR" "Some required tables may be missing"
fi

print_status "SUCCESS" "Schema Setup Complete"
