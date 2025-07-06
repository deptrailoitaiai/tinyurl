# TinyURL Project - MySQL Master-Slave Replication Setup

## 🚀 Quick Start Guide

### Prerequisites
- Docker và Docker Compose đã được cài đặt
- Ports 3307, 3308, 9091, 9092, 9093 không bị sử dụng

### 📋 Step-by-Step Instructions

#### 1. **Start MySQL Containers**
```bash
# Di chuyển vào thư mục dự án
cd /home/tienminh/dotienminh/whereIBeatTheWorld/java/tinyUrl

# Start MySQL Master và Slave containers
docker compose up -d mysql-master mysql-slave
```

#### 2. **Kiểm tra Container Status**
```bash
# Kiểm tra containers đã chạy và healthy
docker compose ps

# Hoặc kiểm tra chi tiết
docker ps --filter "name=mysql" --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}"
```

#### 3. **Setup Pure Replication (Không tạo database)**
```bash
# Chạy script setup replication
./configs/mysql/setup-replication.sh
```

**Kết quả mong đợi:**
- `Slave_IO_Running: Yes`
- `Slave_SQL_Running: Yes`
- Test database được tạo và replica thành công

#### 4. **Tạo Main Application Schema (Tự động replica)**
```bash
# Chạy script tạo schema chính
./run-schema.sh
```

**Schema sẽ được tạo:**
- Database: `tinyurl`
- Tables: `users`, `urls`, `locations`, `devices`, `click_events`, `url_daily_stats`, `url_tags`
- Sample data trong `locations` và `devices`
- Triggers và stored procedures

#### 5. **Tạo Indexes trên Slave Database (Tối ưu Read Performance)**
```bash
# Chạy script tạo indexes trên slave database
./run-indexes.sh localhost 3308 root

# Hoặc với password
./run-indexes.sh localhost 3308 root your_password
```

**⚠️ LƯU Ý QUAN TRỌNG:**
- File `schema.sql`: Chỉ chứa database, tables, columns - dành cho PRIMARY database
- File `indexes.sql`: Chứa tất cả indexes - chỉ chạy trên SLAVE database
- Việc tách indexes giúp tối ưu write performance trên primary và read performance trên slave

**Lợi ích của việc tách indexes:**
- Primary database: Write nhanh hơn do không có indexes (trừ UNIQUE constraints cần thiết)
- Slave database: Read nhanh hơn với đầy đủ indexes
- Giảm lag time trong replication

### 🔧 Manual Commands (Tùy chọn)

#### **Kết nối vào MySQL:**
```bash
# Kết nối MySQL Master (port 3307)
docker exec -it mysql-master mysql -uroot -proot123

# Kết nối MySQL Slave (port 3308)  
docker exec -it mysql-slave mysql -uroot -proot123
```

#### **Kiểm tra Replication Status:**
```bash
# Kiểm tra Master status
docker exec mysql-master mysql -uroot -proot123 -e "SHOW MASTER STATUS;"

# Kiểm tra Slave status
docker exec mysql-slave mysql -uroot -proot123 -e "SHOW SLAVE STATUS\G;"
```

#### **Test Replication Manual:**
```bash
# Tạo database test trên Master
docker exec mysql-master mysql -uroot -proot123 -e "
CREATE DATABASE test_db;
USE test_db;
CREATE TABLE test_table (id INT AUTO_INCREMENT PRIMARY KEY, name VARCHAR(50));
INSERT INTO test_table (name) VALUES ('Hello from Master');
SELECT * FROM test_table;
"

# Kiểm tra trên Slave (sẽ có dữ liệu giống hệt)
docker exec mysql-slave mysql -uroot -proot123 -e "
USE test_db;
SELECT * FROM test_table;
"
```

### 🧹 Cleanup Commands

#### **Dọn dẹp hoàn toàn:**
```bash
# Stop và remove containers + volumes
make clear-complete

# Hoặc manual
docker compose down -v --remove-orphans
docker volume prune -f
```

#### **Restart clean:**
```bash
# Dọn dẹp và khởi động lại
make restart-clean
```

### 🔍 Troubleshooting

#### **Containers không start:**
```bash
# Kiểm tra logs
docker logs mysql-master
docker logs mysql-slave

# Kiểm tra ports
netstat -tulpn | grep -E "(3307|3308)"
```

#### **Replication không hoạt động:**
```bash
# Reset replication trên slave
docker exec mysql-slave mysql -uroot -proot123 -e "
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

# Kiểm tra lại status
docker exec mysql-slave mysql -uroot -proot123 -e "SHOW SLAVE STATUS\G;"
```

### 📊 Architecture

```
┌─────────────────┐    Replication       ┌─────────────────┐
│   MySQL Master  │ ═══════════════════► │   MySQL Slave   │
│   (port 3307)   │    All databases     │   (port 3308)   │
│   Read + Write  │    Tables + Data     │   Read Only     │
└─────────────────┘                      └─────────────────┘
```

### 🎯 Important Notes

- **Master**: Chỉ ghi dữ liệu vào đây
- **Slave**: Chỉ đọc dữ liệu từ đây (read-only)
- **Auto-replication**: Mọi thay đổi trên Master tự động sync sang Slave
- **GTID**: Sử dụng Global Transaction ID để đảm bảo consistency
- **Real-time**: Replication gần như real-time (vài milliseconds delay)

### 🔗 Access Information

- **MySQL Master**: `localhost:3307` (user: `root`, password: `root123`)
- **MySQL Slave**: `localhost:3308` (user: `root`, password: `root123`)
- **Replication User**: `repl` / `repl123` (chỉ dùng cho replication)

---

## 🎉 Success Criteria

Sau khi hoàn thành setup, bạn sẽ có:
1. ✅ 2 MySQL instances chạy healthy
2. ✅ Replication hoạt động (`Slave_IO_Running=Yes`, `Slave_SQL_Running=Yes`)
3. ✅ Database `tinyurl` với đầy đủ tables trên cả Master và Slave
4. ✅ Mọi thay đổi trên Master tự động sync sang Slave