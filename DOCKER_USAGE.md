# Docker Compose Usage Guide

## Lần đầu khởi động (tạo mới containers)

```bash
# Khởi động với init-runner để setup database
docker compose --profile init up -d

# Kiểm tra logs của init-runner
docker logs init-runner

# Sau khi init-runner hoàn thành, có thể tắt nó
docker compose stop init-runner
```

## Khởi động thông thường (containers đã tồn tại)

```bash
# Chỉ khởi động các services chính, không bao gồm init-runner
docker compose up -d

# Hoặc khởi động specific services
docker compose up -d mysql-master mysql-slave redis kafka-1 kafka-2 kafka-3
```

## Khi cần reset hoàn toàn

```bash
# Xóa tất cả containers và volumes
docker compose down -v

# Khởi động lại từ đầu với init
docker compose --profile init up -d
```

## Kiểm tra trạng thái

```bash
# Kiểm tra health của tất cả services
docker compose ps

# Kiểm tra replication status
docker exec mysql-slave mysql -u root -proot123 -e "SHOW REPLICA STATUS\G"

# Kiểm tra databases đã được tạo
docker exec mysql-master mysql -u root -proot123 -e "SHOW DATABASES;"
```

## Vấn đề thường gặp

1. **Init-runner chạy lại không cần thiết:**
   - Sử dụng profile `init` để kiểm soát khi nào init-runner chạy
   - Init-runner đã được cải thiện để kiểm tra trạng thái trước khi thực hiện

2. **Replication bị reset:**
   - Nếu gặp vấn đề replication, chạy lại với profile init:
   ```bash
   docker compose --profile init up init-runner
   ```

3. **Data bị mất:**
   - Luôn backup data trước khi chạy `docker compose down -v`
   - Sử dụng `docker compose stop` thay vì `down` để giữ data
