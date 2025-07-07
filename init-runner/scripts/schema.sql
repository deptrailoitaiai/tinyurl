-- =================================================================
-- Service 1: User Management Service
-- Quản lý thông tin người dùng.
-- =================================================================
CREATE DATABASE IF NOT EXISTS user_management_service;
USE user_management_service;

-- Bảng lưu trữ thông tin người dùng
CREATE TABLE `users` (
    `id` BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    `username` VARCHAR(50) NOT NULL,
    `email` VARCHAR(255) NOT NULL,
    `password_hash` VARCHAR(255) NOT NULL,
    `full_name` VARCHAR(255) NULL,
    `email_verified` BOOLEAN NULL DEFAULT FALSE,
    `status` ENUM('active', 'suspended') NULL DEFAULT 'active',
    `created_at` TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `last_login_at` TIMESTAMP NULL,
    UNIQUE KEY `users_username_unique` (`username`),
    UNIQUE KEY `users_email_unique` (`email`)
);


-- =================================================================
-- Service 2: URL Shortener Service
-- Quản lý việc tạo và truy cập các URL rút gọn.
-- =================================================================
CREATE DATABASE IF NOT EXISTS url_shortener_service;
USE url_shortener_service;

-- Bảng lưu trữ các URL
CREATE TABLE `urls` (
    `id` BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    `user_id` BIGINT NULL, -- Tham chiếu đến `users.id` trong `user_management_service`
    `original_url` TEXT NOT NULL,
    `short_code` VARCHAR(20) NOT NULL,
    `title` VARCHAR(500) NULL,
    `password_hash` VARCHAR(255) NULL,
    `status` ENUM('active', 'expired', 'disabled') NULL DEFAULT 'active',
    `created_at` TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `expires_at` TIMESTAMP NULL,
    UNIQUE KEY `urls_short_code_unique` (`short_code`)
);

-- Bảng quản lý tham chiếu cross-service (tối giản)
CREATE TABLE `service_references` (
    `id` BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    `local_id` BIGINT NOT NULL,                 -- ID của record trong bảng local
    `local_table` VARCHAR(100) NOT NULL,        -- Tên bảng trong service hiện tại
    `target_id` BIGINT NOT NULL,                -- ID của record được tham chiếu
    `target_table` VARCHAR(100) NOT NULL,       -- Tên bảng được tham chiếu
    UNIQUE KEY `service_ref_unique` (`local_table`, `local_id`, `target_table`, `target_id`)
);

-- Ghi dữ liệu mẫu để demo cross-service reference
-- INSERT INTO `service_references` (`local_id`, `local_table`, `target_id`, `target_table`) 
-- VALUES (1, 'urls', 1001, 'users'),
--        (2, 'urls', 1001, 'users'),
--        (3, 'urls', 1002, 'users');


-- =================================================================
-- Service 3: Analytics Batch Service
-- Xử lý và tổng hợp dữ liệu click theo ngày.
-- =================================================================
CREATE DATABASE IF NOT EXISTS analytics_batch_service;
USE analytics_batch_service;

-- Bảng thống kê số lượt click mỗi ngày cho mỗi URL
CREATE TABLE `url_daily_stats` (
    `id` BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    `url_id` BIGINT NOT NULL, -- Tham chiếu đến `urls.id` trong `url_shortener_service`
    `date` DATE NOT NULL,
    `click_count` BIGINT NOT NULL DEFAULT 0,
    `last_processed_click_id` BIGINT NULL, -- Track click cuối cùng đã xử lý
    `last_processed_at` TIMESTAMP NULL, -- Thời điểm cập nhật gần nhất
    UNIQUE KEY `url_date_unique` (`url_id`, `date`)
);

-- Bảng quản lý tham chiếu cross-service (tối giản)
CREATE TABLE `service_references` (
    `id` BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    `local_id` BIGINT NOT NULL,                 -- ID của record trong bảng local
    `local_table` VARCHAR(100) NOT NULL,        -- Tên bảng trong service hiện tại
    `target_id` BIGINT NOT NULL,                -- ID của record được tham chiếu
    `target_table` VARCHAR(100) NOT NULL,       -- Tên bảng được tham chiếu
    UNIQUE KEY `service_ref_unique` (`local_table`, `local_id`, `target_table`, `target_id`)
);

-- Ghi dữ liệu mẫu để demo cross-service reference
-- INSERT INTO `service_references` (`local_id`, `local_table`, `target_id`, `target_table`) 
-- VALUES (1, 'url_daily_stats', 1, 'urls'),
--        (2, 'url_daily_stats', 1, 'urls');


-- =================================================================
-- Service 4: Analytics Real-time Service
-- Ghi nhận các sự kiện click và thông tin ngữ cảnh (vị trí, thiết bị).
-- =================================================================
CREATE DATABASE IF NOT EXISTS analytics_realtime_service;
USE analytics_realtime_service;

-- Bảng lưu thông tin vị trí
CREATE TABLE `locations` (
    `id` BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    `country_code` VARCHAR(2) NOT NULL,
    `country_name` VARCHAR(100) NOT NULL,
    `city` VARCHAR(100) NULL,
    `created_at` TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP
);

-- Bảng lưu thông tin thiết bị
CREATE TABLE `devices` (
    `id` BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    `device_type` ENUM('desktop', 'mobile', 'tablet', 'unknown') NOT NULL,
    `browser_name` VARCHAR(50) NULL,
    `os_name` VARCHAR(50) NULL
);

-- Bảng ghi nhận sự kiện click
CREATE TABLE `click_events` (
    `id` BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    `url_id` BIGINT NOT NULL, -- Tham chiếu đến `urls.id` trong `url_shortener_service`
    `short_code` VARCHAR(20) NOT NULL, -- Duplicate từ `urls` để tối ưu query
    `ip_address` VARCHAR(45) NULL,
    `referrer` VARCHAR(500) NULL,
    `device_id` BIGINT NULL,
    `location_id` BIGINT NULL,
    `clicked_at` TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,
    `processed` BOOLEAN DEFAULT FALSE, -- Đánh dấu đã xử lý bởi batch service chưa
    -- Khóa ngoại trong cùng một service được giữ lại
    CONSTRAINT `click_events_location_id_foreign` FOREIGN KEY (`location_id`) REFERENCES `locations` (`id`),
    CONSTRAINT `click_events_device_id_foreign` FOREIGN KEY (`device_id`) REFERENCES `devices` (`id`)
);

-- Bảng quản lý tham chiếu cross-service (tối giản)
CREATE TABLE `service_references` (
    `id` BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    `local_id` BIGINT NOT NULL,                 -- ID của record trong bảng local
    `local_table` VARCHAR(100) NOT NULL,        -- Tên bảng trong service hiện tại
    `target_id` BIGINT NOT NULL,                -- ID của record được tham chiếu
    `target_table` VARCHAR(100) NOT NULL,       -- Tên bảng được tham chiếu
    UNIQUE KEY `service_ref_unique` (`local_table`, `local_id`, `target_table`, `target_id`)
);

-- Ghi dữ liệu mẫu để demo cross-service reference
-- INSERT INTO `service_references` (`local_id`, `local_table`, `target_id`, `target_table`) 
-- VALUES (1, 'click_events', 1, 'urls'),
--        (2, 'click_events', 1, 'urls');