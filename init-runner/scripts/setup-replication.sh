#!/bin/bash
# MySQL Master-Slave Replication Setup Script

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

echo "=== MySQL Master-Slave Replication Setup ==="
echo "Timestamp: $(date)"

# Thiết lập replication trên slave
print_status "INFO" "Setting up replication on slave..."
mysql_version=$(mysql -h mysql-slave -u root -proot123 -e "SELECT VERSION();" | grep -v "VERSION")
echo "MySQL version: $mysql_version"

# Try REPLICA syntax first (for MySQL 8.0.22+), fall back to SLAVE syntax if needed
if echo "$mysql_version" | grep -q "8.0"; then
    print_status "INFO" "Using MySQL 8.0+ compatible commands..."
    # Try with REPLICA syntax first (MySQL 8.0.22+)
    mysql -h mysql-slave -u root -proot123 -e "
    STOP REPLICA;
    RESET REPLICA ALL;
    CHANGE REPLICATION SOURCE TO
        SOURCE_HOST='mysql-master',
        SOURCE_PORT=3306,
        SOURCE_USER='repl',
        SOURCE_PASSWORD='repl123',
        SOURCE_AUTO_POSITION=1,
        GET_SOURCE_PUBLIC_KEY=1;
    START REPLICA;
    " 2>/dev/null || mysql -h mysql-slave -u root -proot123 -e "
    STOP SLAVE;
    RESET SLAVE ALL;
    CHANGE MASTER TO
        MASTER_HOST='mysql-master',
        MASTER_PORT=3306,
        MASTER_USER='repl',
        MASTER_PASSWORD='repl123',
        MASTER_AUTO_POSITION=1,
        GET_MASTER_PUBLIC_KEY=1;
    START SLAVE;
    "
else
    # Use old SLAVE syntax for MySQL < 8.0
    print_status "INFO" "Using traditional MySQL replication commands..."
    mysql -h mysql-slave -u root -proot123 -e "
    STOP SLAVE;
    RESET SLAVE ALL;
    CHANGE MASTER TO
        MASTER_HOST='mysql-master',
        MASTER_PORT=3306,
        MASTER_USER='repl',
        MASTER_PASSWORD='repl123',
        MASTER_AUTO_POSITION=1,
        GET_MASTER_PUBLIC_KEY=1;
    START SLAVE;
    "
fi

# Kiểm tra trạng thái replication
print_status "INFO" "Checking replication status..."
# Check for MySQL 8.0+ with REPLICA syntax or older MySQL with SLAVE syntax
mysql_version=$(mysql -h mysql-slave -u root -proot123 -e "SELECT VERSION();" | grep -v "VERSION")
echo "MySQL version: $mysql_version"

if echo "$mysql_version" | grep -q "8.0"; then
    # Use REPLICA for MySQL 8.0+
    replication_status=$(mysql -h mysql-slave -u root -proot123 -e "SHOW REPLICA STATUS\G" 2>/dev/null || mysql -h mysql-slave -u root -proot123 -e "SHOW SLAVE STATUS\G" 2>/dev/null | grep -E "(Replica_IO_Running|Replica_SQL_Running|Last_Errno|Seconds_Behind_Master|Slave_IO_Running|Slave_SQL_Running)")
else
    # Use SLAVE for older MySQL versions
    replication_status=$(mysql -h mysql-slave -u root -proot123 -e "SHOW SLAVE STATUS\G" | grep -E "(Slave_IO_Running|Slave_SQL_Running|Last_Errno|Seconds_Behind_Master)")
fi
echo "$replication_status"

if echo "$replication_status" | grep -q "Running: Yes"; then
    print_status "SUCCESS" "Replication is working correctly!"
else
    print_status "ERROR" "Replication setup has issues. Please check the status above."
fi

print_status "INFO" "Creating test database to verify replication..."
mysql -h mysql-master -u root -proot123 -e "
CREATE DATABASE IF NOT EXISTS test_replication_db;
USE test_replication_db;
CREATE TABLE IF NOT EXISTS test_table (
    id INT AUTO_INCREMENT PRIMARY KEY,
    message VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
INSERT INTO test_table (message) VALUES ('Auto setup test - $(date)');
SELECT COUNT(*) as master_count FROM test_table;
"

print_status "INFO" "Checking if database and data replicated to slave..."
sleep 10 # Đợi lâu hơn để đảm bảo replication hoàn tất

# Try to list databases first 
print_status "INFO" "Checking all databases on slave:"
all_databases=$(mysql -h mysql-slave -u root -proot123 -e "SHOW DATABASES;")
echo "$all_databases"

# Test if database exists first before trying to use it
db_exists=$(mysql -h mysql-slave -u root -proot123 -e "SHOW DATABASES LIKE 'test_replication_db';")
echo "Database check result: $db_exists"

if [ -z "$db_exists" ]; then
    print_status "ERROR" "Test database not found on slave. Replication may not be working properly."
    
    # Force a FLUSH TABLES WITH READ LOCK on master to ensure any pending changes are written
    print_status "INFO" "Trying to flush tables on master and reset slave..."
    mysql -h mysql-master -u root -proot123 -e "FLUSH TABLES WITH READ LOCK; UNLOCK TABLES;"
    
    # Get master status
    master_status=$(mysql -h mysql-master -u root -proot123 -e "SHOW MASTER STATUS\G")
    echo "Master status: $master_status"
    
    # Restart slave process
    mysql -h mysql-slave -u root -proot123 -e "STOP SLAVE; START SLAVE;"
    
    # Wait a bit more
    sleep 5
    
    # Check again
    db_exists=$(mysql -h mysql-slave -u root -proot123 -e "SHOW DATABASES LIKE 'test_replication_db';")
    if [ -z "$db_exists" ]; then
        print_status "ERROR" "Test database still not found on slave after retry."
        # Continue with schema anyway as it's a test database
    else
        print_status "SUCCESS" "Test database found on slave after retry!"
    fi
else
    # Try to check table data
    slave_count=$(mysql -h mysql-slave -u root -proot123 -e "USE test_replication_db; SELECT COUNT(*) as slave_count FROM test_table;" 2>/dev/null | grep -v "slave_count")
    
    if [ -z "$slave_count" ]; then
        print_status "WARNING" "Test database exists but test_table may be missing or empty."
    else
        print_status "SUCCESS" "Test database and table data replicated successfully to slave! Count: $slave_count"
    fi
fi

print_status "SUCCESS" "Replication Setup Complete"
