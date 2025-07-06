-- =================================================================
-- Index Definitions for TinyURL Microservices
-- Được áp dụng cho slave databases để tối ưu hóa read performance
-- Không nên chạy trên primary database để tránh làm chậm write operations
-- =================================================================

-- =================================================================
-- Service 1: User Management Service Indexes
-- =================================================================
USE user_management_service;

-- Indexes cho bảng users
CREATE INDEX `users_username_index` ON `users` (`username`);
CREATE INDEX `users_email_index` ON `users` (`email`);
CREATE INDEX `users_status_index` ON `users` (`status`);
CREATE INDEX `users_created_at_index` ON `users` (`created_at`);
CREATE INDEX `users_last_login_at_index` ON `users` (`last_login_at`);

-- =================================================================
-- Service 2: URL Shortener Service Indexes
-- =================================================================
USE url_shortener_service;

-- Indexes cho bảng urls
CREATE INDEX `urls_user_id_index` ON `urls` (`user_id`);
CREATE INDEX `urls_status_index` ON `urls` (`status`);
CREATE INDEX `urls_created_at_index` ON `urls` (`created_at`);
CREATE INDEX `urls_expires_at_index` ON `urls` (`expires_at`);
CREATE INDEX `urls_title_index` ON `urls` (`title`);

-- Indexes cho bảng service_references (tối giản)
CREATE INDEX `service_references_local_id_index` ON `service_references` (`local_id`);
CREATE INDEX `service_references_local_table_index` ON `service_references` (`local_table`);
CREATE INDEX `service_references_target_id_index` ON `service_references` (`target_id`);
CREATE INDEX `service_references_target_table_index` ON `service_references` (`target_table`);

-- Composite indexes cho queries phổ biến
CREATE INDEX `service_ref_local_composite` ON `service_references` (`local_table`, `local_id`);
CREATE INDEX `service_ref_target_composite` ON `service_references` (`target_table`, `target_id`);

-- =================================================================
-- Service 3: Analytics Batch Service Indexes
-- =================================================================
USE analytics_batch_service;

-- Indexes cho bảng url_daily_stats
CREATE INDEX `url_daily_stats_url_id_index` ON `url_daily_stats` (`url_id`);
CREATE INDEX `url_daily_stats_date_index` ON `url_daily_stats` (`date`);
CREATE INDEX `url_daily_stats_click_count_index` ON `url_daily_stats` (`click_count`);
CREATE INDEX `url_daily_stats_last_processed_at_index` ON `url_daily_stats` (`last_processed_at`);

-- Indexes cho bảng service_references
CREATE INDEX `service_references_local_table_index` ON `service_references` (`local_table`);
CREATE INDEX `service_references_local_id_index` ON `service_references` (`local_id`);
CREATE INDEX `service_references_target_service_index` ON `service_references` (`target_service`);
CREATE INDEX `service_references_target_table_index` ON `service_references` (`target_table`);
CREATE INDEX `service_references_target_id_index` ON `service_references` (`target_id`);
CREATE INDEX `service_references_relationship_type_index` ON `service_references` (`relationship_type`);

-- Indexes cho bảng service_references (tối giản)
CREATE INDEX `service_references_local_id_index` ON `service_references` (`local_id`);
CREATE INDEX `service_references_local_table_index` ON `service_references` (`local_table`);
CREATE INDEX `service_references_target_id_index` ON `service_references` (`target_id`);
CREATE INDEX `service_references_target_table_index` ON `service_references` (`target_table`);

-- Composite indexes cho queries phổ biến
CREATE INDEX `service_ref_local_composite` ON `service_references` (`local_table`, `local_id`);
CREATE INDEX `service_ref_target_composite` ON `service_references` (`target_table`, `target_id`);

-- Composite indexes cho url_daily_stats
CREATE INDEX `url_daily_stats_url_date_composite` ON `url_daily_stats` (`url_id`, `date`);
CREATE INDEX `url_daily_stats_date_click_composite` ON `url_daily_stats` (`date`, `click_count`);

-- =================================================================
-- Service 4: Analytics Real-time Service Indexes
-- =================================================================
USE analytics_realtime_service;

-- Indexes cho bảng locations
CREATE INDEX `locations_country_code_index` ON `locations` (`country_code`);
CREATE INDEX `locations_country_name_index` ON `locations` (`country_name`);
CREATE INDEX `locations_city_index` ON `locations` (`city`);

-- Indexes cho bảng devices
CREATE INDEX `devices_device_type_index` ON `devices` (`device_type`);
CREATE INDEX `devices_browser_name_index` ON `devices` (`browser_name`);
CREATE INDEX `devices_os_name_index` ON `devices` (`os_name`);

-- Indexes cho bảng click_events
CREATE INDEX `click_events_url_id_index` ON `click_events` (`url_id`);
CREATE INDEX `click_events_short_code_index` ON `click_events` (`short_code`);
CREATE INDEX `click_events_clicked_at_index` ON `click_events` (`clicked_at`);
CREATE INDEX `click_events_processed_index` ON `click_events` (`processed`);
CREATE INDEX `click_events_ip_address_index` ON `click_events` (`ip_address`);
CREATE INDEX `click_events_device_id_index` ON `click_events` (`device_id`);
CREATE INDEX `click_events_location_id_index` ON `click_events` (`location_id`);

-- Indexes cho bảng service_references (tối giản)
CREATE INDEX `service_references_local_id_index` ON `service_references` (`local_id`);
CREATE INDEX `service_references_local_table_index` ON `service_references` (`local_table`);
CREATE INDEX `service_references_target_id_index` ON `service_references` (`target_id`);
CREATE INDEX `service_references_target_table_index` ON `service_references` (`target_table`);

-- Composite indexes cho queries phổ biến
CREATE INDEX `service_ref_local_composite` ON `service_references` (`local_table`, `local_id`);
CREATE INDEX `service_ref_target_composite` ON `service_references` (`target_table`, `target_id`);

-- Composite indexes cho click_events
CREATE INDEX `click_events_url_clicked_composite` ON `click_events` (`url_id`, `clicked_at`);
CREATE INDEX `click_events_processed_clicked_composite` ON `click_events` (`processed`, `clicked_at`);
CREATE INDEX `click_events_short_code_clicked_composite` ON `click_events` (`short_code`, `clicked_at`);
