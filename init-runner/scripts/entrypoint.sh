#!/bin/bash
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

echo "=== TinyURL Initialization Runner ==="
print_status "INFO" "Starting initialization runner at: $(date)"

# Đường dẫn tới các script
SCRIPT_DIR="$(dirname "$0")"
SETUP_REPLICATION_SCRIPT="$SCRIPT_DIR/setup-replication.sh"
SCHEMA_SCRIPT="$SCRIPT_DIR/run-schema.sh"

# Kiểm tra xem các script có tồn tại không
if [ ! -f "$SETUP_REPLICATION_SCRIPT" ] || [ ! -f "$SCHEMA_SCRIPT" ]; then
    print_status "ERROR" "Required scripts not found."
    exit 1
fi

# Kiểm tra và đợi MySQL master khởi động và healthy
print_status "INFO" "Waiting for MySQL master to be ready..."
until mysql -h mysql-master -P 3306 -u root -proot123 -e "SELECT 1" &> /dev/null
do
    print_status "INFO" "MySQL master is not ready yet... waiting 5 seconds"
    sleep 5
done
print_status "SUCCESS" "MySQL master is ready!"

# Kiểm tra và đợi MySQL slave khởi động và healthy
print_status "INFO" "Waiting for MySQL slave to be ready..."
until mysql -h mysql-slave -P 3306 -u root -proot123 -e "SELECT 1" &> /dev/null
do
    print_status "INFO" "MySQL slave is not ready yet... waiting 5 seconds"
    sleep 5
done
print_status "SUCCESS" "MySQL slave is ready!"

# Kiểm tra và đợi Redis khởi động và healthy
print_status "INFO" "Waiting for Redis to be ready..."
until redis-cli -h redis ping &> /dev/null
do
    print_status "INFO" "Redis is not ready yet... waiting 5 seconds"
    sleep 5
done
print_status "SUCCESS" "Redis is ready!"

# Hiển thị thông tin về schema sẽ được khởi tạo
print_status "INFO" "Schema initialization will create the following structure:"
echo "- user_management_service: users table"
echo "- url_shortener_service: urls, service_references tables"
echo "- analytics_batch_service: url_daily_stats, service_references tables"
echo "- analytics_realtime_service: click_events, devices, locations, service_references tables"
echo

# Kiểm tra xem replication đã được setup chưa
print_status "INFO" "Checking if replication is already configured..."
SLAVE_STATUS=$(mysql -h mysql-slave -u root -proot123 -e "SHOW REPLICA STATUS\G" 2>/dev/null || mysql -h mysql-slave -u root -proot123 -e "SHOW SLAVE STATUS\G" 2>/dev/null || echo "")

if echo "$SLAVE_STATUS" | grep -q "Source_Host: mysql-master\|Master_Host: mysql-master"; then
    print_status "INFO" "Replication is already configured, skipping replication setup"
else
    print_status "INFO" "Running replication setup..."
    bash "$SETUP_REPLICATION_SCRIPT"
fi

# Kiểm tra xem schema đã được tạo chưa
print_status "INFO" "Checking if schema is already initialized..."
SCHEMA_EXISTS=$(mysql -h mysql-master -u root -proot123 -e "SHOW DATABASES LIKE 'url_shortener_service';" | grep -c "url_shortener_service" || echo "0")

if [ "$SCHEMA_EXISTS" -gt 0 ]; then
    print_status "INFO" "Schema already exists, skipping schema creation"
else
    print_status "INFO" "Running schema setup..."
    bash "$SCHEMA_SCRIPT"
fi

print_status "SUCCESS" "Initialization completed successfully at: $(date)"

# Tạo status file để đánh dấu đã hoàn thành
if [ -n "$INIT_STATUS_FILE" ]; then
    echo "$(date): Initialization completed successfully" > "$INIT_STATUS_FILE"
    print_status "INFO" "Status file created at: $INIT_STATUS_FILE"
fi

# Giữ container chạy để có thể check logs
print_status "INFO" "Initialization container will remain running for log inspection"
print_status "INFO" "Use 'docker logs init-runner' to view the complete logs"
print_status "INFO" "Container will exit in 60 seconds to save resources"

# Thay vì chạy mãi, thoát sau 60 giây để tiết kiệm tài nguyên
sleep 60
