-- [info] Pure replication setup - No databases or tables are pre-created
-- [info] All databases and tables will be automatically replicated from master
-- [important] Manual replication setup will be done after container startup

-- [optional] Show current slave status (will be empty initially)
-- SHOW SLAVE STATUS\G

-- [important] Manual setup commands to run after containers are up:
-- 1. STOP SLAVE;
-- 2. RESET SLAVE ALL;
-- 3. CHANGE MASTER TO MASTER_HOST='mysql-master', MASTER_USER='repl', MASTER_PASSWORD='repl123', MASTER_AUTO_POSITION=1, GET_MASTER_PUBLIC_KEY=1;
-- 4. START SLAVE;

-- [info] After replication is established, any database/table created on master will be automatically replicated here