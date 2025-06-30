#!/bin/bash
# Simple script to run schema.sql on MySQL master
# This will create all microservices databases and tables

set -e

echo "=== Running TinyURL Microservices Schema ==="
echo "Timestamp: $(date)"
echo

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

# Check if MySQL master container is running
check_mysql_master() {
    print_status "INFO" "Checking MySQL master container..."
    
    if ! docker ps --filter "name=mysql-master" --filter "status=running" | grep -q mysql-master; then
        print_status "ERROR" "MySQL master container is not running"
        echo "Please start MySQL containers first with: docker-compose up -d"
        exit 1
    fi
    
    print_status "SUCCESS" "MySQL master container is running"
    
    # Wait for MySQL to be ready
    print_status "INFO" "Waiting for MySQL to be ready..."
    local retries=0
    while [ $retries -lt 30 ]; do
        if docker exec mysql-master mysql -uroot -proot123 -e "SELECT 1" >/dev/null 2>&1; then
            print_status "SUCCESS" "MySQL master is ready"
            break
        fi
        ((retries++))
        sleep 2
    done
    
    if [ $retries -eq 30 ]; then
        print_status "ERROR" "MySQL master failed to become ready"
        exit 1
    fi
}

# Run the schema
run_schema() {
    local SCHEMA_FILE="./schema.sql"
    
    if [ ! -f "$SCHEMA_FILE" ]; then
        print_status "ERROR" "Schema file not found: $SCHEMA_FILE"
        echo "Please make sure schema.sql exists in the current directory"
        exit 1
    fi
    
    print_status "INFO" "Running schema.sql on MySQL master..."
    print_status "INFO" "This will create the following databases:"
    echo "  - url_service (URL shortening)"
    echo "  - ana_batch_service (Analytics batch processing)"
    echo "  - ana_rt_service (Analytics real-time)"
    echo "  - user_mgmt_service (User management)"
    echo
    
    # Execute the schema
    if docker exec -i mysql-master mysql -uroot -proot123 < "$SCHEMA_FILE"; then
        print_status "SUCCESS" "Schema executed successfully"
    else
        print_status "ERROR" "Failed to execute schema"
        exit 1
    fi
}

# Verify the schema was created
verify_schema() {
    print_status "INFO" "Verifying schema creation..."
    
    # Check databases
    print_status "INFO" "Checking databases..."
    local databases=$(docker exec mysql-master mysql -uroot -proot123 -e "SHOW DATABASES;" | grep -E "(url_service|ana_batch_service|ana_rt_service|user_mgmt_service)")
    
    if [ -z "$databases" ]; then
        print_status "ERROR" "No microservice databases found"
        exit 1
    fi
    
    echo "Databases created:"
    echo "$databases"
    
    # Check tables in each database
    print_status "INFO" "Checking tables..."
    
    echo
    echo "=== URL SERVICE ==="
    docker exec mysql-master mysql -uroot -proot123 -e "USE url_service; SHOW TABLES;"
    
    echo
    echo "=== ANALYTICS BATCH SERVICE ==="
    docker exec mysql-master mysql -uroot -proot123 -e "USE ana_batch_service; SHOW TABLES;"
    
    echo
    echo "=== ANALYTICS REALTIME SERVICE ==="
    docker exec mysql-master mysql -uroot -proot123 -e "USE ana_rt_service; SHOW TABLES;"
    
    echo
    echo "=== USER MANAGEMENT SERVICE ==="
    docker exec mysql-master mysql -uroot -proot123 -e "USE user_mgmt_service; SHOW TABLES;"
    
    print_status "SUCCESS" "Schema verification completed"
}

# Check replication (if slave exists)
check_replication() {
    print_status "INFO" "Checking if replication is configured..."
    
    if docker ps --filter "name=mysql-slave" --filter "status=running" | grep -q mysql-slave; then
        print_status "INFO" "MySQL slave detected, checking replication..."
        sleep 3  # Give time for replication
        
        # Check if databases are replicated
        local slave_databases=$(docker exec mysql-slave mysql -uroot -proot123 -e "SHOW DATABASES;" | grep -E "(url_service|ana_batch_service|ana_rt_service|user_mgmt_service)" | wc -l)
        
        if [ "$slave_databases" -eq 4 ]; then
            print_status "SUCCESS" "All databases replicated to slave"
        else
            print_status "ERROR" "Not all databases replicated to slave (found $slave_databases/4)"
        fi
        
        # Show replication status
        echo
        echo "Replication status:"
        docker exec mysql-slave mysql -uroot -proot123 -e "SHOW SLAVE STATUS\G" | grep -E "(Slave_IO_Running|Slave_SQL_Running|Seconds_Behind_Master)"
    else
        print_status "INFO" "No MySQL slave detected, skipping replication check"
    fi
}

# Main execution
main() {
    check_mysql_master
    run_schema
    verify_schema
    check_replication
    
    echo
    echo "=== Schema Setup Complete ==="
    print_status "SUCCESS" "TinyURL microservices databases are ready!"
    echo
    echo "You can now:"
    echo "1. Start your Spring Boot backend applications"
    echo "2. Configure each service to connect to its respective database"
    echo "3. Run your applications using the created schema"
    echo
    echo "Database connection examples:"
    echo "- URL Service: jdbc:mysql://localhost:3306/url_service"
    echo "- Analytics Batch: jdbc:mysql://localhost:3306/ana_batch_service"
    echo "- Analytics RT: jdbc:mysql://localhost:3306/ana_rt_service"
    echo "- User Management: jdbc:mysql://localhost:3306/user_mgmt_service"
}

# Execute main function
main
