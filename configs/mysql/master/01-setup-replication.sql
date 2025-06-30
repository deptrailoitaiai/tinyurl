-- [important] Tạo user cho replication
CREATE USER IF NOT EXISTS 'repl'@'%' IDENTIFIED WITH mysql_native_password BY 'repl123';

-- [important] Cấp quyền REPLICATION SLAVE cho user repl
GRANT REPLICATION SLAVE ON *.* TO 'repl'@'%';
GRANT REPLICATION CLIENT ON *.* TO 'repl'@'%'; -- [optional] Additional replication client privileges

-- [important] Áp dụng thay đổi quyền
FLUSH PRIVILEGES;

-- [important] Reset master để có binary log position sạch
RESET MASTER;

-- [optional] Hiển thị master status để debug
SHOW MASTER STATUS;

-- [optional] Hiển thị user đã tạo để verify
SELECT user, host FROM mysql.user WHERE user = 'repl';

-- [info] No databases or tables are created during replication setup
-- [info] All databases and tables will be created later and automatically replicated to slave