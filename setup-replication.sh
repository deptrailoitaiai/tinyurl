#!/bin/bash
# [important] Manual MySQL Replication Setup Script
# Run this script after containers are up and healthy

echo "=== MySQL Master-Slave Replication Setup ==="
echo

# [important] Step 1: Check if containers are running
echo "1. Checking container status..."
docker ps --filter "name=mysql" --format "table {{.Names}}\t{{.Status}}"
echo

# [important] Step 2: Setup replication on slave
echo "2. Setting up replication on slave..."
docker exec -it mysql-slave mysql -uroot -proot123 -e "
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

echo "3. Checking replication status..."
docker exec -it mysql-slave mysql -uroot -proot123 -e "SHOW SLAVE STATUS\G" | grep -E "(Slave_IO_Running|Slave_SQL_Running|Last_Errno|Seconds_Behind_Master)"

echo
echo "4. Testing replication with dynamic database creation..."
echo "Creating test database and table on master..."
docker exec -it mysql-master mysql -uroot -proot123 -e "
CREATE DATABASE test_replication_db;
USE test_replication_db;
CREATE TABLE test_table (
    id INT AUTO_INCREMENT PRIMARY KEY,
    message VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
INSERT INTO test_table (message) VALUES ('Auto setup test - $(date)');
SELECT COUNT(*) as master_count FROM test_table;
"

echo "Checking if database and data replicated to slave..."
docker exec -it mysql-slave mysql -uroot -proot123 -e "
SHOW DATABASES LIKE 'test_replication_db';
USE test_replication_db;
SELECT COUNT(*) as slave_count FROM test_table;
SELECT message FROM test_table ORDER BY id DESC LIMIT 1;
"

echo
echo "=== Setup Complete ==="
echo "If Slave_IO_Running=Yes and Slave_SQL_Running=Yes, replication is working!"
