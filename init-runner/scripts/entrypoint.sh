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

# Chạy script thiết lập replication
print_status "INFO" "Running replication setup..."
bash "$SETUP_REPLICATION_SCRIPT"

# Chạy script khởi tạo schema
print_status "INFO" "Running schema setup..."
bash "$SCHEMA_SCRIPT"

print_status "SUCCESS" "Initialization completed successfully at: $(date)"

# Giữ container chạy để có thể check logs
print_status "INFO" "Initialization container will remain running for log inspection"
print_status "INFO" "Use 'docker logs init-runner' to view the complete logs"
tail -f /dev/null
