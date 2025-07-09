# Service References Table Documentation (Minimalist Design)

## 📋 Tổng quan

Bảng `service_references` được thiết kế tối giản để lưu trữ các mối quan hệ cross-service với chỉ 5 cột cần thiết nhất. Đây là giải pháp đơn giản, hiệu quả cho việc maintain relationships giữa các microservices.

## 🔗 Cấu trúc bảng tối giản

```sql
CREATE TABLE `service_references` (
    `id` BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    `local_id` BIGINT NOT NULL,                 -- ID của record trong bảng local
    `local_table` VARCHAR(100) NOT NULL,        -- Tên bảng trong service hiện tại
    `target_id` BIGINT NOT NULL,                -- ID của record được tham chiếu
    `target_table` VARCHAR(100) NOT NULL,       -- Tên bảng được tham chiếu
    UNIQUE KEY `service_ref_unique` (`local_table`, `local_id`, `target_table`, `target_id`)
);
```

### 5 cột cốt lõi:
- **`id`**: Primary key tự động tăng
- **`local_id`**: ID của record trong service hiện tại  
- **`local_table`**: Tên bảng trong service hiện tại
- **`target_id`**: ID của record được tham chiếu ở service khác
- **`target_table`**: Tên bảng được tham chiếu ở service khác

## 🎯 Các trường hợp sử dụng

### 1. URL Shortener Service → User Management Service

**Liên kết URLs với Users:**
```sql
-- Tạo URL mới
INSERT INTO urls (original_url, short_code, user_id) VALUES ('https://example.com', 'abc123', 0);

-- Tạo reference đến user
INSERT INTO service_references (local_id, local_table, target_id, target_table) 
VALUES (LAST_INSERT_ID(), 'urls', 1001, 'users');

-- Query URLs của một user (từ phía User service)
SELECT u.* FROM urls u 
JOIN service_references sr ON u.id = sr.local_id 
WHERE sr.local_table = 'urls' 
  AND sr.target_table = 'users'
  AND sr.target_id = 1001;
```

### 2. Analytics Batch Service → URL Shortener Service

**Liên kết Daily Stats với URLs:**
```sql
-- Tạo daily stats
INSERT INTO url_daily_stats (url_id, date, click_count) VALUES (0, '2025-07-04', 100);

-- Tạo reference đến URL
INSERT INTO service_references (local_id, local_table, target_id, target_table) 
VALUES (LAST_INSERT_ID(), 'url_daily_stats', 1, 'urls');

-- Query stats của một URL (từ phía URL service)
SELECT s.* FROM url_daily_stats s
JOIN service_references sr ON s.id = sr.local_id
WHERE sr.local_table = 'url_daily_stats'
  AND sr.target_table = 'urls'
  AND sr.target_id = 1;
```

### 3. Analytics Realtime Service → URL Shortener Service

**Liên kết Click Events với URLs:**
```sql
-- Ghi nhận click event
INSERT INTO click_events (url_id, short_code, ip_address, clicked_at) 
VALUES (0, 'abc123', '192.168.1.1', NOW());

-- Tạo reference đến URL
INSERT INTO service_references (local_id, local_table, target_id, target_table) 
VALUES (LAST_INSERT_ID(), 'click_events', 1, 'urls');

-- Query click events của một URL
SELECT c.* FROM click_events c
JOIN service_references sr ON c.id = sr.local_id
WHERE sr.local_table = 'click_events'
  AND sr.target_table = 'urls'
  AND sr.target_id = 1;
```

## 🎯 Lợi ích của thiết kế tối giản

### 1. **Simplicity** 🎯
- Chỉ 5 cột cần thiết - dễ hiểu, dễ maintain
- Không phức tạp với metadata không cần thiết
- Schema đơn giản = ít bugs, ít confusion

### 2. **Performance** ⚡
- Bảng nhỏ gọn = insert/select nhanh hơn
- Ít cột = ít disk I/O
- Index đơn giản = query optimization tốt hơn

### 3. **Flexibility** 💪
- Có thể represent bất kỳ relationship nào
- Không bị giới hạn bởi relationship types
- Dễ dàng extend khi cần

### 4. **Scalability** 🚀
- Cấu trúc đơn giản scale tốt
- Ít memory footprint
- Suitable cho high-volume data

## 🔧 Best Practices

### 1. **Naming Convention**
```sql
-- Consistent table naming
'users'          -- User Management Service
'urls'           -- URL Shortener Service  
'click_events'   -- Analytics Realtime Service
'url_daily_stats' -- Analytics Batch Service
```

### 2. **Query Patterns**
```sql
-- Tìm tất cả local records tham chiếu đến target record
SELECT sr.local_id, sr.local_table 
FROM service_references sr
WHERE sr.target_table = 'users' AND sr.target_id = 1001;

-- Tìm tất cả target records được tham chiếu bởi local record  
SELECT sr.target_id, sr.target_table
FROM service_references sr  
WHERE sr.local_table = 'urls' AND sr.local_id = 1;

-- Kiểm tra relationship có tồn tại không
SELECT EXISTS(
  SELECT 1 FROM service_references 
  WHERE local_table = 'urls' AND local_id = 1 
    AND target_table = 'users' AND target_id = 1001
);
```

### 3. **Cleanup Operations**
```sql
-- Cleanup orphaned references khi xóa local record
DELETE FROM service_references 
WHERE local_table = 'urls' 
  AND local_id NOT IN (SELECT id FROM urls);

-- Cleanup references khi xóa target record (cần coordination với target service)
DELETE FROM service_references
WHERE target_table = 'users' 
  AND target_id = 1001; -- Sau khi confirm user đã bị xóa
```

### 4. **Batch Operations**
```sql
-- Batch insert cho performance
INSERT INTO service_references (local_id, local_table, target_id, target_table) VALUES
(1, 'urls', 1001, 'users'),
(2, 'urls', 1001, 'users'),  
(3, 'urls', 1002, 'users');

-- Batch cleanup
DELETE FROM service_references 
WHERE (local_table, local_id) IN (
  SELECT 'urls', id FROM urls WHERE status = 'deleted'
);
```

## 🚨 Lưu ý quan trọng

### 1. **Data Consistency**
- Implement application-level consistency checks
- Use event-driven patterns để maintain references
- Monitor orphaned references regularly

### 2. **No Cache = More Service Calls** 
- Thiết kế tối giản có nghĩa là sẽ cần more cross-service calls
- Implement proper caching strategies ở application layer
- Consider response time trade-offs

### 3. **Index Strategy**
```sql
-- Essential indexes
CREATE INDEX `idx_local` ON service_references (local_table, local_id);
CREATE INDEX `idx_target` ON service_references (target_table, target_id);

-- Query-specific indexes nếu cần
CREATE INDEX `idx_local_target` ON service_references (local_table, target_table);
```

### 4. **Monitoring**
```sql
-- Monitor table size
SELECT COUNT(*) as total_references FROM service_references;

-- Monitor references by table
SELECT local_table, target_table, COUNT(*) as ref_count
FROM service_references 
GROUP BY local_table, target_table;

-- Find potential orphans
SELECT local_table, COUNT(*) as potential_orphans
FROM service_references sr
LEFT JOIN urls u ON sr.local_table = 'urls' AND sr.local_id = u.id
WHERE sr.local_table = 'urls' AND u.id IS NULL
GROUP BY local_table;
```

## 💡 Kết luận

Thiết kế tối giản này phù hợp khi:
- ✅ Bạn muốn simplicity over features
- ✅ Performance là priority cao  
- ✅ Team có khả năng handle cross-service coordination
- ✅ Application layer có thể handle caching logic

Không phù hợp khi:
- ❌ Cần rich metadata về relationships
- ❌ Muốn avoid cross-service calls
- ❌ Cần audit trail cho reference changes
- ❌ Team không có experience với distributed systems
