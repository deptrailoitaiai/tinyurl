#!/bin/bash

# =================================================================
# Script để tạo indexes trên slave database
# Chỉ chạy script này trên slave database để tối ưu hóa read performance
# KHÔNG chạy trên primary database
# =================================================================

# Kiểm tra xem có tham số host không
if [ $# -eq 0 ]; then
    echo "Usage: $0 <mysql_host> [mysql_port] [mysql_user] [mysql_password]"
    echo "Example: $0 localhost 3307 root password"
    exit 1
fi

HOST=$1
PORT=${2:-3306}
USER=${3:-root}
PASSWORD=${4:-""}

# Tạo connection string
if [ -z "$PASSWORD" ]; then
    MYSQL_CMD="mysql -h $HOST -P $PORT -u $USER"
else
    MYSQL_CMD="mysql -h $HOST -P $PORT -u $USER -p$PASSWORD"
fi

echo "==================================================================="
echo "Tạo indexes trên slave database: $HOST:$PORT"
echo "==================================================================="

# Kiểm tra kết nối
echo "Kiểm tra kết nối database..."
if ! $MYSQL_CMD -e "SELECT 1;" > /dev/null 2>&1; then
    echo "ERROR: Không thể kết nối đến database $HOST:$PORT"
    exit 1
fi

echo "Kết nối thành công!"

# Chạy indexes.sql
echo "Đang tạo indexes..."
if $MYSQL_CMD < indexes.sql; then
    echo "SUCCESS: Tạo indexes thành công!"
else
    echo "ERROR: Có lỗi xảy ra khi tạo indexes"
    exit 1
fi

echo "==================================================================="
echo "Hoàn thành tạo indexes trên slave database"
echo "==================================================================="
