# Các lệnh Makefile để quản lý môi trường Docker của TinyURL

# Default target
.DEFAULT_GOAL := help
.PHONY: help clear-complete clear-unused clear-system-all clear-project-only clear-container-all clear-volume-all clear-image-all clear-all restart-clean start logs init-logs

# === STARTUP COMMANDS ===
start:
	@echo "Starting all services with Docker Compose..."
	docker-compose up -d
	@echo "All services started, including automated database initialization."
	@echo "Use 'make init-logs' to monitor the initialization process."

init-logs:
	@echo "Showing logs from the init-runner container..."
	docker logs -f init-runner

logs:
	@echo "Showing logs from all containers..."
	docker-compose logs -f

# === CLEANUP COMMANDS ===

# Clear tất cả volumes
clear-volume-all:
	@if [ -n "$$(docker volume ls -q)" ]; then \
		docker volume rm $$(docker volume ls -q); \
	else \
		echo "No Docker volumes to remove."; \
	fi

# Clear tất cả containers
clear-container-all:
	@if [ -n "$$(docker ps -aq)" ]; then \
		docker rm -f $$(docker ps -aq); \
	else \
		echo "No Docker containers to remove."; \
	fi

# Clear tất cả images
clear-image-all:
	@if [ -n "$$(docker images -q)" ]; then \
		docker rmi -f $$(docker images -q); \
	else \
		echo "No Docker images to remove."; \
	fi

# Clear containers và volumes tồn đọng
clear-unused:
	@echo "Cleaning up unused Docker resources..."
	docker container prune -f
	docker volume prune -f
	docker network prune -f
	@echo "Cleanup completed!"

# Clear toàn bộ hệ thống Docker (bao gồm images, containers, volumes, networks)
clear-system-all:
	@echo "Performing complete Docker system cleanup..."
	docker system prune -a -f --volumes
	@echo "Complete system cleanup finished!"

# Clear chỉ project hiện tại (containers và volumes của docker-compose)
clear-project-only:
	@echo "Stopping and removing project containers and volumes..."
	docker compose down -v --remove-orphans
	@echo "Project cleanup completed!"

# Clear project + tất cả volumes tồn đọng từ các lần chạy trước
clear-complete:
	@echo "Performing complete project cleanup..."
	@echo "Step 1: Stopping current project containers and volumes..."
	docker compose down -v --remove-orphans
	@echo "Step 2: Removing all remaining volumes..."
	@if [ -n "$$(docker volume ls -q)" ]; then \
		docker volume rm $$(docker volume ls -q); \
	else \
		echo "No additional volumes to remove."; \
	fi
	@echo "Step 3: Cleaning up unused containers and networks..."
	docker container prune -f
	docker network prune -f
	@echo "Complete project cleanup finished!"

# Clear tất cả (containers + volumes) - compatibility với lệnh cũ
clear-all: clear-container-all clear-volume-all

# Clear và khởi động lại project
restart-clean: clear-project-only
	@echo "Starting fresh containers..."
	docker compose up -d
	@echo "Project restarted with clean state!"

# === MYSQL COMMANDS ===

mysql-m:
	docker exec -it -e MYSQL_PWD=root123 mysql-master mysql -uroot -proot123

mysql-s:
	docker exec -it -e MYSQL_PWD=root123 mysql-slave mysql -uroot -proot123

# Kiểm tra trạng thái Master
mysql-master-status:
	docker exec -e MYSQL_PWD=root123 mysql-master mysql -uroot -e "SHOW MASTER STATUS;"

# Kiểm tra trạng thái Slave
mysql-slave-status:
	docker exec -e MYSQL_PWD=root123 mysql-slave mysql -uroot -e "SHOW SLAVE STATUS\G;"

# Chạy lệnh SQL trên Master
mysql-master-exec:
	@echo "Usage: make mysql-master-exec SQL='SELECT 1;'"
	@if [ -z "$(SQL)" ]; then echo "Please provide SQL parameter"; exit 1; fi
	docker exec -e MYSQL_PWD=root123 mysql-master mysql -uroot -e "$(SQL)"

# Chạy lệnh SQL trên Slave
mysql-slave-exec:
	@echo "Usage: make mysql-slave-exec SQL='SELECT 1;'"
	@if [ -z "$(SQL)" ]; then echo "Please provide SQL parameter"; exit 1; fi
	docker exec -e MYSQL_PWD=root123 mysql-slave mysql -uroot -e "$(SQL)"

# Tạo database tinyurl trên Master
create-db:
	docker exec -e MYSQL_PWD=root123 mysql-master mysql -uroot -e "CREATE DATABASE IF NOT EXISTS tinyurl; USE tinyurl; SHOW TABLES;"

# Hiển thị logs của containers
logs-master:
	docker logs mysql-master

logs-slave:
	docker logs mysql-slave

# === REDIS COMMAND ===
redis-cli:
	docker exec -it redis redis-cli